package com.github.vtitov.kafka.pipeline

import java.io.File
import java.util
import java.util.UUID

import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
import com.github.vtitov.kafka.pipeline.emulator.RemoteEmulator
import com.github.vtitov.kafka.pipeline.kafka.{Admin, Consumer, Producer}
import com.github.vtitov.kafka.pipeline.rest.RestService.noPrettyPrinter
import com.typesafe.scalalogging.StrictLogging
import net.manub.embeddedkafka.EmbeddedKafka.{startKafka, startZooKeeper, zookeeperPort}
import net.manub.embeddedkafka.ops.AdminOps
import net.manub.embeddedkafka.{EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig, EmbeddedKafkaConfigImpl, EmbeddedZ}
import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration._
import scala.reflect.io.Directory

//import io.restassured.RestAssured.when
//import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
//import org.hamcrest.Matchers.equalTo

import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.syntax._
//import org.http4s._
//import org.http4s.circe._
import com.github.vtitov.kafka.pipeline.json._
import com.github.vtitov.kafka.pipeline.topology.DedupMessageGenerator._
import io.circe.parser._
import resource._

class DedupSpec
  extends StrictLogging
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
{
  import com.github.vtitov.kafka.pipeline.config.{allTopics, globalConfig, render}

  // TODO read from application conf
  //lazy val tempDirName = "E:/Users/Public/vt/local/msys2-x86_64-latest/msys64/tmp"
  //lazy val tempDir = new File(tempDirName)
  lazy val tempDir = null
  lazy val messgesToSendNumber = 128
  lazy val batchPause = 50L
  lazy val batchSize = 64
  implicit val config = {

    //lazy val zkSnapshotsDir = Directory.makeTemp("zookeeper-snapshot", "-dir", new File(tempDirName))
    //lazy val zkLogsDir = Directory.makeTemp("zookeeper-logs", "-dir", new File(tempDirName))
    //lazy val kafkaLogsDir = Directory.makeTemp("kafka-logs", "-dir", tempDir)
    new EmbeddedKafkaConfigImpl(
      kafkaPort = 7031,
      zooKeeperPort = 7030,
      customBrokerProperties = Map(
        "delete.topic.enable"-> "true",
        "auto.create.topics.enable" -> "true",
        //"log.flush.interval.messages" -> "1",
        //"log.dir" -> kafkaLogsDir.toAbsolute.path
      ),
      customProducerProperties = Map.empty,
      customConsumerProperties = Map(
        "group.id" -> groupId,
      ),
    ) with AdminOps[EmbeddedKafkaConfig]
    //or with AdminOps[EmbeddedKafkaConfigImpl]
  }

  lazy val groupId = s"it-${UUID.randomUUID().toString}"

  implicit val backend = HttpURLConnectionBackend() // TODO concider http4s backend

  lazy val httpPort = globalConfig.http.port.value
  lazy val httpEndpoint = s"http://localhost:${httpPort}"
  lazy val httpRestEndpoint = s"${httpEndpoint}/v1/api/rest/integration"

  private val ZOOKEEPER_DATA_DIR = "dataDir"
  private val ZOOKEEPER_DATA_LOG_DIR = "dataLogDir"
  private val KAFKA_LOG_DIR = "log.dir"
  private val KAFKA_SETTINGS = Map(
    ZOOKEEPER_DATA_DIR -> "zookeeper-snapshot",
    ZOOKEEPER_DATA_LOG_DIR -> "zookeeper-logs",
    KAFKA_LOG_DIR -> "kafkla-log-dirs")

  val resources:mutable.Map[String,AutoCloseable] = mutable.LinkedHashMap()



  override def beforeAll() = {
    // start embedded kafka
    EmbeddedKafka.start()

    val admin = new Admin()
    allTopics.foreach { t => logger.debug(s"createCustomTopic [$t]"); admin.createCustomTopic(t) }
    //allTopics.foreach { t => logger.debug(s"createCustomTopic [$t]"); config.createCustomTopic(t) }
    //com.github.vtitov.kafka.pipeline.cli.execApp(Seq("all-services"))
    // start http
    resources += "http" -> com.github.vtitov.kafka.pipeline.cli.startRestService(null)
    // start streams
    System.setProperty("application.id", s"app-kafka-pipeline-it-test-${UUID.randomUUID()}")
    resources += "streams" -> com.github.vtitov.kafka.pipeline.cli.startStreamsWithShutdownHookThread

    lazy val isLoopback = globalConfig.remoteSystem.loopback.getOrElse(false)
    if(!isLoopback) {
      resources += "emulator" -> RemoteEmulator.run()
    }
    resources += "consumer" -> new Consumer(Map("group.id" -> s"it-${UUID.randomUUID.toString}"))

    // check http connection
    (1 to 42).find{i=>
      logger.debug(s"ping attempt $i")
      Thread.sleep((i*100).toLong)
      val rq = sttp.get(uri"${httpEndpoint}/ping")
      logger.debug(s"ping request [$rq]")
      val rs = rq.send()
      logger.debug(s"ping response [$rs]")
      rs.isSuccess
    } should not be None // no response
  }
  //override def afterAll():Unit = afterAllImpl()
  private def afterAllImpl():Unit = {
    // stop streams
    // stop http
    resources.toSeq.reverse.toMap.foreach{case (k,v) =>
      logger.debug(s"closing $k")
      v.close()
    }
    resources("http").close()

    // config.deleteTopics(allTopics.toList)
    // stop embedded kafka
    EmbeddedKafka.stop()
  }
  "service" can {
    "sub-suite" should {
      "dump-config" in {
        // logger.debug(s"globalConfig asJson ${globalConfig.asJson}") ??? could not find implicit value for parameter encoder
        logger.debug(s"render globalConfig ${render(globalConfig)}")
      }
      "ping" in {
        sttp.get(uri"${httpEndpoint}/ping").send().isSuccess shouldBe true
      }
      "send-via-kafka" in {
        //val messagesToSendViaKafka = (genKeys zip genLoremTexts).take(1024).toList
        //val messagesToSendViaKafka = (genKeysInt.toList zip genLoremTexts.toList).take(1024).toList
        //val messagesToSendViaKafka = (genKeys zip genLoremTextsStream).take(1024).toList
        val messagesToSendViaKafka = (genKeysInt zip genKeysUid).take(messgesToSendNumber).toList
        managed(new Producer) acquireAndGet { producer =>
          logger.debug(s"sending with producer [$producer])")
          val totallySend = (messagesToSendViaKafka ++ messagesToSendViaKafka)
          totallySend.zipWithIndex.foreach{ case((k,v),idx) =>
            //note(s"send to kafka $k -> $v")
            logger.debug(s"send $k -> $v")
            //producer.send(globalConfig.remoteSystem.topics.inTopic, k,v)
            producer.send(globalConfig.remoteSystem.topics.generalInTopic, null, v)
            //if(idx % batchSize == 0) {Thread.sleep(batchPause)}
          }
          logger.debug(s"totally sent ${totallySend.length} messages")
          //producer.flush()
        }

        managed(new Consumer(Map("group.id" -> groupId))) acquireAndGet { consumer =>
          logger.debug(s"consumer [$consumer]")
          val crl:ConsumerRebalanceListener = new ConsumerRebalanceListener(){
            override def onPartitionsRevoked (partitions: util.Collection[TopicPartition]): Unit = logger.debug(s"partitions revoked: [${partitions.asScala}]")
            override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = logger.debug(s"partitions assigned: [${partitions.asScala}]")
          }
          consumer.consumer.subscribe(Seq(remoteInTopic).asJava,crl)

          note(s"subscriptions: [${consumer.consumer.subscription().asScala}]")
          logger.debug(s"subscriptions: [${consumer.consumer.subscription().asScala}]")
          //val consumed: List[ConsumerRecord[String, String]] = consumer.consumer.poll(30.second.toJava).asScala.toList
          val consumed = (1 to 10).flatMap { idx =>
            val chunk = consumer.consumer.poll(10.second.toJava).asScala
            note(s"chunk $idx w/ ${chunk.size} records")
            logger.debug(s"chunk $idx w/ ${chunk.size} records")
            chunk
          }.toStream

          note(s"tolally consumed ${consumed.size} records")
          logger.debug(s"tolally consumed records $consumed")
          //lazy val allFiles = Seq(filesSendViaHttp, filesToSendViaKafka)
          lazy val allMessges = messagesToSendViaKafka
          //consumed.length shouldEqual allMessges.length
          consumed.zip(allMessges).map{ case(cr,ff) =>
            //note(s"consumed record $cr")
            logger.debug(s"consumed record $cr")
            1
          }.length shouldEqual allMessges.length
        }
        assert(true)
      }
    }
  }
}
