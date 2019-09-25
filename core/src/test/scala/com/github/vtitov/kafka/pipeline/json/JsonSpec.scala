package com.github.vtitov.kafka.pipeline.json

import java.util.UUID.randomUUID

import com.github.javafaker.Faker
import com.softwaremill.quicklens._
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{Matchers, WordSpec}
import com.github.vtitov.kafka.pipeline.types.DataReadyFromSender
import com.github.vtitov.kafka.pipeline.faker._
class JsonSpec
  extends WordSpec
  with Matchers
  with StrictLogging
{
  val faker = new Faker()
  "json-suite" can {
    "send-files-json" should {
      "write-files-explicitly" in {
        //import com.github.plokhotnyuk.jsoniter_scala.macros._
        import com.github.plokhotnyuk.jsoniter_scala.core._

        val fs = DataReadyFromSender(files = Seq("foo.txt"))
        val json = writeToString(fs).replace("foo","bar")
        logger.debug(s"json is ${json}")
        val fs2 = readFromString(json)
        fs2 shouldEqual fs.modify(_.files.at(0)).setTo("bar.txt")
      }
      "write-files-implicitly" in {
        val fs = DataReadyFromSender(files = Seq("foo.txt"))
        val json:String = fs.replace("foo","bar")
        logger.debug(s"json is ${json}")
        val fs2:DataReadyFromSender = json
        fs2 shouldEqual fs.modify(_.files.at(0)).setTo("bar.txt")
      }
    }
    "kafka-to-json" should {
      import StatusMessage._
      "write-msg-implicitly" in {
        val msg1::msg2::Nil =
          (1 to 2).map(_=> StatusMessage(randomUUID(),randomUUID(),fileName(),StatusMessage.Status.Success)).toList
        val json1:String = msg1
        logger.debug(s"message1: $msg1")
        logger.debug(s"json1: $json1")
        val json2 =
          json1
            .replaceAll(msg1.uid.toString,msg2.uid.toString)
            .replaceAll(msg1.rqId.toString,msg2.rqId.toString)
            .replaceAll(msg1.filePath.toString,msg2.filePath.toString)
        val msg3:StatusMessage = json2
        val json3:String = msg3
        logger.debug(s"message3: $msg3")
        logger.debug(s"json3: $json3")
        msg2 shouldEqual msg3
      }
    }
  }
}
