package com.github.vtitov.kafka.pipeline.topology

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.DirectoryNotEmptyException
import java.util.UUID

import com.github.javafaker.Faker
import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
import com.github.vtitov.kafka.pipeline.helpers._
import com.github.vtitov.kafka.pipeline.json.{fromJsonString, toJsonString}
import com.github.vtitov.kafka.pipeline.types.{DataReadyFromSender, newRqUid}
import com.typesafe.scalalogging.StrictLogging

import scala.annotation.tailrec
import net.manub.embeddedkafka.Codecs._
import net.manub.embeddedkafka.ConsumerExtensions._
import net.manub.embeddedkafka.EmbeddedKafkaConfig
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.{KeyValue, TopologyTestDriver}
import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsException
import org.scalatest.{Matchers, WordSpec}
import resource._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}


object DedupMessageGenerator {
  lazy val faker = new Faker()

  def genMessagesToSend: Seq[DataReadyFromSender] = Stream.from(1) map { n =>
    (0 until n) map { _ =>
      faker.file().fileName(null, null, null, "/")
    }
  } map { fs: Seq[String] =>
    val uid = newRqUid
    fromJsonString(toJsonString(DataReadyFromSender(files = fs, uid = Some(uid))))
  }

  def genKeys = genKeysUid
  //def genKeys = genKeysInt
  //def genKeysInt = Stream.from(1000).map(_.toString)
  def genKeysInt = Stream.from(1).map(_.toString)
  def genKeysUid = Stream.continually(()).map(_=>newRqUid)

//  def genLoremTexts = genLoremTextsStream.take(7)
//  def genLoremTexts = genLoremTextsN.take(1000)
  def genLoremTexts = genLoremTextsStream.take(256)
  //def genLoremTexts = genLoremTextsStream.take(1000)

  def genLoremTextsN = Stream.from(1).map(_.toString)
  def genLoremTextsStream: Seq[String] = Stream.continually(()) map { _ =>
    faker.lorem().paragraph()
  }

  def genMessagesToSend3: Seq[DataReadyFromSender] = genMessagesToSend.take(3)

}


