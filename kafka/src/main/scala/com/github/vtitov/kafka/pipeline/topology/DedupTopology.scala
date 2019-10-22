package com.github.vtitov.kafka.pipeline.topology

import java.time.Instant
import java.time.temporal.ChronoField._

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.{KeyValue, Topology}
import org.apache.kafka.streams.kstream.GlobalKTable
//import org.apache.kafka.streams.scala.kstream.{KStream, KTable, Materialized, _}
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.state.Stores

import com.github.vtitov.kafka.pipeline.config.globalConfig
import globalConfig.remoteSystem.topics._
import com.github.vtitov.kafka.pipeline.json._

import Serdes._


object DedupTopology extends StrictLogging {

  lazy val shardingFactor = globalConfig.remoteSystem.shardedTopics.shardsNumber.value

//  type RawDedupK = String
//  type RawDedupV = String
//  type DedupK = String
//  type DedupV = String
  type RawDedupK = Array[Byte]
  type RawDedupV = Array[Byte]
  type DedupK = Array[Byte]
  type DedupV = Array[Byte]
  type DedupKV = KeyValue[DedupK,DedupV]
  type DedupProducerRecord = ProducerRecord[DedupK,DedupV]
  type DedupPair = (DedupK,DedupV)
  type DedupPairWithRight = (DedupK,(DedupV,DedupV))


  lazy val digestAlgorithm = "SHA-256"
  lazy val digestBytes: Array[Byte]=>Array[Byte] = java.security.MessageDigest.getInstance(digestAlgorithm).digest

  lazy val debug = globalConfig.remoteSystem.loopback.getOrElse(false)
  lazy val isLoopback = globalConfig.remoteSystem.loopback.getOrElse(false)
  lazy val isInMemoryKeyValueStore = globalConfig.remoteSystem.inMemoryKeyValueStore.getOrElse(false)

  def takeElementsFromValueHead(v:DedupV, n:Int = 100):DedupV = {
    if(v == null) Array.empty
    else v.take(n)
  }

  def materializedAs[K, V](storeName: String)(implicit keySerde: Serde[K], valueSerde: Serde[V]): Materialized[K, V, ByteArrayKeyValueStore] ={
    if(!isInMemoryKeyValueStore) Materialized.as(storeName)                          // 'default' store for production
    else Materialized.as(Stores.inMemoryKeyValueStore(storeName))  // inMemoryKeyValueStore for unit tests
  }

  def buildTopology(probe: Option[ActorRef] = None):Topology = buildTopologyImpl(probe)


  def buildTopologyImpl(probe: Option[ActorRef] = None):Topology = {
    val topo = new DedupTopology()
      //.addMainTopologyNoShardes
      .addMainTopologyShardes
      .builder.build
    logger.info(s"topology built: ${topo.describe()}")
    topo
  }
}


class DedupTopology extends StrictLogging {
  import DedupTopology._

  lazy val builder = new StreamsBuilder

  lazy val generalInStream: KStream[RawDedupK,RawDedupV] = builder.stream[RawDedupK,RawDedupV](generalInTopic)
  lazy val indexedInStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](inTopic)

  //lazy val duplicatesStream: KStream[String, String] = builder.stream[String, String](duplicatesTable)
  lazy val duplicatesTblStream: GlobalKTable[DedupK,DedupV] = builder.globalTable[DedupK,DedupV](duplicatesTable)
  lazy val duplicatesTblStreams: List[GlobalKTable[DedupK,DedupV]] = (0 until shardingFactor).map { idx =>
    logger.debug(s"builder.stream $duplicatesTable.$idx")
    builder.globalTable[DedupK, DedupV](s"$duplicatesTable.$idx")
  }.toList

  //lazy val toRemoteTblStream: GlobalKTable[DedupK,DedupV] = builder.globalTable[DedupK,DedupV](toRemoteTable)
  //lazy val fromRemoteTblStream: GlobalKTable[DedupK,DedupV] = builder.globalTable[DedupK,DedupV](fromRemoteTable)

  lazy val toRemoteStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](toRemoteTable)
  lazy val toRemoteStreams = (0 until shardingFactor).map { idx =>
    logger.debug(s"builder.stream $toRemoteTable.$idx")
    builder.stream[DedupK, DedupV](s"$toRemoteTable.$idx")
  }
//  lazy val fromRemoteTblStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](fromRemoteTable)
//  lazy val toMonitoringTblStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](toMonitoringTable)


  lazy val remoteOutStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](remoteOutTopic)
  lazy val remoteInStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](remoteInTopic)
