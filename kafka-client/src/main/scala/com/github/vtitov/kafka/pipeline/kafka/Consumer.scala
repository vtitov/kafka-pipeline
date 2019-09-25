package com.github.vtitov.kafka.pipeline.kafka

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import com.github.vtitov.kafka.pipeline.config.globalConfig

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

class Consumer(customProps:Map[String,String] = Map.empty) extends AutoCloseable with StrictLogging{
  import com.github.vtitov.kafka.pipeline.helpers._
  val consumer = {
    lazy val props = globalConfig.kafka.common ++
      globalConfig.kafka.consumer +
      ("key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer") +     // TODO consider uuid?
      ("value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer") ++  // TODO consider Json
      customProps
    new KafkaConsumer[String, String](props)
  }

  override def close(): Unit = {
    consumer.close(10.seconds.toJava) // TODO make config param
  }

}
