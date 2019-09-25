package com.github.vtitov.kafka.pipeline.kafka

import java.util.Properties

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.streams.{KafkaStreams, Topology}
import com.github.vtitov.kafka.pipeline.figlet

import scala.concurrent.duration._
import scala.compat.java8.DurationConverters._

object StreamUtils extends StrictLogging {

  def startStreamsWithShutdownHookThread(topology:Topology, props: Properties, name: String = "unknown"):KafkaStreams = {
    val streams = startStreams(topology, props, name)
    sys.ShutdownHookThread {
      streams.close(10.seconds.toJava) //TODO make config parameter
    }
    streams
  }

  def startStreams(topology:Topology, props: Properties, name: String):KafkaStreams = {
    logger.info(s"run streams with topology [${topology.describe().toString}]")
    val streams = new KafkaStreams(topology, props)
    streams.start()
    logger.info(figlet.duplicateOneLine(s"$name streams"))
    streams
  }
}
