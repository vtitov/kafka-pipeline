package com.github.vtitov.kafka.pipeline.jinjava

import com.hubspot.jinjava.Jinjava
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class JinjavaSpec
  extends WordSpec
  with Matchers
  with StrictLogging
{
  "jinja" can {
    "suite" should {
      "standalone-test" in {
        val jinjava = new Jinjava();
        val context: Map[String, Object]  = Map.empty + ("name" -> "Jared")
        val template = "Hello, {{ name }}";
        val renderedTemplate = jinjava.render(template, context.asJava);
        renderedTemplate shouldEqual s"""Hello, ${context("name")}"""
      }
      "api-test" ignore  {
        val context: Map[String, Object]  = Map.empty + ("name" -> "Jared", "my.name" -> "Me")
        val template = "Hello, {{ name }}. I'm {{ my.name }}.";
        val renderedTemplate = renderJinja(template, context);
        renderedTemplate shouldEqual s"""Hello, ${context("name")}"""
      }
    }
  }
}
