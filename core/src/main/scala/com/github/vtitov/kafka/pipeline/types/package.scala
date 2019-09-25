package com.github.vtitov.kafka.pipeline

import java.util.{GregorianCalendar, UUID}



//import akka.http.scaladsl.model.Uri
import com.softwaremill.sttp.Uri
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

import scala.xml.XML


package object types {

  type IdType = Long

  def newRqUid:String = UUID.randomUUID().toString.replaceAll("-","")
  def newRqTm:XMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar)

}

package  types {

  trait Enum[A] {

    trait Value {
      self: A =>
    }

    val values: List[A]

    def withName(name: String): A = {
      val result = values.filter(v => v.toString == name)
      if (result.length == 1) result.head
      else throw new Exception(s"name error: $result")
    }

    def view(a: A): String = a.toString
  }

  case class DataReadyFromSender(
                                  files: Seq[String],
                                  uid: Option[String] = None,
                                )

}
