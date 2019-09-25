package com.github.vtitov.kafka.pipeline.kafka

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._

import Serdes._


import com.github.vtitov.kafka.pipeline.xml.Utils._

import com.github.vtitov.kafka.pipeline.config.globalConfig.remoteSystem.topics._

object LoopbakEmu extends StrictLogging {

  def buildRemoteTopology():Topology = {

    lazy val builder: StreamsBuilder = new StreamsBuilder
    lazy val remoteInStream: KStream[String, String] = builder.stream[String, String](remoteInTopic)

    remoteInStream
      .peek{case(k,v) => logger.debug(s"Loopback Emu received $k -> $v")}
      //.flatMap{case(k,v) => convertMessageOption(v).map{rs => (k,rs)}.toIterable}
      .peek{case(k,v) => logger.debug(s"Loopback Emu sending $k -> $v")}
      .to(remoteOutTopic)
    builder.build()
  }
}
