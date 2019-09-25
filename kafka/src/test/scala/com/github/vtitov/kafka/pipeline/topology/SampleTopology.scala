package com.github.vtitov.kafka.pipeline.topology

import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._

class SampleTopology {

  // DefaultSerdes brings into scope implicit SerDes (mostly for primitives)
  // that will set up all Grouped, Produced, Consumed and Joined instances.
  // So all APIs below that accept Grouped, Produced, Consumed or Joined will
  // get these instances automatically
  import org.apache.kafka.streams.scala.Serdes._

  val builder = new StreamsBuilder()

  val userClicksTopic = "userClicksTopic"
  val userRegionsTopic = "userRegionsTopic"
  val outputTopic = "outputTopic"

  val userClicksStream: KStream[String, Long] = builder.stream(userClicksTopic)

  val userRegionsTable: KTable[String, String] = builder.table(userRegionsTopic)

  // The following code fragment does not have a single instance of Grouped,
  // Produced, Consumed or Joined supplied explicitly.
  // All of them are taken care of by the implicit SerDes imported by DefaultSerdes
  val clicksPerRegion: KTable[String, Long] =
  userClicksStream
    .leftJoin(userRegionsTable)((clicks, region) => (if (region == null) "UNKNOWN" else region, clicks))
    .map((_, regionWithClicks) => regionWithClicks)
    .groupByKey
    .reduce(_ + _)

  clicksPerRegion.toStream.to(outputTopic)
}
