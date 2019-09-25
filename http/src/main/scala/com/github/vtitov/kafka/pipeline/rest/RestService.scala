package com.github.vtitov.kafka.pipeline.rest

import java.util.UUID

import cats.effect._
import com.typesafe.scalalogging.StrictLogging
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
//import org.http4s.client.middleware.Logger
import org.http4s.dsl.io._
import org.http4s.implicits._
import com.github.vtitov.kafka.pipeline.json._
import com.github.vtitov.kafka.pipeline.kafka.Producer
import com.github.vtitov.kafka.pipeline.types.DataReadyFromSender

import scala.concurrent.ExecutionContext.Implicits.global

object RestService extends StrictLogging {

  // Needed by `BlazeServerBuilder`. Provided by `IOApp`.
  implicit lazy val cs: ContextShift[IO] = IO.contextShift(global)
  implicit lazy val timer: Timer[IO] = IO.timer(global)

  implicit val filesDecoder = jsonOf[IO, DataReadyFromSender]

  lazy val apiVersionPrefix = "v1/api"
  lazy val apiPrefix = "rest/integration"
  lazy val apiPath = Root / apiVersionPrefix // / apiPrefix

  lazy val noPrettyPrinter = Printer(
    dropNullValues = true,
    indent = ""
  )
}

class RestService extends StrictLogging with AutoCloseable {
  import RestService._
  import com.github.vtitov.kafka.pipeline.config.globalConfig

  import org.http4s.server.blaze._

  val producer = new Producer

  def prosessData(rq: Request[IO]) = {
    rq.as[DataReadyFromSender].flatMap { files =>
      logger.info(s"processing data: ${files.asJson}")
      val uid = files.uid.getOrElse(UUID.randomUUID.toString.replaceAll("-", ""))
      //val stringToSend1 = noPrettyPrinter.pretty(files.asJson)
      //logger.debug(s"files to kafka: ${stringToSend1}")
      val stringToSend = toJsonString(files)
      logger.debug(s"data to kafka: ${stringToSend}")
      producer.send(globalConfig.remoteSystem.topics.inTopic, uid, stringToSend)
      val rs = Seq(uid)
      logger.info(s"responding with uid: ${rs.asJson}")
      Ok(rs.asJson.noSpaces)
    }
  }

  lazy val restService = restRoutes // <+> add middleware
  lazy val restRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "ping" => Ok("pong")
    case rq@POST -> Root / "ping" => Ok(rq.as[Json])
    case GET -> Root / "v1" / "api" / "info" => Ok(BuildInfo.asJson)
    case rq@POST -> Root / "v1" / "api" / "send" / " files" => prosessData(rq)
    case rq@POST -> apiPath / "send" / "files" => prosessData(rq)
  }.orNotFound

  def server = BlazeServerBuilder[IO]
    .bindHttp(globalConfig.http.port.value)
    .withHttpApp(restService)
    .resource
  def fiber() = server.use(_ => IO.never).start.unsafeRunSync()

  override def close(): Unit = {
    producer.close()
  }
}
