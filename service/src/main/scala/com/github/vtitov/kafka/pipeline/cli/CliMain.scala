package com.github.vtitov.kafka.pipeline.cli

import com.typesafe.scalalogging.StrictLogging

object CliMain extends StrictLogging {

  def main(args: Array[String]): Unit = {
    run(args)
    Thread.sleep(scala.Long.MaxValue)
  }

  def run(args: Array[String]):Unit = {
    logger.info(s"cli args: $args")
    execApp(args)
    logger.info(s"${this.getClass.getCanonicalName} started")
  }
}
