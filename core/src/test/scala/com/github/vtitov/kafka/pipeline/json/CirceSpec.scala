//package ru.sberbank.bigdata.ctl.kafka.json
//
//import io.circe.{Decoder, Encoder, Json}
////import io.circe.java8.time.TimeInstances
//import io.circe.java8.time._
//import java.time.{LocalDate, LocalDateTime}
//import java.time.format.DateTimeFormatter
//
//import com.typesafe.scalalogging.StrictLogging
//import org.scalatest.{Matchers, WordSpec}
//
//
//trait BookingEvent {
//  def json: Json
//}
//
//trait BookingInstances extends TimeInstances {
////trait BookingInstances {
//
//  val localDateTimePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
//
//  implicit final val decodeLocalDateTime: Decoder[LocalDateTime] = decodeLocalDateTime(localDateTimePattern)
//  implicit final val encodeLocalDateTime: Encoder[LocalDateTime] = encodeLocalDateTime(localDateTimePattern)
//
//  val localDatePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy")
//  implicit final val decodeLocalDate: Decoder[LocalDate] = decodeLocalDate(localDatePattern)
//  implicit final val encodeLocalDate: Encoder[LocalDate] = encodeLocalDate(localDatePattern)
//}
//
//import io.circe.syntax._
//import io.circe.generic.semiauto._
//
//case class Create(
//                   date: LocalDateTime = LocalDateTime.now
//                 ) extends BookingEvent  {
//  def json = this.asJson
//}
//
//object Create extends BookingInstances {
//  implicit val jsonEncoder: Encoder[Create] = deriveEncoder[Create]
//  implicit val jsonDecoder: Decoder[Create] = deriveDecoder[Create]
//}
//object CirceSpec {
//
//}
//class CirceSpec extends WordSpec
//  with Matchers
//  with StrictLogging
//{
//
//}