//  lazy val toMonitoringStream: KStream[DedupK,DedupV] = builder.stream[DedupK,DedupV](toMonitoringTopic)


  def calculateSharde(k:DedupK) = k.head % shardingFactor
  def shardingPredicates = (0 until shardingFactor) map { idx =>
    (k:DedupK, v:DedupV) => calculateSharde(k) == idx
  }

  def addIndexingTopology/*:Topology*/ = {
    generalInStream
      .map{case (_,v) =>
        //val newK:String = java.lang.String.format("%064x", new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(v)))
        //val newK:String = java.security.MessageDigest.getInstance(digestAlgorithm).digest(v).map("%02x".format(_)).mkString
        //val newK = java.security.MessageDigest.getInstance(digestAlgorithm).digest(v)
        val newK = digestBytes(v)
        (newK,v)
        }
      .to(inTopic)
    this
  }

  def addMainTopologyShardes0(implicit probe: Option[ActorRef] = None)/*:Topology*/ = { // TODO remove too naive approach
    val branched = indexedInStream
      //.peek { case (k, v) => logger.debug(s"read from input: $k") }
      .peek { case (k, v) => logger.debug(s"read from input: $k -> $v") }
      .branch(shardingPredicates: _*)

    branched.zipWithIndex.foreach{ case (branch, idx) =>
      branch.leftJoin(duplicatesTblStreams(idx))(
        { case (k, v) => k },
        //{case(v,rv) => if(rv==null) v else null}
        { case (v, rv) =>
          if (rv == null) {
            val inst = Instant.now();
            (inst.get(MILLI_OF_SECOND) + inst.getEpochSecond * 1000).toString
          }
          else null
        }
      )
        .peek { case (k, v) => logger.debug(s"join ($idx) result: ${k}") }
        .filter { case (k, v) =>
          v != null
        }
        .to(s"$toRemoteTable.$idx")
    }
    toRemoteStreams.zipWithIndex.foreach { case (stream, idx) =>
      stream
        .peek { case (k, v) => logger.debug(s"to duplicatesTable:$idx ($duplicatesTable.$idx): ${k}") }
        .to(s"$duplicatesTable.$idx")
      stream
        .peek { case (k, v) => logger.debug(s"to remoteInTopic ($remoteInTopic): ${k}") }
        .to(remoteInTopic)
    }
    addIndexingTopology
    this
  }

  def addMainTopologyShardesWithTimestamp(implicit probe: Option[ActorRef] = None)/*:Topology*/ = { // some optimization
    val branched = indexedInStream
      //.peek { case (k, v) => logger.debug(s"read from input: $k") }
      .peek { case (k, v) => logger.debug(s"read from input: $k -> $v") }
      .branch(shardingPredicates: _*)

    branched.zipWithIndex.foreach{ case (branch, idx) =>
      branch.leftJoin(duplicatesTblStreams(idx))(
        { case (k, v) => k },
        { case(v,rv) => if(rv==null) v else null}
//        { case (v, rv) =>
//          if (rv == null) {
//            val inst = Instant.now();
//            (inst.get(MILLI_OF_SECOND) + inst.getEpochSecond * 1000).toString
//          }
//          else null
//        }
      )
        .peek { case (k, v) => logger.debug(s"join ($idx) result: ${k}") }
        .filter { case (k, v) =>
          v != null
        }
        .to(s"$toRemoteTable.$idx")
    }
    toRemoteStreams.zipWithIndex.foreach { case (stream, idx) =>
      stream
        .map{case(k,v) => // we do not want to store whole message in the cache
          val inst = Instant.now()
          val millis = inst.get(MILLI_OF_SECOND) + inst.getEpochSecond * 1000
          k -> BigInt(millis).toByteArray
        }
        .peek { case (k, v) => logger.debug(s"to duplicatesTable:$idx ($duplicatesTable.$idx): ${k}") }
        .to(s"$duplicatesTable.$idx")
      stream
        .peek { case (k, v) => logger.debug(s"to remoteInTopic ($remoteInTopic): ${k}") }
        .to(remoteInTopic)
    }
    addIndexingTopology
    this
  }

  def addMainTopologyShardes(implicit probe: Option[ActorRef] = None)/*:Topology*/ = {
    val branched = indexedInStream
      //.peek { case (k, v) => logger.debug(s"read from input: $k") }
      .peek { case (k, v) =>
        logger.debug(s"read from input: $k -> ${takeElementsFromValueHead(v)}")
      }
      .branch(shardingPredicates: _*)

    branched.zipWithIndex.foreach{ case (branch, idx) =>
      branch.leftJoin(duplicatesTblStreams(idx))(
        { case (k, v) => k },
        { case(v,rv) => if(rv==null) v else null}
      )
        .peek { case (k, v) => logger.debug(s"join ($idx) result: ${k} -> ${takeElementsFromValueHead(v)}") }
        .filter { case (k, v) => v != null }
        .peek { case (k, v) => logger.debug(s"join ($idx) non-null result: ${k} -> ${takeElementsFromValueHead(v)}") }
        .to(s"$toRemoteTable.$idx")
    }
    toRemoteStreams.zipWithIndex.foreach { case (stream, idx) =>
      stream
        .peek { case (k, v) => logger.debug(s"to duplicatesTable:$idx ($duplicatesTable.$idx): ${k} -> ${takeElementsFromValueHead(v)}") }
        .to(s"$duplicatesTable.$idx")
      stream
        .peek { case (k, v) => logger.debug(s"to remoteInTopic ($remoteInTopic): ${k} -> ${takeElementsFromValueHead(v)}") }
        .to(remoteInTopic)
    }
    addIndexingTopology
    this
  }

  def addMainTopologyNoShardes(implicit probe: Option[ActorRef] = None)/*:Topology*/ = {
    indexedInStream
      .peek{ case (k,v) => logger.debug(s"read from input: ${k}")}
      .leftJoin(duplicatesTblStream)(
        {case(k,v)=> k},
        //{case(v,rv) => if(rv==null) v else null}
        {case(v,rv) =>
          //if(rv==null) {val inst = Instant.now(); (inst.get(MILLI_OF_SECOND) + inst.getEpochSecond * 1000).toString}
          if(rv==null) {v}
          else null
        }
      )
      .peek{ case (k,v) => logger.debug(s"join result: ${k}")}
      .filter{case(k,v) => v != null}
      .through(toRemoteTable)
      .map{case(k,v) => // we do not want to store whole message in the cache
        val inst = Instant.now()
        val millis = inst.get(MILLI_OF_SECOND) + inst.getEpochSecond * 1000
        k -> BigInt(millis).toByteArray
      }
      .to(duplicatesTable)

//    toRemoteStream
//      .peek{ case (k,v) => logger.debug(s"to duplicatesTable: ${k}")}
//      .to(duplicatesTable)
    toRemoteStream
      .peek{ case (k,v) => logger.debug(s"to remoteInTopic: ${k}")}
      .to(remoteInTopic)

    //duplicatesStream.to(remoteInTopic)
    //duplicatesStream.to(remoteInTopic)
    addIndexingTopology
    this
  }

