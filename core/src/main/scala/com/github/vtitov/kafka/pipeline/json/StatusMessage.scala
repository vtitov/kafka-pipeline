package com.github.vtitov.kafka.pipeline.json

import java.util.UUID

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.vtitov.kafka.pipeline.json.StatusMessage._


case class StatusMessage(uid: UUID, rqId: UUID, filePath: String, status: Status)

object StatusMessage {

  import scala.language.implicitConversions

//  sealed trait Status
//  case object Success extends Status
//  case object InProgress extends Status
//  case object Failed extends Status
//
//  object Status {
//    def toStatus(s: String): Option[Status] = s match {
//      case "Success" => Some(Success)
//      case "InProgress" => Some(InProgress)
//      case "Failed" => Some(Failed)
//      case _ => None
//    }
//
//    def fromStatus(x: Status): String = x match {
//      case Success => "Success"
//      case InProgress => "InProgress"
//      case Failed => "Failed"
//    }
//  }

  object Status extends Enumeration {
    type _Status = Value
    val Success: _Status = Value(1, "Success")
    val InProgress: _Status = Value(2, "InProgress")
    val Failed: _Status = Value(3, "Failed")
  }
  type
  Status = Status._Status


//  import enumeratum._
//  sealed trait Status extends EnumEntry
//  object Status extends Enum[Status] {
//    val values = findValues
//
//    case object Success     extends Status
//    case object InProgress  extends Status
//    case object Failed      extends Status
//  }

  implicit val filefromSenderCodec: JsonValueCodec[StatusMessage] = JsonCodecMaker.make[StatusMessage](CodecMakerConfig())
  implicit def fromKafkaNCI(x: StatusMessage): String = writeToString(x)
  implicit def toKafkaNCI(x: String): StatusMessage = readFromString(x)

}
