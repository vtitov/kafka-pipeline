package com.github.vtitov.kafka.pipeline

//import pureconfig.generic.auto._

import java.util.UUID
import java.nio.file.{Path=>JPath}

import com.typesafe.config.{ConfigRenderOptions, ConfigValue}
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigWriter
import pureconfig.generic.auto._

//import scala.collection.JavaConverters._


package object config extends StrictLogging {
  lazy val globalConfig:GlobalConfig = {
    logger.debug(s"loading config") // -Dconfig.file=path/to/config-file
    //logger.debug(s"""logback.configurationFile is ${System.getProperty("logback.configurationFile")}"""")
    logger.debug(s"""-Dconfig.file=${System.getProperty("config.file")}"""")
    val c = pureconfig.loadConfig[GlobalConfig]
      .fold(
        left => {
          logger.error(s"cannot load config: ${left.toList.toString}")
          throw new IllegalStateException(left.toList.toString)
        },
        right => right
      )
    logger.debug(s"effective config: ${render(c)}")
    logger.debug(s"config object: ${c}")
    c
  }

  lazy val allTopics:Seq[String] =
    globalConfig.remoteSystem.topics.productIterator.map(_.toString).toSeq ++
      //globalConfig.remoteSystem.shards.flatMap{case (t,n) => (0 until n).map(idx=>s"$t.$idx")}
      globalConfig.remoteSystem.shardedTopics.prefixes.flatMap{prefix:String =>
        (0 until globalConfig.remoteSystem.shardedTopics.shardsNumber.value).map(idx=>s"$prefix.$idx")
      }


  def render: GlobalConfig => String = globalConfigWriter(_).render(configRenderOptions)
  def globalConfigWriter:GlobalConfig => ConfigValue = ConfigWriter[GlobalConfig].to
  lazy val configRenderOptions:ConfigRenderOptions =  ConfigRenderOptions.defaults()
    .setOriginComments(false)
    .setComments(false)
    .setFormatted(true)
    .setJson(false)
}

package config {

  final case class Port(value: Int) extends AnyVal
  final case class ShardsNumber(value: Int) extends AnyVal

  final case class Topics
  (
    inTopic:String             = "LOCAL.IN",
    inTopicDebug:String        = "LOCAL.IN.DEBUG", // FIXME remove

    duplicatesSink:String      = "LOCAL.TO.REMOTE.SINK",
    duplicatesTable:String     = "DUPLICATES.TABLE",

    remoteInTopic:String       = "REMOTE.IN.TOPIC",
    toRemoteTable:String       = "LOCAL.TO.REMOTE.TABLE",

    remoteOutTopic:String      = "REMOTE.OUT.TOPIC",
//    outTopic:String            = "LOCAL.OUT",
//    toMonitoringTopic:String   = "LOCAL.TO.MON",
//    fromRemoteTable:String     = "LOCAL.FROM.REMOTE.TABLE",
//    toMonitoringTable:String   = "LOCAL.TO.MON.TABLE",
//    toRemoteSink:String        = "LOCAL.TO.REMOTE.SINK",
//    fromRemoteSink:String      = "LOCAL.FROM.REMOTE.SINK",
//    //toMonitoringSink:String   = "LOCAL.TO.MON.SINK",
  )

  object RemoteSystem {
    def defaultShardes: Map[String, Int] = Map(
      "DUPLICATES.TABLE" -> 4,
      "LOCAL.TO.REMOTE.TABLE" -> 4,
    )
  }
  final case class Shards
  (
    shardsNumber: ShardsNumber = ShardsNumber(4),
    prefixes: Seq[String] = Seq("DUPLICATES.TABLE", "LOCAL.TO.REMOTE.TABLE"),
  )

  final case class RemoteSystem
  (
    loopback: Option[Boolean] = None,
    inMemoryKeyValueStore: Option[Boolean] = None,
    topics: Topics = Topics(),
    shardedTopics:Shards = Shards(),
    shards: Map[String, Int] = RemoteSystem.defaultShardes
  ) {
  }


  final case class Kafka
  (
    common:   Map[String, String] = Map(), // Map("application.id" -> null),
    producer: Map[String, String] = Map(),
    consumer: Map[String, String] = Map(),
    admin: Map[String, String] = Map(),
    streams: Map[String, String] = Map(
      "num.stream.threads" -> s"16",

    ),
    query: Map[String, String] = Map(),
    embeddedKafka: EmbeddedKafkaConf = EmbeddedKafkaConf(),
  )
  {
    lazy val allSettings:Map[String, String] = common ++ producer ++ consumer
    lazy val validate:Unit = { // TODO rewrite with Validation
      Seq("application.id","bootstrap.servers") foreach { k =>
        require(allSettings.get(k).nonEmpty, s"no [${k}] specified")
      }
    }
  }

  final case class EmbeddedKafkaConf
  (
    kafkaPort: Port = Port(7021),
    zookeeperPort: Port = Port(7020)
  )
  final case class HttpConf
  (
    port: Port = Port(8089),
  )

  final case class MessageSettings(
                        uid: UUID,
                      )

  final case class GlobalConfig(
                                 messageSettings: MessageSettings,
                                 remoteSystem: RemoteSystem = RemoteSystem(),
                                 kafka: Kafka = Kafka(),
                                 http: HttpConf = HttpConf(),
                            )

}
