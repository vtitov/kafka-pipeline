package com.github.vtitov.kafka.pipeline

import com.hubspot.jinjava.Jinjava
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

package object jinjava extends StrictLogging{

  lazy val jinjava = new Jinjava()

  def renderJinja(template:String, context: Map[String, Object]): String = {
    logger.debug(s"rendering [${template}] with [${context}]")
    val renderedTemplate = jinjava.render(template, context.asJava);
    logger.debug(s"rendered result [${renderedTemplate}]")
    renderedTemplate
  }
}
