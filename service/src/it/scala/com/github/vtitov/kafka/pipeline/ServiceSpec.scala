//package com.github.vtitov.kafka.pipeline
//
//import java.util
//import java.util.UUID
//import java.util.regex.Pattern
//
//import com.typesafe.scalalogging.StrictLogging
//import net.manub.embeddedkafka.ops.AdminOps
//import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig, EmbeddedKafkaConfigImpl}
//import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, ConsumerRecord}
//import org.apache.kafka.common.TopicPartition
//import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
//import com.github.vtitov.kafka.pipeline.emulator.RemoteEmulator
//import com.github.vtitov.kafka.pipeline.kafka.{Consumer, Producer}
//import com.github.vtitov.kafka.pipeline.rest.RestService.noPrettyPrinter
//
//import scala.collection.mutable
//import scala.collection.JavaConverters._
//import scala.concurrent.duration._
//import scala.compat.java8.DurationConverters._
//
////import io.restassured.RestAssured.when
////import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
////import org.hamcrest.Matchers.equalTo
//
//import com.softwaremill.sttp.Response
//import com.softwaremill.sttp._
//import com.softwaremill.sttp.circe._
//import io.circe._
//import io.circe.generic.auto._
//import io.circe.syntax._
////import org.http4s._
////import org.http4s.circe._
//import cats.syntax.either._
//import io.circe.parser._
//import resource._
//
//import com.github.vtitov.kafka.pipeline.topology.DedupMessageGenerator._
//import com.github.vtitov.kafka.pipeline.json._
//
//import com.softwaremill.quicklens._
//
//class ServiceSpec
//  extends StrictLogging
//  with WordSpecLike
//  with Matchers
//  with BeforeAndAfterAll
//{
//  import com.github.vtitov.kafka.pipeline.config.{allTopics,render,globalConfig}
//  import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
//
//  // TODO read from application conf
//  implicit val config = new EmbeddedKafkaConfigImpl(
//    kafkaPort = 7031,
//    zooKeeperPort = 7030,
//    customBrokerProperties = Map(
//      "delete.topic.enable"-> "true",
//      "auto.create.topics.enable" -> "true",
//      //"log.flush.interval.messages" -> "1",
//    ),
//    customProducerProperties = Map.empty,
//    customConsumerProperties = Map("group.id" -> groupId),
//  ) with AdminOps[EmbeddedKafkaConfig]
//  //or with AdminOps[EmbeddedKafkaConfigImpl]
//
//  lazy val groupId = s"it-${UUID.randomUUID().toString}"
//
//  implicit val backend = HttpURLConnectionBackend() // TODO concider http4s backend
//
//  lazy val httpPort = globalConfig.http.port.value
//  lazy val httpEndpoint = s"http://localhost:${httpPort}"
//  lazy val httpRestEndpoint = s"${httpEndpoint}/v1/api/rest/integration"
//
//  private val ZOOKEEPER_DATA_DIR = "dataDir"
//  private val ZOOKEEPER_DATA_LOG_DIR = "dataLogDir"
//  private val KAFKA_LOG_DIR = "log.dir"
//  private val KAFKA_SETTINGS = Map(
//    ZOOKEEPER_DATA_DIR -> "zookeeper-snapshot",
//    ZOOKEEPER_DATA_LOG_DIR -> "zookeeper-logs",
//    KAFKA_LOG_DIR -> "kafkla-log-dirs")
//
//  val resources:mutable.Map[String,AutoCloseable] = mutable.LinkedHashMap()
//
//  override def beforeAll() = {
//    // start embedded kafka
//    EmbeddedKafka.start()
//    allTopics.foreach { t =>
//      logger.debug(s"createCustomTopic [$t]")
//      config.createCustomTopic(t)
//    }
//    //com.github.vtitov.kafka.pipeline.cli.execApp(Seq("all-services"))
//    // start http
//    resources += "http" -> com.github.vtitov.kafka.pipeline.cli.startRestService(null)
//    // start streams
//    resources += "streams" -> com.github.vtitov.kafka.pipeline.cli.startStreamsWithShutdownHookThread
//
//    lazy val isLoopback = globalConfig.remoteSystem.loopback.getOrElse(false)
//    if(!isLoopback) {
//      resources += "emulator" -> RemoteEmulator.run()
//    }
//    resources += "consumer" -> new Consumer(Map("group.id" -> s"it-${UUID.randomUUID.toString}"))
//
//    // check http connection
//    (1 to 42).find{i=>
//      logger.debug(s"ping attempt $i")
//      Thread.sleep((i*100).toLong)
//      val rq = sttp.get(uri"${httpEndpoint}/ping")
//      logger.debug(s"ping request [$rq]")
//      val rs = rq.send()
//      logger.debug(s"ping response [$rs]")
//      rs.isSuccess
//    } should not be None // no response
//  }
//  //override def afterAll():Unit = afterAllImpl()
//  private def afterAllImpl():Unit = {
//    // stop streams
//    // stop http
//    resources.toSeq.reverse.toMap.foreach{case (k,v) =>
//      logger.debug(s"closing $k")
//      v.close()
//    }
//    resources("http").close()
//
//    // config.deleteTopics(allTopics.toList)
//    // stop embedded kafka
//    EmbeddedKafka.stop()
//  }
//  "service" can {
//    "sub-suite" should {
//      "dump-config" in {
//        // logger.debug(s"globalConfig asJson ${globalConfig.asJson}") ??? could not find implicit value for parameter encoder
//        logger.debug(s"render globalConfig ${render(globalConfig)}")
//      }
//      "ping" in {
//        sttp.get(uri"${httpEndpoint}/ping").send().isSuccess shouldBe true
//      }
//      "send-via-http" in {
//        val filesToSendViaHttp = genMessagesToSend.head
//        note(s"post to http ${toJsonString(filesToSendViaHttp)}")
//        logger.debug(s"post to http ${toJsonString(filesToSendViaHttp)}")
//        val rq = sttp
//          .post(uri"${httpRestEndpoint}/send/files")
//          .body(toJsonString(filesToSendViaHttp))
//        //.response(asJson[List[String]])
//        logger.debug(s"send/files request [$rq]")
//        val rs = rq.send()
//        logger.debug(s"send/files response [$rs]")
//        rs.isSuccess shouldBe true
//        val asAList = parse(rs.body.right.get).right.get.as[List[String]].right.get
//        logger.debug(s"asAList: [$asAList]")
//        asAList.size shouldBe 1
//        val uidFromHttp = asAList(0)
//        note(s"uid from http: [${asAList(0)}]")
//        logger.debug(s"uid from http: [${asAList(0)}]")
//        asAList(0).length shouldBe 32
//        val filesSendViaHttp = filesToSendViaHttp.copy(uid = Some(uidFromHttp))
//
//        val filesToSenfViaKafka = genMessagesToSend.head
//        managed(new Producer) acquireAndGet { producer =>
//          logger.debug(s"producer [$producer])")
//          val json4Kafka = noPrettyPrinter.pretty(filesToSenfViaKafka.asJson)
//          note(s"send to kafka ${filesToSenfViaKafka.uid.get} -> ${json4Kafka}")
//          logger.debug(s"send ${filesToSenfViaKafka.uid.get} -> ${json4Kafka}")
//          producer.send(globalConfig.remoteSystem.topics.inTopic, filesToSenfViaKafka.uid.get, json4Kafka)
//        }
//
//        managed(new Consumer(Map("group.id" -> groupId))) acquireAndGet { consumer =>
//          logger.debug(s"consumer [$consumer]")
//          val crl:ConsumerRebalanceListener = new ConsumerRebalanceListener(){
//            override def onPartitionsRevoked (partitions: util.Collection[TopicPartition]): Unit = logger.debug(s"partitions revoked: [${partitions.asScala}]")
//            override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = logger.debug(s"partitions assigned: [${partitions.asScala}]")
//          }
//          consumer.consumer.subscribe(Seq(remoteInTopic).asJava,crl)
//
//          logger.debug(s"subscriptions: [${consumer.consumer.subscription().asScala}]")
//          val consumed: List[ConsumerRecord[String, String]] = consumer.consumer.poll(15.second.toJava).asScala.toList
//          note(s"consumed ${consumed.size} records")
//          logger.debug(s"consumed records $consumed")
//          lazy val allFiles = Seq(filesSendViaHttp, filesToSenfViaKafka)
//          consumed.length shouldEqual allFiles.length
//          consumed.zip(allFiles).foreach { case(cr,ff) =>
//            note(s"consumed record $cr")
//            logger.debug(s"consumed record $cr")
//            val nci: StatusMessage = cr.value()
//            nci.rqId.toString.replaceAll("-","") shouldEqual ff.uid.get
//            true
//          }
//        }
//        assert(true)
//      }
//    }
//  }
//}
