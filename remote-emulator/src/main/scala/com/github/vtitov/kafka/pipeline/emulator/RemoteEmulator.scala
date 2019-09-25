package com.github.vtitov.kafka.pipeline.emulator

import org.apache.kafka.streams.{KafkaStreams, Topology}
import com.github.vtitov.kafka.pipeline.kafka.{StreamUtils, LoopbakEmu}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.ImplicitConversions._
import com.github.vtitov.kafka.pipeline.config.globalConfig
import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._
import com.github.vtitov.kafka.pipeline.helpers._


object RemoteEmulator extends StrictLogging {
  def main(args: Array[String]): Unit = {
    run()
    Thread.sleep(scala.Long.MaxValue)
  }

  def run():KafkaStreams = {
    val props = Seq(
      globalConfig.kafka.common,
      globalConfig.kafka.producer,
      globalConfig.kafka.consumer
    ).flatten.toMap ++
    Seq("application.id" -> "remote-emulator")
    val topology: Topology = LoopbakEmu.buildRemoteTopology()
    StreamUtils.startStreamsWithShutdownHookThread(topology,props,this.getClass.getSimpleName)
  }

  def buildRemoteTopology():Topology = {
    lazy val builder: StreamsBuilder = new StreamsBuilder
    lazy val remoteInStream: KStream[String, String] = builder.stream[String, String](remoteInTopic)
    remoteInStream
      .peek{case(k,v) => logger.debug(s"remote emulator received $k -> $v")}
      //.flatMap{case(k,v) => convertMessage(v).map{rs => (k,rs)}.toIterable}
      .peek{case(k,v) => logger.debug(s"remote emulator $k -> $v")}
      .to(remoteOutTopic)
    val topology:Topology = builder.build()
    logger.info(s"${this.getClass.getCanonicalName} started")
    topology
  }
}
