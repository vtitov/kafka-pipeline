package com.github.vtitov.kafka.pipeline


import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

import types.DataReadyFromSender


package object json {

  // TODO consider migration to jackson: jsoniter_scala is fast but kafka uses jackson for json ser/der

  import scala.language.implicitConversions
  //implicit def filefromSenderCodec[A]: JsonValueCodec[A] = JsonCodecMaker.make[A](CodecMakerConfig())
  //implicit def toJsonString[A](x: A): String = writeToString(x)

  implicit val filefromSenderCodec: JsonValueCodec[DataReadyFromSender] = JsonCodecMaker.make[DataReadyFromSender](CodecMakerConfig())
  implicit def toJsonString(x: DataReadyFromSender): String = writeToString(x)
  implicit def fromJsonString(x: String): DataReadyFromSender = readFromString(x)
}
