package com.github.vtitov.kafka.pipeline.config

import java.util.UUID
import java.nio.file.{Path=>JPath}
import java.io.{File=>JFile}

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValue}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{Matchers, WordSpec}
import pureconfig.ConfigWriter
//import com.softwaremill.quicklens._

import pureconfig.generic.auto._

class ConfigSpec
  extends WordSpec
  with Matchers
  with StrictLogging
{

  "suite" can {
    "config" should {
      "write-parse-config" in {
        val debugConfig = GlobalConfig(
          remoteSystem = RemoteSystem(
            loopback = Some(true), inMemoryKeyValueStore = Some(true),
            topics = Topics(),
          ),
          messageSettings = MessageSettings(uid = UUID.fromString("733f652d-53d0-42ff-876b-ad21015c20c1")),
          kafka = Kafka(common = Map("application.id" -> "???")),
          //embeddedKafka = EmbeddedKafkaConf()
        )

        val rendered = render(debugConfig)
        //System.out.println(s"rendered config:\n${render(debugConfig)}")
        logger.debug(s"rendered debug config\n${render(debugConfig)}")
        //val parsed = pureconfig.loadConfig[GlobalConfig](ConfigFactory.parseString(rendered).resolve())
        //info(s"parsed: ${parsed}")
        //val readConfig:GlobalConfig = pureconfig.loadConfig[GlobalConfig](rendered).right.get
        val readConfig:GlobalConfig = pureconfig
          .loadConfig[GlobalConfig](ConfigFactory.parseString(rendered).resolve())
          .right.get
        logger.debug(s"readConfig: ${readConfig}")
        readConfig shouldEqual debugConfig
      }
      "load-config" in {
        val loadedConfig = pureconfig.loadConfig[GlobalConfig]
        logger.debug(s"$loadedConfig")
        loadedConfig.map{c =>
          c.remoteSystem.loopback shouldBe Some(true)
        }
        logger.info(s"rendered loaded global config\n${render(globalConfig)}")
        globalConfig shouldEqual loadedConfig.right.get
      }

    }
  }
}
