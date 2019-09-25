//package com.github.vtitov.kafka.pipeline.topology
//
//import akka.actor.ActorRef
//import com.typesafe.scalalogging.StrictLogging
//import org.apache.kafka.common.serialization.Serde
//import org.apache.kafka.streams.kstream.GlobalKTable
////import org.apache.kafka.streams.scala.kstream.{KStream, KTable, Materialized, _}
//import org.apache.kafka.streams.scala.kstream._
//import org.apache.kafka.streams.scala.ImplicitConversions._
//import org.apache.kafka.streams.scala._
//import org.apache.kafka.streams.state.Stores
//
//import com.github.vtitov.kafka.pipeline.config.globalConfig
//import globalConfig.remoteSystem.topics._
//import com.github.vtitov.kafka.pipeline.json._
//
//import Serdes._
//
//
//class InOutTopology(probe: Option[ActorRef] = None) extends StrictLogging {
//  import InOutTopology._
//
//  lazy val builder = createBuilder()
//
//  lazy val toRemoteTblStream: GlobalKTable[String, String] = builder.globalTable[String, String](toRemoteTable)
//  //lazy val fromRemoteTblStream: GlobalKTable[String, String] = builder.globalTable[String, String](fromRemoteTable)
//
//
//  //lazy val toRemoteTblStream: KStream[String, String] = builder.stream[String, String](toRemoteTable)
//  lazy val fromRemoteTblStream: KStream[String, String] = builder.stream[String, String](fromRemoteTable)
//  lazy val toMonitoringTblStream: KStream[String, String] = builder.stream[String, String](toMonitoringTable)
//
//  lazy val inStream: KStream[String, String] = builder.stream[String, String](inTopic)
//
//  lazy val remoteOutStream: KStream[String, String] = builder.stream[String, String](remoteOutTopic)
//  lazy val remoteInStream: KStream[String, String] = builder.stream[String, String](remoteInTopic)
//  lazy val toMonitoringStream: KStream[String, String] = builder.stream[String, String](toMonitoringTopic)
//
//
//  def addMainTopology(implicit probe: Option[ActorRef] = None)/*:Topology*/ = {
//
//    inStream
//      .peek{case (k,v) =>
//        logger.debug(s"received message from client: $k -> $v")
//        probe.foreach(a => a ! ((k,v)))
//        //logger.info(s"sent greeting: $k -> $v")
//        logger.debug(s"received: $k -> $v")
//      }
//      .foreach{ case (k, v) => }
//
//    //    inStream
//    //      .map{case (uid, message) =>
//    //        val ffs = fromJsonString(message)
//    //        ffs.uid.getOrElse(uid) -> saveToXml(
//    //          createTransferFileCephRq(
//    //            filesFromSender = ffs
//    //            ,???
//    //          ))
//    //      }
//    inStream
//      //      .flatMap{case (uid, message) =>
//      //        logger.debug(s"received message from client: $uid -> $message")
//      //        val ffs = fromJsonString(message)
//      //        val _uid = ffs.uid.getOrElse(uid)
//      //        routeToRemoteForFilesEither(ffs).map{ route =>
//      //          _uid -> saveToXml(
//      //            createMessage(
//      //              filesFromSender = ffs
//      //              , route = route
//      //            ))
//      //        }.toSeq
//      //      }
//      .peek{ case (k,v) => logger.debug(s"for remote: ${v}")}
//      .groupByKey
//      .reduce{ case(fst,_) => fst } (materializedAs(toRemoteTable))
//      .toStream
//      .to(toRemoteTable)
//
//    inStream
//      .leftJoin(toRemoteTblStream)({case(k,v)=>k},{case (v,v0)=>if(v0==null) v else null})
//      .to(remoteInTopic)
//
//    remoteOutStream
//      .peek{case (k,v) => logger.debug(s"from remote: ${v}")}
//      .map{case (key, message) =>
//        (key, message)
//      }
//      //.groupByKey
//      //.reduce{case(_,n) => n}(materializedAs(fromRemoteTable))
//      //.aggregate{vr:String=>vr}{case(_,_:String,_:String)=>null)}
//      //.toStream
//      .to(fromRemoteTable)
//
//    fromRemoteTblStream
//      .peek{case (k,v) => logger.debug(s"fromRemoteTbl: ${v}")}
//      //      .flatMap{case (k,v) =>
//      //        monitoringOption(v).toSeq.flatMap{monitoring =>
//      //          monitoring.map((k,_))
//      //        }
//      //      }
//      .peek{case (k,v) => logger.debug(s"single message: ${v}")}
//      //      .groupByKey
//      //      .reduce{ case(_,n) => n } (materializedAs(toMonitoringTable))
//      //      .toStream
//      .to(toMonitoringTable)
//    //.foreach{ case (k, v) => }
//
//    toMonitoringTblStream
//      .peek{case (k,v) => logger.debug(s"toMonitoringStream: ${v}")}
//      .to(toMonitoringTopic)
//
//    //    inStream
//    //        .map { case (k: String, v: String) =>
//    //        }
//
//    if(isLoopback) {
//      inStream.mapValues(v=>v).to(inTopicDebug)
//      remoteInStream
//        .peek{case(k,v) => logger.debug(s"remote emu received $k -> $v")}
//        //.flatMap{case(k,v) => convertOption(v).map{rs => (k,rs)}.toIterable}
//        .peek{case(k,v) => logger.debug(s"remote emu sending $k -> $v")}
//        .to(remoteOutTopic)
//    }
//
//
//    //    //builder.addGlobalStore()
//    //    builder.addStateStore(
//    //      Stores.keyValueStoreBuilder(
//    //        Stores.inMemoryKeyValueStore("aggStore"),
//    //        Serdes.String,
//    //        Serdes.String).withLoggingDisabled, // need to disable logging to allow store pre-populating
//    //      "aggregator")
//    //    )
//    this
//  }
//
//
//
//  def addQueryTopology() = {
//    case class CaseClass(name: String, /*sink: String, */stream: KStream[String, String])
//    Seq(
//      //CaseClass(toRemoteTable, /*toRemoteSink,*/ toRemoteTblStream), // FIXME
//      CaseClass(fromRemoteTable, /*fromRemoteSink,*/ fromRemoteTblStream),
//      CaseClass(toMonitoringTable, /*toMonitoringSink,*/ toMonitoringStream),
//    ).foreach{case(CaseClass(name,/*sink,*/stream)) =>
//      stream.groupByKey
//        .reduce{ case(_,n) => n } (materializedAs(name))
//        .toStream
//        //.to(sink)
//        //.foreach{case (k,v) => }
//    }
////    toRemoteTblStream
////      .groupByKey
////      .reduce{ case(_,n) => n } (materializedAs(toRemoteTable))
////        .toStream
////        .foreach{case (k,v) => }
////    fromRemoteTblStream
////      .groupByKey
////      .reduce{ case(_,n) => n } (materializedAs(fromRemoteTable))
////      .toStream
////      .foreach{case (k,v) => }
////    toMonitoringTblStream
////      .groupByKey
////      .reduce{ case(_,n) => n } (materializedAs(toMonitoringTable))
////      .toStream
////      .foreach{case (k,v) => }
//    this
//  }
//
//
////  def createQueryTopology() = {
////  }
//
//  def createTopology/*:Topology*/ = {
//    builder.build()
//  }
//
//  }
//
//object InOutTopology extends StrictLogging
//{
//
//  lazy val debug = globalConfig.remoteSystem.loopback.getOrElse(false)
//  lazy val isLoopback = globalConfig.remoteSystem.loopback.getOrElse(false)
//  lazy val isInMemoryKeyValueStore = globalConfig.remoteSystem.inMemoryKeyValueStore.getOrElse(false)
//
//  def materializedAs[K, V](storeName: String)(implicit keySerde: Serde[K], valueSerde: Serde[V]): Materialized[K, V, ByteArrayKeyValueStore] ={
//    if(!isInMemoryKeyValueStore) Materialized.as(storeName)                          // 'default' store for production
//    else Materialized.as(Stores.inMemoryKeyValueStore(storeName))  // inMemoryKeyValueStore for unit tests
//  }
//
//  def createBuilder():StreamsBuilder = {
//    lazy val builder: StreamsBuilder = new StreamsBuilder
//    builder
//  }
//
//  def buildQueryTopology(probe: Option[ActorRef] = None) = buildQueryTopologyImpl(probe)
//  def buildTopology(probe: Option[ActorRef] = None) = buildTopologyImpl(probe)
//
//
//  def buildTopologyImpl(probe: Option[ActorRef] = None)/*:Topology*/ = {
//    def topology = new InOutTopology(probe)
//    .addMainTopology
//    .addQueryTopology()
//    .createTopology
//    logger.debug(s"TopologyDescription: ${topology.describe}")
//    topology
//  }
//
//  def buildQueryTopologyImpl(probe: Option[ActorRef] = None)/*:Topology*/ = {
//    val cephTopology = new InOutTopology
//    cephTopology.addQueryTopology()
//    cephTopology.createTopology
//  }
//
//}
