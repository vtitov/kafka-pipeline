import sbt._

object Dependencies {
  // Versions

  lazy val logbackVersion = "1.2.3"
  lazy val slf4jVersion = "1.7.28"
  lazy val airframeVersion = "19.9.3"

  lazy val akkaVersion = "2.5.23" // "akka-stream-kafka-testkit" % "1.0-M1"
  //lazy val akkaVersion = "2.5.24" // "akka-stream-kafka-testkit" % "1.0-M1"
  lazy val akkaHttpVersion = "10.1.9"
  lazy val akkaStreamKafkaVersion = "1.0.5"
  lazy val confluentVersion = "5.2.1"

  //lazy val kafkaVersion = "2.1.1"
  lazy val kafkaVersion = "2.2.1"

  //lazy val kafkaStreamsScalaVersion = "0.2.1"

  lazy val embeddedKafkaVersion = kafkaVersion
  //lazy val embeddedKafkaVersion = "2.3.0"

  //lazy val manubEmbeddedKafkaVersion = "0.14.0" // kafka 0.10.x
  lazy val manubEmbeddedKafkaVersion = "2.0.0" // kafka 0.10.x

  //lazy val akkaStreamKafkaTestkitVersion = "1.0-M1"
  lazy val akkaStreamKafkaTestkitVersion = akkaStreamKafkaVersion

  //lazy val circeVersion = "0.9.3"
  //lazy val circeVersion = "0.11.1"
  //lazy val circeDerivationVersion = "0.11.0-M3"
  lazy val circeVersion = "0.12.0-RC2"
  lazy val circeDerivationVersion = "0.12.0-M6"
  lazy val circeJava8Version = "0.12.0-M1"

  lazy val catsVersion = "2.0.0-RC1"
  lazy val enumeratumVersion = "1.5.13"
  lazy val http4sVersion = "0.21.0-M4"
  lazy val jacksonVersion = "2.9.9"
  lazy val javafakerVersion = "1.0.0"
  lazy val jsoniterVersion = "0.55.0"
  lazy val kantanVersion = "0.5.1"
  lazy val scalazVersion = "7.2.28"

  lazy val restAssuredVersion = "4.1.1"

  // scalaxb stuff
  lazy val vScalaXml = "1.2.0"
  //lazy val vScalaParser =  "1.0.1"
  lazy val vScalaParser =  "1.1.2"
  //lazy val vDispatch = "0.11.3"
  lazy val vDispatch = "0.13.4"

  // Libraries
  //val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  //val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  //val specs2core = "org.specs2" %% "specs2-core" % "2.4.17"

  lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % vScalaXml
  lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % vScalaParser
  lazy val dispatch = "net.databinder.dispatch" %% "dispatch-core" % vDispatch

  // Projects
  //val backendDeps =
  //  Seq(akkaActor, specs2core % Test)

  lazy val commonDeps = Seq(
    "com.jsuereth" %% "scala-arm" % "2.0",
    "com.lihaoyi" %% "os-lib" % "0.3.0",

    "com.beachape" %% "enumeratum" % enumeratumVersion,

    "org.scalaz" %% "scalaz-core" % scalazVersion,
    //"com.github.mpilquist" % "simulacrum_2.12" % "0.19.0",

    // config
    "com.github.pureconfig" %% "pureconfig" % "0.11.1",
    // args
    "com.github.scopt" %% "scopt" % "4.0.0-RC2",

    // optics
    "com.softwaremill.quicklens" %% "quicklens" % "1.4.12",

    "org.typelevel" %% "cats-core" % catsVersion
  )

  lazy val akkaDeps = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

