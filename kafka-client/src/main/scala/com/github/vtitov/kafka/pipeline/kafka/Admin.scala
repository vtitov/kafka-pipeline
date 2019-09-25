package com.github.vtitov.kafka.pipeline.kafka

import java.util.Properties

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, KafkaAdminClient, NewTopic}
import com.github.vtitov.kafka.pipeline.config.globalConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

class Admin(customProps:Map[String,String] = Map.empty) extends AutoCloseable with StrictLogging{
  import com.github.vtitov.kafka.pipeline.helpers._
  val zkSessionTimeoutMs = 10000
  val zkConnectionTimeoutMs = 10000
  protected val topicCreationTimeout: FiniteDuration = 2.seconds
  protected val topicDeletionTimeout: FiniteDuration = 2.seconds
  protected val adminClientCloseTimeout: FiniteDuration = 2.seconds

  val admin = {
    lazy val props = Map(AdminClientConfig.CLIENT_ID_CONFIG -> "ctl-kafka-admin-client",
        AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG -> zkSessionTimeoutMs.toString,
        AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG -> zkConnectionTimeoutMs.toString
    ) ++
    globalConfig.kafka.common ++
    globalConfig.kafka.admin ++
    customProps
    AdminClient.create(mapToProps(props))
  }


  def createCustomTopic(
                         topic: String,
                         topicConfig: Map[String, String] = Map.empty,
                         partitions: Int = 1,
                         replicationFactor: Int = 1): Unit = {
    val newTopic = new NewTopic(topic, partitions, replicationFactor.toShort)
      .configs(topicConfig.asJava)
    admin
      .createTopics(Seq(newTopic).asJava)
      .all
      .get(topicCreationTimeout.length, topicCreationTimeout.unit)
  }

  override def close(): Unit = {
    admin.close(10.seconds.toJava) // TODO make config param
  }

}
