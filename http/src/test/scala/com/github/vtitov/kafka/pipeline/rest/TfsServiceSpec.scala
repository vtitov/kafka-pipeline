//package ru.sberbank.bigdata.ctl.kafka.rest
//
//import java.util.UUID
//
////import cats.effect.{Effect, IO}
////import com.typesafe.scalalogging.StrictLogging
////import io.circe.{Decoder, Encoder}
////import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
////import org.http4s.HttpRoutes
////import org.http4s.dsl.io.{->, /, POST, Root}
////import org.scalatest.{Matchers, WordSpec}
////import ru.sberbank.bigdata.ctl.kafka.types.FileSeqReadyFromSender
////
////import org.http4s._
////import org.http4s.circe._
////import org.http4s.dsl.io._
////
////import cats.implicits._
////import io.circe._
////import io.circe.syntax._
////import io.circe.generic.semiauto._
////import cats.effect._
////import org.http4s._
////import org.http4s.circe._
////import org.http4s.dsl.io._
////import org.http4s.implicits._
////import cats.effect.Effect
////import com.typesafe.scalalogging.StrictLogging
////import io.circe.{Decoder, Encoder}
////import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
////import org.http4s.{HttpRoutes, Response, Status}
////import org.http4s.dsl.io.{->, /, GET, Root}
////import org.scalatest.{Matchers, WordSpec}
//
//
//
//import cats.effect._
//
//import io.circe._
//import io.circe.generic.auto._
//import io.circe.syntax._
//
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.dsl.io._
//import org.http4s.implicits._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//
//import ru.sberbank.bigdata.ctl.kafka.types.FileSeqReadyFromSender
//
//
//
//import scala.language.higherKinds
//
//
//object RestServiceSpec {
//  //import org.http4s.circe.CirceEntityCodec._
//
//  import org.http4s.circe.CirceEntityEncoder._
//  import org.http4s.circe.CirceEntityDecoder._
////  implicit val filesEncoder: Encoder[FileSeqReadyFromSender] = deriveEncoder[FileSeqReadyFromSender]
////  implicit val filesDecoder: Decoder[FileSeqReadyFromSender] = deriveDecoder[FileSeqReadyFromSender]
//
//  implicit val filesDecoder = jsonOf[IO, FileSeqReadyFromSender]
//  //implicit val userDecoder = jsonOf[IO, FileSeqReadyFromSender]
//
//  lazy val apiVersionPrefix = "v1/api"
//  lazy val restPrefix = "rest/integration"
//  lazy val restPath = Root / apiVersionPrefix / restPrefix
//
//  val restIntegrationService0 = HttpRoutes.of[IO] {
//  case rq @ POST -> restPath / "send" / "files" => {
//           val rs = Seq(
//             UUID.randomUUID.toString.replaceAll("-","")
//           )
//    for {
//      // Decode a FileSeqReadyFromSender request
//      files <- rq.as[FileSeqReadyFromSender]
//      //logger.debug(s"recived files: ${files}")
//      // Encode a hello response
//      resp <- Ok(rs.asJson)
//    } yield (resp)
//  }}.orNotFound
//
//  trait Sender[F[_]] {
//    def send(files: FileSeqReadyFromSender): F[Option[Seq[String]]]
//  }
//
////  def restIntegrationService1[F[_]](sender: Sender[F])(implicit F: Effect[F]): HttpRoutes[F] = {
////    HttpRoutes.of[IO] {
////      case rq@POST -> restPath / "send" / "files" => {
////        (
////          rq.as[FileSeqReadyFromSender].map { files =>
////            //            sender.send(files).map {
////            //              case Some(seq) => ??? //Response(status = Status.Ok).withEntity(seq.asJson)
////            //              case None => ??? //Response(status = Status.InternalServerError)
////            //            }
////            ???
////          }
////          )
////      }}}
//
//}
//class RestServiceSpec
//  extends WordSpec
//  with Matchers
//  with StrictLogging
//{
//
//  import RestServiceSpec._
//  "rest-service" can {
//
//    "???" should {
//      "???" in {}
//    }
//  }
//
//}