    //"com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion, // kafka client 0.11.x
  )
  lazy val csvDeps = Seq(
    "com.nrinaudo" %% "kantan.csv" % kantanVersion,
    "com.nrinaudo" %% "kantan.csv-java8" % kantanVersion,
    "com.nrinaudo" %% "kantan.csv-generic" % kantanVersion,
    "com.nrinaudo" %% "kantan.csv-enumeratum" % kantanVersion,
    "com.nrinaudo" %% "kantan.csv-scalaz" % kantanVersion
  )
  lazy val http4sDeps = Seq(
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.typelevel" %% "cats-core" % "2.0.0-RC1",
    "org.typelevel" %% "cats-effect" % "2.0.0-RC1"
  )
  lazy val sttpVersion = "1.6.6"
  lazy val httpClientDeps = Seq(
    "com.softwaremill.sttp" %% "core" % sttpVersion,
    "com.softwaremill.sttp" %% "circe" % sttpVersion,
    "com.softwaremill.sttp" %% "http4s-backend" % sttpVersion
  )
  lazy val internalDeps = Seq(
    "ru.sbrf.bdata.jenkins.openapi" % "jctl-jersey2-client" % "0.2.0-SNAPSHOT"
  )
  lazy val jinjavaDeps = Seq(
    "com.hubspot.jinjava" % "jinjava" % "2.5.2"
  )
  lazy val jsonDeps = Seq(
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % jacksonVersion,

    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % jsoniterVersion % Compile,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % Provided, // required only in compile-time

    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    // Optional for auto-derivation of JSON codecs
    "io.circe" %% "circe-generic" % circeVersion,
    // Optional for string interpolation to JSON model
    "io.circe" %% "circe-literal" % circeVersion,

    "io.circe" %% "circe-optics" % circeVersion,

    "io.circe" %% "circe-jackson29" % circeVersion,

    "io.circe" %% "circe-derivation" % circeDerivationVersion,
    "io.circe" %% "circe-java8" % circeJava8Version
    //"io.circe" %% "circe-config" % "0.6.1",
  )
  lazy val kafkaClientsDeps = Seq(
    "org.apache.kafka" % "kafka-clients" % kafkaVersion
  )
  lazy val scalaKafkaClientsDeps = Seq(
    "net.cakesolutions" %% "scala-kafka-client" % "2.1.0",
    "net.cakesolutions" %% "scala-kafka-client-akka" % "2.1.0",
    "net.cakesolutions" %% "scala-kafka-client-testkit" % "2.1.0" % "test"
  )
  lazy val kafkaStreamsDeps = Seq(
    "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
    "org.apache.kafka" % "kafka-streams" % kafkaVersion
    //,"com.lightbend" %% "kafka-streams-scala" % kafkaStreamsScalaVersion
  )
  lazy val kafkaStreamsProvidedDeps = Seq(
    "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion % Provided,
    "org.apache.kafka" % "kafka-streams" % kafkaVersion % Provided
    //,"com.lightbend" %% "kafka-streams-scala" % kafkaStreamsScalaVersion
  )

  lazy val akkaLoggingDeps = Seq(
    "com.typesafe.akka" %% "akka-slf4" % akkaVersion
  )
  lazy val loggingDeps = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,

    "org.wvlet.airframe" %% "airframe-log" % airframeVersion,
    "org.wvlet.airframe" %% "airframe-jmx" % airframeVersion,
    //"org.slf4j" % "slf4j-jdk14" % slf4jVersion,

    "com.github.lalyos" % "jfiglet" % "0.0.8"
  )
  lazy val scalaModuleDeps = Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
  )
  lazy val xmlDeps = Seq(
    // xsd
    "org.scala-lang.modules" %% "scala-xml" % vScalaXml,
    "org.scala-lang.modules" %% "scala-parser-combinators" % vScalaParser,
    "net.databinder.dispatch" %% "dispatch-core" % vDispatch
  )
//  lazy val unusedDeps = Seq(
//    "dev.zio" %% "zio" % "1.0.0-RC9-4",
//    "com.lihaoyi" %% "cask" % "0.2.0-2-e0a30a",
//    "com.lihaoyi" %% "requests" % "0.2.0"
//  )

  lazy val itDeps = Seq(
    "org.scalacheck" %% "scalacheck" % "1.14.0" % IntegrationTest,
    "org.scalatest" %% "scalatest" % "3.0.8" % IntegrationTest,
    "org.pegdown" % "pegdown" % "1.6.0" % IntegrationTest,
    //"io.rest-assured" % "rest-assured" % restAssuredVersion % IntegrationTest,
    //"io.rest-assured" % "json-schema-validator" % restAssuredVersion % IntegrationTest,
    //"io.rest-assured" % "scala-support" % restAssuredVersion % IntegrationTest,
    "org.hamcrest" % "hamcrest" % "2.1" % IntegrationTest
  )


  lazy val fakerDeps = Seq(
    "com.github.javafaker" % "javafaker" % javafakerVersion
    //"com.github.javafaker" % "javafaker" % javafakerVersion % Test
  )
  lazy val testDeps = Seq(
    // testing
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "junit" % "junit" % "4.12" % Test,
    "org.assertj" % "assertj-core" % "3.13.2" % Test,
    "org.mockito" % "mockito-core" % "3.0.0" % Test,

    "com.github.javafaker" % "javafaker" % javafakerVersion % Test,
    "com.ibm.icu" % "icu4j" % "64.2" % Test
  ) ++ fakerDeps
  lazy val akkaTestDeps = Seq(
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-kafka-testkit" % akkaStreamKafkaTestkitVersion % Test
  )
  lazy val embeddedKafkaRevolverDeps = Seq(
    "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion,
    "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion
  )
  lazy val kafkaITDeps = Seq(
    "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % IntegrationTest,
    "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion % IntegrationTest
  )
  lazy val kafkaTestDeps = Seq(
    "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % Test,
    "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion % Test,
    //,"net.manub" %% "scalatest-embedded-kafka" % manubEmbeddedKafkaVersion % Test,
    //,"net.manub" %% "scalatest-embedded-kafka-streams" % manubEmbeddedKafkaVersion % Test,

    "org.apache.kafka" % "kafka-clients" % kafkaVersion % Test,
    "org.apache.kafka" % "kafka-clients" % kafkaVersion % Test classifier "test",
    "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion % Test,
    "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion % Test classifier "test"

    //"io.confluent" % "kafka-schema-registry" % confluentVersion,
    //"io.confluent" % "kafka-schema-registry-client" % confluentVersion,

    //"org.apache.kafka" % "kafka-examples" % kafkaVersion % Test,
    //"org.apache.kafka" % "kafka-streams-examples" % kafkaVersion % Test,
    //"io.confluent" % "streams-examples" % "3.0.0" % Test,
  )
  lazy val groovySdkDeps = Seq(
    "org.codehaus.groovy" % "groovy-all" % "2.4.16" % Test // FIXME or provided?
  )
  lazy val gatlingVersion = "3.2"
  lazy val gatlingDeps = Seq(
    "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test",
    "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test"
  )

}