//  def addMainTopology1(implicit probe: Option[ActorRef] = None)/*:Topology*/ = {
////    inStream
////      .peek{ case (k,v) => logger.debug(s"for remote: ${v}")}
////      .groupByKey
////      //.aggregate()
////      .reduce{ case(fst,_) => fst } (materializedAs(duplicatesSink))
////      .toStream
////      .to(duplicatesTable)
//
//
//
//    val predicates:Seq[(String, (String, String)) => Boolean] = Seq(
//      {
//        case(k:DedupK,(v:DedupV,vr:DedupV)) => (vr == null)
//        case other => throw new IllegalArgumentException(s"$other")
//      },
//      {
//        case(k:DedupK,(v:DedupV,vr:DedupV)) => (vr != null)
//        case other => throw new IllegalArgumentException(s"$other")
//      }
//    )
//
//    val branched = inStream
//        .leftJoin(duplicatesTblStream)(
//          {case(k,v)=> k},
//          {case(v,rv) => (v,rv)}
//        )
//      .peek{ case (k,v) => logger.debug(s"join result: ${v}")}
//      //.foreach{case(k,v) => }
//      .branch(predicates:_*)
//    branched(0)
//      .map{case(k:DedupK,(v:DedupV,vr:DedupV))=>
//        logger.debug(s"adding to duplicates store: $k -> $v")
//        (k,v)
//      }
//      .to(duplicatesTable)
//    branched(1)
//      .foreach{case(k:DedupK,(v:DedupV,vr:DedupV)) =>
//        logger.debug(s"skipping duplicate: $k -> $v")
//      }
//    this
//  }
//
//  def addMainTopologyReduced(implicit probe: Option[ActorRef] = None)/*:Topology*/ = {
//    inStream
//      .peek{ case (k,v) => logger.debug(s"for remote: ${v}")}
//      .groupByKey
//      .reduce{ case(fst,_) => null } (materializedAs(duplicatesSink))
//      .toStream
//      .to(toRemoteTable)
//    this
//  }

  //  def createTopology/*:Topology*/ = {
  //    builder.build()
  //  }
}
