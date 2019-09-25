package com.github.vtitov.kafka.pipeline.csv


import java.util.UUID

import com.github.javafaker.Faker
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{Matchers, WordSpec}
import os._
import com.github.vtitov.kafka.pipeline.config.{MessageSettings, RemoteSystem, GlobalConfig}

class CsvSpec
  extends WordSpec
  with Matchers
  with StrictLogging
{
  lazy val faker = new Faker()

  "csv-suite" can {
    "suite" should {
      "read-routes" in {
        implicit val config:GlobalConfig = GlobalConfig(
          messageSettings = MessageSettings(UUID.randomUUID()),
          remoteSystem = RemoteSystem(),
        )
      }
    }
  }
}
