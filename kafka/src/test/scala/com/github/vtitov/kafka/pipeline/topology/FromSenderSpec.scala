//package com.github.vtitov.kafka.pipeline.topology
//
//import java.util.UUID
//
//import akka.actor.ActorSystem
//import akka.testkit.TestProbe
//import com.github.javafaker.Faker
//import com.typesafe.scalalogging.StrictLogging
//import net.manub.embeddedkafka.Codecs._
//import net.manub.embeddedkafka.ConsumerExtensions._
//import net.manub.embeddedkafka.EmbeddedKafkaConfig
//import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
//import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
//import org.apache.kafka.streams.state.KeyValueStore
//import org.apache.kafka.streams.{KeyValue, TopologyTestDriver}
//import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
//import org.apache.kafka.streams.StreamsConfig
//import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
//import com.github.vtitov.kafka.pipeline.figlet
//import com.github.vtitov.kafka.pipeline.helpers._
//import com.github.vtitov.kafka.pipeline.json._
//import com.github.vtitov.kafka.pipeline.kafka.LoopbakEmu
//import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
//import com.github.vtitov.kafka.pipeline.config.allTopics
//import com.github.vtitov.kafka.pipeline.topology.InOutTopology.buildTopology
//import com.github.vtitov.kafka.pipeline.types._
//
//
//
//import resource._
//import scala.collection.JavaConverters._
//
//
//object MessageGenerator {
//  lazy val faker = new Faker()
//
//  def genMessagesToSend: Seq[DataReadyFromSender] = Stream.from(1) map { n =>
//    (0 until n) map { _ =>
//      faker.file().fileName(null, null, null, "/")
//    }
//  } map { fs: Seq[String] =>
//    val uid = newRqUid
//    fromJsonString(toJsonString(DataReadyFromSender(files = fs, uid = Some(uid))))
//  }
//
//  def genMessagesToSend3: Seq[DataReadyFromSender] = genMessagesToSend.take(3)
//}
//
//class FromSenderSpec
//  extends WordSpec
//    with BeforeAndAfterAll
//    with Matchers
//    with EmbeddedKafkaStreamsAllInOne
//    //with TestKitBase
//    with StrictLogging {
//
//  import DedupMessageGenerator._
//
//  implicit lazy val system = ActorSystem()
//
//  implicit lazy val config =
//    EmbeddedKafkaConfig(kafkaPort = 7000, zooKeeperPort = 7001)
//
//  lazy val testProbe = TestProbe()
//
//  "from-sender-spec" can {
//    logger.debug(figlet.duplicateOneLine(s"from-sender-spec"))
//    "sanbox-kafka-stream" should {
//      logger.debug(s"sanbox-kafka-stream")
//
//      "do-filenames" in {
//        val filesToSendMessages = genMessagesToSend3
//        logger.debug(s"files: ${filesToSendMessages}")
//        filesToSendMessages.map { fs => fs.uid.get.length shouldEqual 32 }
//        filesToSendMessages(2).files.length shouldEqual 3
//      }
//      "do-messages-to-remote" ignore {
//        val filesToSendMessages = genMessagesToSend3
//        logger.debug(s"files: ${filesToSendMessages}")
//        val cephRqs = filesToSendMessages.map { msg =>
//          val ffs = fromJsonString(msg)
//          ??? //createXml()
//        }
//        logger.debug(s"cephRqs: ${cephRqs}")
//        def saveToXml(any:Any) = ???   // FIXME
//        val xmlRqs = cephRqs.map(saveToXml(_))  // FIXME
//        logger.debug(s"xmlRqs: ${xmlRqs}")
//      }
//      "do-topology" ignore { // FIXME
//        val testProps = config.customBrokerProperties ++
//          Map(
//            StreamsConfig.APPLICATION_ID_CONFIG -> ("app-" + UUID.randomUUID().toString)
//            , StreamsConfig.BOOTSTRAP_SERVERS_CONFIG -> s"localhost:${config.kafkaPort}"
//          )
//
//        managed(new TopologyTestDriver(buildTopology(Option(testProbe.ref)), testProps)) and
//          managed(new TopologyTestDriver(LoopbakEmu.buildRemoteTopology(), testProps)) acquireAndGet { case (testDriver, remoteEmuDriver) =>
//          // populate
//          logger.debug(s"all stores: ${testDriver.getAllStateStores}")
//          lazy val kvStores = (toRemoteTable :: fromRemoteTable :: toMonitoringTable :: Nil).map { table =>
//            (table -> testDriver.getKeyValueStore(table).asInstanceOf[KeyValueStore[String, String]])
//          }.toMap
//          val storeToRemote: KeyValueStore[String, String] = kvStores(toRemoteTable)
//          Map[String, String](
//            //"1" -> "<a>1</a>"
//            //,"2" -> "<b>2</b>"
//          ).foreach { case (k, v) => storeToRemote.put(k, v) }
//          //kvStores
//
//          lazy val messagesToSend: Seq[KeyValue[String, String]] =
//            Seq(new KeyValue(null: String, """{"foo":"bar"}""")) ++
//              genMessagesToSend3.map(msg => new KeyValue(msg.uid.getOrElse(newRqUid), toJsonString(msg)))
//
//          val factory = new ConsumerRecordFactory[String, String](inTopic, new StringSerializer(), new StringSerializer());
//          testDriver.advanceWallClockTime(20L)
//          testDriver.pipeInput(factory.create(inTopic, messagesToSend.asJava))
//          testDriver.advanceWallClockTime(20L)
//
//          testDriver.readOutput(inTopicDebug, new StringDeserializer(), new StringDeserializer())
//          val realMessagesToSend = messagesToSend
//            .filter { msg =>
//              //note(s"filtering message $msg")
//              logger.debug(s"filtering message $msg")
//              Option(msg.key).map(_.length == 32).getOrElse {
//                logger.debug(s"skipping bad message $msg");
//                false
//              }
//            }
//          realMessagesToSend.map { msg =>
//            //note(s"checking message $msg")
//            logger.debug(s"checking message $msg")
//            val outputRqRecord = testDriver.readOutput(inTopicDebug, new StringDeserializer(), new StringDeserializer())
//            OutputVerifier.compareKeyValue(outputRqRecord, msg.key, msg.value)
//            outputRqRecord.key() shouldEqual msg.key
//            outputRqRecord.value() shouldEqual msg.value
//            msg
//          }
//            //          .filter{ msg =>
//            //          //note(s"filtering message $msg")
//            //          logger.debug(s"filtering message $msg")
//            //          Option(msg.key).map(_.length==32).getOrElse{logger.debug(s"skipping bad message $msg") ;false }
//            //        }
//            .map { msg =>
//              val toRemoteRecord = testDriver.readOutput(remoteInTopic, new StringDeserializer(), new StringDeserializer())
//              val (k, v) = (toRemoteRecord.key, toRemoteRecord.value)
//              k shouldEqual msg.key
//
//              val v1 = testDriver.getKeyValueStore(toRemoteTable).asInstanceOf[KeyValueStore[String, String]].get(k)
//              //note(s"toRemoteTable ($k, $v1) ")
//              logger.debug(s"toRemoteTable ($k, $v1) ")
//              v shouldEqual v1
//              testDriver.advanceWallClockTime(20L)
//            }.map { msg =>
//            val outputMonRecord = testDriver.readOutput(toMonitoringTopic, new StringDeserializer(), new StringDeserializer())
//            Option(outputMonRecord) should not be null
//            logger.debug(s"kafka mon record: $outputMonRecord")
//            val (k, v) = (outputMonRecord.key, outputMonRecord.value)
//            logger.debug(s"kafka mon record: $outputMonRecord")
//            kvStores(toRemoteTable).get(k) should not be null
//            kvStores(fromRemoteTable).get(k) should not be null
//            kvStores(toMonitoringTable).get(k) should not be null
//            (k, v)
//          }.length should equal(realMessagesToSend.length)
//
//          val storeSizes = kvStores.keys.map { store =>
//            logger.debug(s"values in $store")
//            kvStores(store).all().asScala.foreach { kv =>
//              logger.debug(s"  ${kv.key} => ${kv.value}")
//              //kv.key → kv.value
//            }
//            store -> kvStores(store).all().asScala.toList.length
//          }.toMap.values.toList.sorted.groupBy(k => k).size
//          storeSizes shouldBe 1
//
//          val toMonitoringTableLength = kvStores(toMonitoringTable).all().asScala.toSeq.length
//          logger.debug(s"kafka mon records: $toMonitoringTableLength")
//          kvStores(toMonitoringTable).all().asScala.toSeq.length shouldEqual realMessagesToSend.length
//          logger.debug(s"kafka mon approx records: ${kvStores(toMonitoringTable).approximateNumEntries}")
//
//          kvStores(toMonitoringTable).all.asScala.toList.map { kv =>
//            logger.debug(s"table mon record: $kv")
//            val (k, v) = (kv.key, kv.value)
//            logger.debug(s"table mon record: $k -> $v")
//            val stored = storeToRemote.get(k)
//            logger.debug(s"stored record: $k -> $stored")
//            // TODO check monitoring vs input ids
//          }
//
//          // TODO check store
//          //testDriver.getAllStateStores.asScala.foreach{case (name,store) => note(s"store $name -> $store")}
//          //(toRemoteTable::fromremoteTable::Nil) foreach  {name =>
//          //  val store = testDriver.getKeyValueStore(name).asInstanceOf[KeyValueStore[String,String]]
//          //  note(s"store $name -> $store")
//          //  store.all.asScala.foreach{kv =>
//          //    note(s"kv from ${name}: (${kv.key}, ${kv.value}) ")
//          //  }
//          //}
//        }
//      }
//
//      // TODO remove, or move to it suite
//      "do-something" ignore {
//        logger.debug(s"doing something")
//        runStreams(allTopics, buildTopology(Option(testProbe.ref))) {
//
//          lazy val messagesToSend = genMessagesToSend3
//          messagesToSend.foreach { msg => publishToKafka(inTopic, msg.uid.getOrElse(newRqUid), toJsonString(msg)) }
//          managed(newConsumer[String, String]) acquireAndGet { consumer =>
//            //consumer.consumeLazily[(String, String)](outTopic) should be(Stream(("hello", "world"), ("foo", "bar"), ("foz", "baz")))
//            consumer.consumeLazily[(String, String)](inTopicDebug) should be(messagesToSend)
//            //consumer.consumeLazily[(String, String)](outTopic2)should be(Stream(("hello", "worldworld"), ("foo", "barbar"), ("foz", "bazbaz")))
//            //consumer.consumeLazily[(String, String)](outTopic2).length should be(messagesToSend.length)
//            logger.debug(s"consumed")
//          }
//
//          lazy val teakeOnly = 4
//          lazy val received = testProbe.receiveN(teakeOnly)
//          received should have length messagesToSend.length.asInstanceOf[Long]
//
//          import com.ibm.icu.text.RuleBasedNumberFormat
//          lazy val nf = new RuleBasedNumberFormat(java.util.Locale.UK, RuleBasedNumberFormat.SPELLOUT)
//          received.zipAll(messagesToSend.take(teakeOnly), null, null)
//            .zipWithIndex.foreach { case ((s, r), idx) =>
//            lazy val ordIdx = nf.format(idx.asInstanceOf[Long], "%spellout-ordinal")
//            (ordIdx, s) shouldEqual ((ordIdx, r))
//          }
//          logger.debug(s"done something")
//        }
//      }
//    }
//  }
//}
