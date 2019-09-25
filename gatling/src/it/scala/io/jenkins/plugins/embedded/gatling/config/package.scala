package io.jenkins.plugins.ctl.remote.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.{Duration, FiniteDuration, NANOSECONDS}
import scala.compat.java8.DurationConverters._


trait GatlingSimulationConfigLike {
  //def configName: String
  def configName: String
  def config: Config
}


trait GatlingSimulationConfig extends GatlingSimulationConfigLike with StrictLogging {
  override val configName: String = System.getProperty("testConfig", "baseline")
  override val config: Config = {
    val defaultFallback = "default"
    val referenceFallback = "reference.conf"
    logger.info(s"loading ${configName} with fallbacks ${defaultFallback} andThen ${referenceFallback} ")
    ConfigFactory.load(configName)
      .withFallback(ConfigFactory.load(defaultFallback))
      .withFallback(ConfigFactory.load(referenceFallback))
  }
}


trait GatlingRemoteConfigLike {
  def bootstrapServers: String
  def inputDirectory: String
  def destination: String
  def scenarioId: String
  def spName: String
  def systemId: String
  def remoteOutTopic: String
  //def duration: FiniteDuration
  //def pause: Duration
  //def rate: Double
}


trait GatlingSimulationSettings extends StrictLogging {
  this: GatlingSimulationConfigLike =>

  def getCurrentDirectory = new File("").getAbsolutePath
  def userDataDirectory = getCurrentDirectory + "/src/gatling/resources/data"

  // basic test setup
  val durationSeconds = config.getInt("performance.durationSeconds")
  val rampUpSeconds = config.getInt("performance.rampUpSeconds")
  val rampDownSeconds = config.getInt("performance.rampDownSeconds")
  val authentication = config.getString("performance.authorizationHeader")
  val acceptHeader = config.getString("performance.acceptType")
  val contentTypeHeader = config.getString("performance.contentType")
  val rateMultiplier = config.getDouble("performance.rateMultiplier")
  val instanceMultiplier = config.getDouble("performance.instanceMultiplier")

  // global assertion data
  val globalResponseTimeMinLTE = config.getInt("performance.global.assertions.responseTime.min.lte")
  val globalResponseTimeMinGTE = config.getInt("performance.global.assertions.responseTime.min.gte")
  val globalResponseTimeMaxLTE = config.getInt("performance.global.assertions.responseTime.max.lte")
  val globalResponseTimeMaxGTE = config.getInt("performance.global.assertions.responseTime.max.gte")
  val globalResponseTimeMeanLTE = config.getInt("performance.global.assertions.responseTime.mean.lte")
  val globalResponseTimeMeanGTE = config.getInt("performance.global.assertions.responseTime.mean.gte")
  val globalResponseTimeFailedRequestsPercentLTE = config.getDouble("performance.global.assertions.failedRequests.percent.lte")
  val globalResponseTimeFailedRequestsPercentGTE = config.getDouble("performance.global.assertions.failedRequests.percent.gte")
  val globalResponseTimeSuccessfulRequestsPercentLTE = config.getDouble("performance.global.assertions.successfulRequests.percent.lte")
  val globalResponseTimeSuccessfulRequestsPercentGTE = config.getDouble("performance.global.assertions.successfulRequests.percent.gte")

  // Setup all the operations per second for the test to ultimately be generated from configs
  val filePerSecond = config.getDouble("performance.operationsPerSecond.file") * rateMultiplier * instanceMultiplier
  val filesPerSecond = config.getDouble("performance.operationsPerSecond.files") * rateMultiplier * instanceMultiplier
  val fileInfoFromRemotePerSecond = config.getDouble("performance.operationsPerSecond.fileInfoFromRemote") * rateMultiplier * instanceMultiplier
}


trait GatlingRemoteSystemConfig extends GatlingRemoteConfigLike {
  this: GatlingSimulationConfigLike =>
  override lazy val bootstrapServers = config.getString("kafka.bootstrap.servers") // e.g. "localhost:9092"

  override lazy val inputDirectory = config.getString("remote.inputDirectory")
  override lazy val destination = config.getString("remote.destination")
  override lazy val scenarioId = config.getString("remote.scenarioId")
  override lazy val spName = config.getString("remote.spName")
  override lazy val systemId = config.getString("remote.systemId")
  override lazy val remoteOutTopic = config.getString("remote.remoteOutTopic")

  //override lazy val duration = config.getDuration("gatling.duration").toScala
  //override lazy val pause = config.getDuration("gatling.pause").toScala
  //override lazy val rate = config.getDouble("gatling.rate")
}
