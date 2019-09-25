package com.github.vtitov.kafka.pipeline.unused

import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConverters._

/*
  Sample main for revolver
 */
object Main extends App with StrictLogging {
  logger.info(s"starting in ${os.pwd}")
  sys.ShutdownHookThread {
    logger.info("stopping...")
  }

  logger.info("with system properties")
  System.getProperties.asScala.map{case(k,v) =>   logger.info(s"$k -> $v")}

  //val libpath = System.getProperty("java.library.path") +
  //  System.getProperty("path.separator") +
  //  "c:/Windows/System32"
  //System.loadLibrary("kernel32")

  logger.info("waiting...")
  Thread.sleep(Long.MaxValue)
}
