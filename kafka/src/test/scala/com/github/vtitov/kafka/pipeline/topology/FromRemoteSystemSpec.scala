//package com.github.vtitov.kafka.pipeline.topology
//
//import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
//import akka.testkit.TestProbe
//import com.typesafe.scalalogging.StrictLogging
//import net.manub.embeddedkafka.Codecs._
//import net.manub.embeddedkafka.ConsumerExtensions._
//import net.manub.embeddedkafka.EmbeddedKafkaConfig
//import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
//import org.apache.kafka.common.serialization.{Serde, Serdes}
//import org.apache.kafka.streams.scala.StreamsBuilder
//import org.apache.kafka.streams.scala.kstream.{Consumed, KStream}
//import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
//
//import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
//
//object Printer {
//  def props: Props = Props[Printer]
//  final case class Message(message: String)
//}
//
//class Printer extends Actor with ActorLogging {
//  import Printer._
//
//  def receive = {
//    case Message(message) =>
//      log.info(s"Greeting received (from ${sender()}): $message")
//      ???
//  }
//}
//
//class FromRemoteSystemSpec
//  extends WordSpec
//    with BeforeAndAfterAll
//    with Matchers
//    with EmbeddedKafkaStreamsAllInOne
//    //with TestKitBase
//    with StrictLogging
//{
//
//  implicit val system = ActorSystem()
//
//  //override lazy val system = ActorSystem()
//  //val printer: ActorRef = system.actorOf(Printer.props, "printerActor")
//
//  implicit val config =
//  EmbeddedKafkaConfig(kafkaPort = 7000, zooKeeperPort = 7001) // FIXME move to config
//  //EmbeddedKafkaConfig(kafkaPort = 0, zooKeeperPort = 0)
//
//
//
//  val stringSerde: Serde[String] = Serdes.String()
//
//  val testProbe = TestProbe()
//
//  //override def beforeAll() = { EmbeddedKafka.start()}
//  //override def afterAll() = { EmbeddedKafka.stop()}
//
//  "kafka-pipeline-stream" ignore  /*should*/ { // FIXME
//    logger.debug(s"kafka-pipeline-stream")
//
//    import net.manub.embeddedkafka.Codecs.stringKeyValueCrDecoder
//
//    val stringSerde: Serde[String] = Serdes.String
//    implicit val consumed = Consumed.`with`(stringSerde, stringSerde)
//    //implicit val produced = Produced.`with`(stringSerde, stringSerde)
//
//    "do-something" in {
//      logger.debug(s"doing something")
//      val builder: StreamsBuilder = new StreamsBuilder
//      val textLines: KStream[String, String] = builder.stream[String, String](inTopic)
//      textLines.foreach{case (k,v) =>
//        note(s"received: $k -> $v")
//        logger.info(s"received message: $k -> $v")
//        //printer.tell((k,v),null)
//        //printer ! ((k,v))
//        testProbe.ref ! ((k,v))
//        logger.info(s"sent greeting: $k -> $v")
//      }
//      //textLines.to(outTopic)
//
//      //withRunningKafkaOnFoundPort(config) { actualConfig =>
//      //  note(s"actual config $actualConfig")
//      runStreams(Seq(inTopic, outTopic), builder.build()) {
//        //note(s"actual config $actualConfig")
//
//        //lazy val topology = builder.build()
//
//        val messagesToSend = Seq(
//          ("hello", "world")
//          ,("foo", "bar")
//          ,("foz", "baz")
//        )
//        messagesToSend.foreach { case (k, v) =>
//          publishToKafka(inTopic, k, v)
//        }
//        //publishToKafka(inTopic, "hello", "world")
//        //publishToKafka(inTopic, "foo", "bar")
//
//        val consumer = newConsumer[String, String]()
//        consumer.consumeLazily[(String, String)](outTopic) should be(Stream())
//        logger.debug(s"consumed")
//        consumer.close()
//
//        val received = testProbe.receiveN(2)
//        received should have length messagesToSend.length.asInstanceOf[Long]
//
//        //lazy val pf = new PluralFormat
//        import com.ibm.icu.text.RuleBasedNumberFormat
//        val nf = new RuleBasedNumberFormat(java.util.Locale.UK, RuleBasedNumberFormat.SPELLOUT)
//        received.zipAll(messagesToSend.take(2), null, null)
//        .zipWithIndex.foreach{case((s,r),idx) =>
//          val ordIdx = nf.format(idx.asInstanceOf[Long], "%spellout-ordinal")
//          (ordIdx,s) shouldEqual ((ordIdx,r))
//        }
//        logger.debug(s"done something")
//
//      }
//    }
//  }
//}
