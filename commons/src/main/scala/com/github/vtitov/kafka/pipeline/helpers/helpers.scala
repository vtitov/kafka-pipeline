package com.github.vtitov.kafka.pipeline

import java.util.Properties
import scala.collection.JavaConverters._
import scala.language.implicitConversions

package object helpers {
  import scala.language.implicitConversions
  implicit def mapToProps[K <: AnyRef,V <: AnyRef](aMap: Map[K,V]): Properties = {
    val pps = new Properties
    pps.putAll(aMap.asJava)
    pps
  }
}
