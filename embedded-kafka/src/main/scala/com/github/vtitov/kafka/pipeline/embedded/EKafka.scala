package com.github.vtitov.kafka.pipeline.embedded

import com.typesafe.scalalogging.StrictLogging
import net.manub.embeddedkafka.ops.AdminOps
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

//import resources._
import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._

object EKafka extends StrictLogging with AdminOps[EmbeddedKafkaConfig] {

  def main(args: Array[String]): Unit = {
    logger.info(s"start EmbeddedKafka w/ args $args")
    run(kafkaPort = args(0).toInt, zkPort = args(1).toInt)
    Thread.sleep(Long.MaxValue)
  }

  def run(kafkaPort: Int, zkPort: Int) {
    logger.info(s"start EmbeddedKafka w/ kafkaPort=$kafkaPort, zkPort=$zkPort")
    implicit val ekConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      kafkaPort = kafkaPort,
      zooKeeperPort = zkPort,
      customBrokerProperties = Map(
        "delete.topic.enable" -> "true",
        "auto.create.topics.enable" -> "true",
        //"log.flush.interval.messages" -> "1",
      )
    )
    logger.info(s"start EmbeddedKafka w/ config $ekConfig")
    EmbeddedKafka.start()

    com.github.vtitov.kafka.pipeline.config.allTopics.foreach{ topic =>
      createCustomTopic(topic)
      logger.info(s"created topic $topic")
    }
    sys.ShutdownHookThread {
      EmbeddedKafka.stop()
    }
    logger.info(s"${this.getClass.getCanonicalName} started")
  }
}
