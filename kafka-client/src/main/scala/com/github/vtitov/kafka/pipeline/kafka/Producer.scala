package com.github.vtitov.kafka.pipeline.kafka

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import com.github.vtitov.kafka.pipeline.config.globalConfig
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

object Producer extends StrictLogging{
  def apply(customProps: Map[String, String]): Producer = new Producer(customProps)

  def metadataString(md: RecordMetadata):String = {
    if (md == null) {
      "null"
    } else {
      Map(
        "topic" -> md.topic(),
        "offset" -> md.offset(),
        "timestamp" -> md.timestamp(),
        "partition" -> md.partition(),
        "serializedKeySize" -> md.serializedKeySize(),
        "serializedValueSize" -> md.serializedValueSize(),
      ).map { case (k, v) => s"$k->$v" }
        .mkString(", ")
    }
  }
  val sendCallback:Callback = (metadata: RecordMetadata, exception: Exception) => {
    if(exception!=null) logger.warn(s"exception while sending record ${metadataString(metadata)}", exception)
    else logger.info(s"sent record metadata: ${metadataString(metadata)}")
  }

}
class Producer(customProps:Map[String,String] = Map.empty) extends AutoCloseable with StrictLogging{
  import com.github.vtitov.kafka.pipeline.helpers._
  import Producer.sendCallback

  val producer = {
    lazy val props = globalConfig.kafka.common ++
      globalConfig.kafka.producer +
      ("key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer") +     // TODO consider uuid?
      ("value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer") ++  // TODO consider Json
      customProps
    new KafkaProducer[String, String](props)
  }

  def flush(): Unit = producer.flush()
  def send(topic: String, key: String, value: String): Unit = {
    val record = new ProducerRecord(topic, key, value)
    logger.info(s"producer record: ${record.toString}")
    producer.send(record, sendCallback)
  }

  override def close(): Unit = {
    producer.close(10.seconds.toJava) // TODO make config param
  }
}
