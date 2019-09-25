package com.github.vtitov.kafka.pipeline.xml

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging


import scala.xml.{Elem, XML}


object Utils extends StrictLogging {


  def rqUidToUuid(rqUid: String): UUID = {
    UUID.
      fromString(s"${rqUid.substring(0,8)}-${rqUid.substring(8,12)}-${rqUid.substring(12,16)}-${rqUid.substring(16,20)}-${rqUid.substring(20)}")
  }

  //def loadXmlFromString= XML.loadString _
  def rqUidOption: Elem => Option[String] = {elem => (elem \ "uid").headOption.map(_.text)}
}