object DedupTopologySpec {
}
class DedupTopologySpec
  extends WordSpec
    //with BeforeAndAfterAll
    with Matchers
    with EmbeddedKafkaStreamsAllInOne
    //with TestKitBase
    with StrictLogging
{
  import DedupMessageGenerator._
  import DedupTopology._

  implicit lazy val config =
    EmbeddedKafkaConfig(kafkaPort = 7000, zooKeeperPort = 7001)

  "dedup-spec" can {
    logger.debug(s"from-sender-spec")
    "dedup-kafka-stream" should {

      "do-topology" in { // FIXME
        val testProps = config.customBrokerProperties ++
          Map(
            StreamsConfig.APPLICATION_ID_CONFIG -> ("app-" + UUID.randomUUID().toString),
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG -> s"localhost:${config.kafkaPort}",
            StreamsConfig.NUM_STREAM_THREADS_CONFIG -> s"16",
          )
        Try{
          managed(new TopologyTestDriver(buildTopology(), testProps)) acquireAndGet { case (testDriver) =>
            //@tailrec def readTillLastDedup(topic:String, last: DedupPair, acc:Stream[DedupPair] = Stream()):Stream[DedupPair] = {
            //  val msg:DedupProducerRecord = testDriver.readOutput(topic, new StringDeserializer(), new StringDeserializer())
            //  lazy val accNew = acc :+ (msg.key->msg.value)
            //  if(last._1 == msg.key()) accNew
            //  else readTillLastDedup(topic, last, accNew)
            //}

            @tailrec def readTillLastStrings(topic:String, last: (String,String), acc:Stream[(String,String)] = Stream()):Stream[(String,String)] = {
              val msg:ProducerRecord[String,String] = testDriver.readOutput(topic, new StringDeserializer(), new StringDeserializer())
              //lazy val accNew = acc :+ (msg.key->msg.value)
              if(msg==null) acc
              else readTillLastStrings(topic, last, acc :+ (msg.key->msg.value))
              //if(last._1 == msg.key()) accNew
              //else readTillLastStrings(topic, last, accNew)
            }

            //@tailrec def readTillLastKV[K,V]
            //(topic:String, last: (K,V), acc:Stream[(K,V)] = Stream())
            //(implicit derK:Deserializer[K],derV:Deserializer[V])
            //:Stream[(K,V)] = {
            //  val msg = testDriver.readOutput(topic, derK, derV)
            //  //val msg = testDriver.readOutput(topic)
            //  //val key:Array[Byte]=msg.key
            //  //lazy val accNew = acc :+ (msg.key.asInstanceOf[K]->msg.value.asInstanceOf[V])
            //  lazy val accNew = acc :+ (msg.key.asInstanceOf[K]->msg.value.asInstanceOf[V])
            //  if(last._1 == msg.key ) accNew
            //  else readTillLastKV(topic, last, accNew)
            //}

            @tailrec def readTillLastBytes[K,V]
            (topic:String, last: (K,V), acc:Stream[(K,V)] = Stream())
            :Stream[(K,V)] = {
              val msg = testDriver.readOutput(topic)
              //val msg = testDriver.readOutput(topic)
              //val key:Array[Byte]=msg.key
              //lazy val accNew = acc :+ (msg.key.asInstanceOf[K]->msg.value.asInstanceOf[V])
              lazy val accNew = acc :+ (msg.key.asInstanceOf[K]->msg.value.asInstanceOf[V])
              if(last._1 == new String(msg.key, StandardCharsets.UTF_8) ) accNew
              else readTillLastBytes(topic, last, accNew)
            }


            logger.debug(s"all stores: ${testDriver.getAllStateStores}")
            lazy val kvStores = (toRemoteTable :: duplicatesTable :: Nil).map { table =>
              (table -> testDriver.getKeyValueStore(table).asInstanceOf[KeyValueStore[String, String]])
            }.toMap
            val storeToRemote: KeyValueStore[String, String] = kvStores(toRemoteTable)
            Map[String, String](
              //"1" -> "<a>1</a>"
              //,"2" -> "<b>2</b>"
            )
            //.foreach { case (k, v) => storeToRemote.put(k, v) }

            //lazy val loremMessagesToSend = genLoremTexts.map{ v => newRqUid -> v}
            //lazy val loremMessagesToSend = genLoremTexts.zipWithIndex.map{case(v,k) => k.toString -> v}
            //lazy val loremMessagesToSend = genLoremTexts.zip(genKeys).map{case(v,k) => k.toString -> v}
            lazy val loremMessagesToSend = genLoremTexts.map{
              /*case*/ v:String => (digestBytes(v.getBytes(StandardCharsets.UTF_8)), v)
              //case v:Array[Byte] => (digestBytes(v), v)
            }
            lazy val lastMessagesToSend = Seq(Seq.fill(32)('f').mkString -> "!!! last message !!!")
            lazy val duplicatesAppended = (loremMessagesToSend ++ loremMessagesToSend)
            lazy val duplicates = duplicatesAppended
            lazy val allMessagesToSend = (duplicates :+ lastMessagesToSend.head)
              .map{case(k,v) => new KeyValue(k, v)}
            note(s"allMessagesToSend (${allMessagesToSend.size}): ")
            allMessagesToSend.zipWithIndex.foreach{ case(msg,idx) =>
              //logger.debug(s"message to send ($idx): $msg")
            }
            //val factory = new ConsumerRecordFactory[String, String](inTopic, new StringSerializer(), new StringSerializer())
            val factory = new ConsumerRecordFactory[String, String](generalInTopic, new StringSerializer(), new StringSerializer())
            logger.debug(s"send all")
            //testDriver.pipeInput(factory.create(inTopic, allMessagesToSend.asJava))
            //allMessagesToSend.foreach(msg => testDriver.pipeInput(factory.create(inTopic, msg.key, msg.value)))
            //allMessagesToSend.foreach(msg => testDriver.pipeInput(factory.create(generalInTopic, null, msg.value)))
            allMessagesToSend.foreach(msg => testDriver.pipeInput(factory.create(msg.value)))
            logger.debug(s"all was sent")

            Seq(
              (0 until shardingFactor).map(idx=>s"$toRemoteTable.$idx"),
              (0 until shardingFactor).map(idx=>s"$duplicatesTable.$idx"),
              Seq(remoteInTopic)
            ).flatten.foreach{topic =>
              logger.debug(s"read $topic")
              val allTopicMsgs = readTillLastStrings(topic, lastMessagesToSend.head)
              note(s"all $topic messages (${allTopicMsgs.size}):")
              logger.debug(s"all $topic messages (${allTopicMsgs.size}):")
              allTopicMsgs.foreach{ msg =>
                //note(s"${msg}")
                //logger.debug(s"${msg}")
              }
            }
            logger.debug(s"that's it")
          }
        } match {
          case Success(s) =>
            note("cleaned")
            s
          case Failure(e:StreamsException) =>
            Option(e.getCause) match {
              case Some(nonEmpty:DirectoryNotEmptyException) =>
                note("could not clean")
                logger.debug(s"acceptable DirectoryNotEmptyException ${nonEmpty.getLocalizedMessage}")
              case None => throw e
              case _ => throw e
            }
          case Failure(e) => throw e
        }
      }
    }
  }
}
