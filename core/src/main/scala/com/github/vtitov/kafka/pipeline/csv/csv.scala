package com.github.vtitov.kafka.pipeline

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.typesafe.scalalogging.StrictLogging
import types._

import scala.util.Try
import scala.io.Source
import java.io.FileNotFoundException

import kantan.csv._         // All kantan.csv types.
import kantan.csv.ops._     // Enriches types with useful methods.
import kantan.csv.generic._
import os._

package object csv extends StrictLogging {


  case class CsvTable(prefix: String, base: String, suffux: String)
  def readTable(path: os.Path) = os.read(path)
    .asCsvReader[CsvTable](rfc.withHeader)
    .map { eitherRow => eitherRow.toTry.fold(e => throw e, row => row) }.toSeq
}
