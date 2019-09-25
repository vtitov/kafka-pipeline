package io.jenkins.plugins.gatling


import java.io.File
import java.util.{GregorianCalendar, UUID}
import javax.xml.datatype.DatatypeFactory

import com.github.mnogu.gatling.kafka.Predef._
import io.gatling.core.Predef._
import io.gatling.core.feeder.{Feeder, FeederBuilder}
import org.apache.kafka.clients.producer.ProducerConfig

import scala.collection.immutable.HashMap
import scala.concurrent.duration._

object DirectoryFeederBuilder extends FeederBuilder {

  def apply:Feeder[Any] = new DirectoryFeeder(
  ).iterator
}


class DirectoryFeeder
  extends config.GatlingRemoteSystemConfig
  with config.GatlingSimulationConfig
{
  def iterator:Feeder[Any] =
    getListOfFiles(inputDirectory)
      .map{f => new HashMap[String,Any]() +
        ("file" -> f) + ("xml" -> createSendFileInfo(Seq(f)))
      }
      .iterator


  def getListOfFiles(dir: String):Seq[File] =  {
    new File(dir).listFiles.filter(_.isFile)
      //.map(_.getPath)
      .toList
  }

  def createSendFileInfo(files: Seq[File]): SendFileInfoNf_Type = ???
}

class KafkaSimulation
  extends Simulation
    with config.GatlingSimulationConfig
    with config.GatlingRemoteSystemConfig
    with config.GatlingSimulationSettings
{

  val kafkaConf = kafka
    // Kafka topic name
    .topic(remoteOutTopic)
    // Kafka producer configs
    .properties(
    Map(
      ProducerConfig.ACKS_CONFIG -> "1",
      // list of Kafka broker hostname and port pairs
      //ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers,

      // in most cases, StringSerializer or ByteArraySerializer
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer"))

  //val scnSingle = scenario("From Remote Kafka Test")
  //  .exec(DirectoryFeederBuilder
  //    kafka("request")
  //      // message to send
  //      .send[String]("foo"))

  //val scnCsv = scenario("Kafka Test")
  //  // You can also use file based feeder
  //  .feed(csv("test.csv").random)
  //  .exec(kafka("request").send[String]("${foo}"))



  //val _duration:FiniteDuration = FiniteDuration(durationSeconds, java.util.concurrent.TimeUnit.SECONDS)

  val scnFileInfoFromRemote = scenario("REMOTE.OUT Test")
    .feed(DirectoryFeederBuilder)
    .exec(kafka("SendFileInfo").send[String]("${xml}"))
    //.exec(???)

  //val scnFileInfoFromRemoteConstantBuilder = scnFileInfoFromRemote.inject(
  //  constantUsersPerSec(fileInfoFromRemotePerSecond) during durationSeconds
  //)

  val scnFileInfoFromRemoteBuilder = scnFileInfoFromRemote.inject(
    rampUsersPerSec(1) to(fileInfoFromRemotePerSecond) during(rampUpSeconds),
    constantUsersPerSec(fileInfoFromRemotePerSecond) during(durationSeconds),
    rampUsersPerSec(fileInfoFromRemotePerSecond) to(1) during(rampDownSeconds)
  )

  val scnFileStatusToRemote = scenario("REMOTE.IN Test")
      .forever(
        pace(1 second)
          .exec(
            //pause(1 second, 4 seconds) // Will be run every 5 seconds, irrespective of what pause time is used
            // TODO add kafka reads and calcalate time put answer to hash make scnFileInfoFromRemote wait with answer in hash (or use kafka table)
          )
      )
//  val scnFileStatusToRemoteBuilder = scnFileInfoFromRemote.inject(
//    rampUsers(1) during(100500)
//  )


  setUp(List(scnFileInfoFromRemoteBuilder/*, scnFileStatusToRemoteBuilder*/))
    .protocols(kafkaConf)
    .assertions() // TODO receive file status messages
}
