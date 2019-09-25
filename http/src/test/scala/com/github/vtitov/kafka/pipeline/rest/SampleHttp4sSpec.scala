package com.github.vtitov.kafka.pipeline.rest


import cats.effect.{Effect, _}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import io.circe.{Encoder, _}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io.{->, /, GET, Root}
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Response, Status, _}
import org.scalatest.{Matchers, WordSpec}

import scala.language.higherKinds


object SampleHttp4sSpec {
  final case class User(name: String, age: Int)

}

class SampleHttp4sSpec
  extends WordSpec
  with Matchers
  with StrictLogging
{
  import SampleHttp4sSpec._
  implicit val UserEncoder: Encoder[User] = deriveEncoder[User]
  trait UserRepo[F[_]] {
    def find(userId: String): F[Option[User]]
  }
  def service[F[_]](repo: UserRepo[F])(
    implicit F: Effect[F]
  ): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "user" / id =>
      repo.find(id).map {
        case Some(user) => Response(status = Status.Ok).withEntity(user.asJson)
        case None       => Response(status = Status.NotFound)
      }
  }

  def check[A](actual:        IO[Response[IO]],
               expectedStatus: Status,
               expectedBody:   Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Boolean =  {
    val actualResp         = actual.unsafeRunSync
    val statusCheck        = actualResp.status == expectedStatus
    val bodyCheck          = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty)( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync == expected
    )
    statusCheck && bodyCheck
  }


  "sample-http4s" can {
    "user" should {
      "return-success" in {
        val success: UserRepo[IO] = new UserRepo[IO] {
          def find(id: String): IO[Option[User]] = IO.pure(Some(User("johndoe", 42)))
        }
        // success: UserRepo[cats.effect.IO] = $anon$1@6b4d5b9b

        val response: IO[Response[IO]] = service[IO](success).orNotFound.run(
          Request(method = Method.GET, uri = uri"/user/not-used" )
        )
        // response: cats.effect.IO[org.http4s.Response[cats.effect.IO]] = <function1>

        val expectedJson = Json.obj(
          ("name", Json.fromString("johndoe")),
          ("age",  Json.fromBigInt(42))
        )
        // expectedJson: io.circe.Json =
        // {
        //   "name" : "johndoe",
        //   "age" : 42
        // }

        check[Json](response, Status.Ok, Some(expectedJson)) shouldBe true
        // res1: Boolean = true
      }
      "found-none" in {
        val foundNone: UserRepo[IO] = new UserRepo[IO] {
          def find(id: String): IO[Option[User]] = IO.pure(None)
        }
        // foundNone: UserRepo[cats.effect.IO] = $anon$1@22dbbe53

        val response: IO[Response[IO]] = service[IO](foundNone).orNotFound.run(
          Request(method = Method.GET, uri = uri"/user/not-used" )
        )
        // response: cats.effect.IO[org.http4s.Response[cats.effect.IO]] = <function1>

        check[Json](response, Status.NotFound, None) shouldBe true
        // res2: Boolean = true
      }
      "does-not-matter" in {
        val doesNotMatter: UserRepo[IO] = new UserRepo[IO] {
          def find(id: String): IO[Option[User]] = IO.raiseError(new RuntimeException("Should not get called!"))
        }
        // doesNotMatter: UserRepo[cats.effect.IO] = $anon$1@236ea5a1

        val response: IO[Response[IO]] = service[IO](doesNotMatter).orNotFound.run(
          Request(method = Method.GET, uri = uri"/not-a-matching-path" )
        )
        // response: cats.effect.IO[org.http4s.Response[cats.effect.IO]] = <function1>

        check[String](response, Status.NotFound, Some("Not found")) shouldBe true
        // res3: Boolean = true
      }
    }
  }
}
