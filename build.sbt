
// sbt -Djava.io.tmpdir=`cygpath.exe -am /tmp`

import Dependencies._
import Settings._

//publishTo := sonatypePublishToBundle.value
//publishTo := sonatypePublishTo.value


inThisBuild(List(
  organization := "com.github.vtitov.kafka",
  scalaVersion := "2.12.9",
  assembleArtifact := false,

  //licenses := Seq("TODO" -> url("http://host/licenses/TODO")),
  //homepage := Some(url("https://github.com/$ORG/$PROJECT")),
  //developers := List(Developer("$HANDLE", "$NAME", "$EMAIL", url("http://nonexistenthost.com/"))),
  //scmInfo := Some(ScmInfo(url("https://"), "scm:git:git@host/path/to.git")),
  //releaseEarlyWith := SonatypePublisher,

  // If you want to try your setup with a random version locally, you can define
  // releaseEarlyEnableLocalReleases := true
  // in your projects so that local releases are enabled.
  // This is not recommended, releases should happen in isolated environments like a CI.
  //releaseEarlyEnableLocalReleases := true,
  //releaseEarlyNoGpg := true,
  dynverSonatypeSnapshots := true,

  // FIXME
  evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false),


))

//ThisBuild / organization := "com.github.vtitov.kafka.pipeline"
//ThisBuild / scalaVersion := "2.12.9"

lazy val root = (project in file("."))
  .disablePlugins(AssemblyPlugin,RevolverPlugin)
  .aggregate(`kafka-pipeline`)
  //.settings(commonSettings:_*)
  .settings(
    scalaVersion := "2.12.9",
    name := "kafka-pipeline-root",
    //assembleArtifact := false,
  )

//lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
//lazy val testScalastyle = taskKey[Unit]("testScalastyle")
//
//lazy val javacOptionsValues = Seq(
//  "-Xlint:deprecation"
//)
//lazy val scalacOptionsValues = Seq(
//  "-deprecation"
//  ,"-encoding"
//  ,"UTF-8" // yes, this is 2 args
//  ,"-feature"
//  ,"-unchecked"
//  //,"-Xlint" // TODO enable lint
//  ,"-Ywarn-dead-code"
//  ,"-Ywarn-numeric-widen"
//  ,"-Ypartial-unification" // for cats, http4s
//  //,"-Xmacro-settings:print-codecs" // To see generated code for codecs
//)
//
//lazy val commonSettings:Seq[Def.Setting[_]] = Seq(
//  // Resolving a snapshot version. It's going to be slow unless you use `updateOptions := updateOptions.value.withLatestSnapshots(false)` options.
//  updateOptions := updateOptions.value.withLatestSnapshots(false),
//  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
//  javacOptions ++= javacOptionsValues,
//  scalacOptions ++= scalacOptionsValues,
//  assembly / test := {}, // skip the test during assembly
//
//  Test / parallelExecution  := true,
//  Test / fork  := true,
//  testOptions ++= Seq (
//    Tests.Argument("-oF"), // dump full stask
//    Tests.Argument("-h", (target.value.getAbsoluteFile / "test-reports" / "html").getAbsolutePath),
//    //Tests.Argument("-h", "target/test-reports/html/"),
//  ),
//  // scalastyle TODO enable scalastyle
//  //compileScalastyle := scalastyle.in(Compile).toTask("").value,
//  //(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
//  //testScalastyle := scalastyle.in(Test).toTask("").value,
//  //(test in Test) := ((test in Test) dependsOn testScalastyle).value,
//)


lazy val commons = project
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= loggingDeps ++ commonDeps ++ testDeps,
  )

lazy val core = project
  .dependsOn(
    Seq
    (commons,xml)
      .map(_ % "compile->compile;test->test"):_*)
  .aggregate(commons,xml)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings:_*)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.vtitov.kafka.pipeline.config",

    libraryDependencies ++= loggingDeps ++ commonDeps,
    libraryDependencies ++= jinjavaDeps, // FIXME refactor out, move to service or new separate subproject
    libraryDependencies ++= kafkaStreamsProvidedDeps, // FIXME refactor, move to kafka-core
    libraryDependencies ++= Seq(
      csvDeps,
      fakerDeps,
      jsonDeps,
      httpClientDeps,
      scalaModuleDeps,
      testDeps,
    ).flatten
      ++ Seq(),
  )
  .settings(commonReSettings:_*)
  .settings(
    reStart / mainClass := Some("com.github.vtitov.kafka.pipeline.unused.Main"),
  )

lazy val xml = project
  .disablePlugins(RevolverPlugin)
  .enablePlugins(ScalaxbPlugin)
  .settings(commonSettings:_*)
  .settings(
    scalaxbDispatchVersion in (Compile, scalaxb) := vDispatch,
    scalaxbPackageName in (Compile, scalaxb)     := "com.github.vtitov.kafka.pipeline.xml",
    libraryDependencies := xmlDeps,
  )

lazy val `embedded-kafka` = project // (project in file("embedded-kafka"))
  .dependsOn(
    Seq
    (commons)
      .map(_ % "compile->compile;test->test"):_*)
  .settings(
    //name := "embedded-kafka",
    libraryDependencies ++= commonDeps ++loggingDeps ++ embeddedKafkaRevolverDeps ++ scalaModuleDeps,

  )
  .settings(
    //    //reStart / javaOptions +=
    //    //  "-Dlogback.configurationFile=" + (baseDirectory.value / "embedded-kafka/src/test/resources/logback-embedded-kafka.xml").absolutePath,
    //    reStart / javaOptions ++= Seq(
    //      "-Dlogback.configurationFile=../commons/src/test/resources/logback-revolver.xml",
    //      "-Dlog.name=" + thisProject.value.id,
    //    ),
    //reStart / reStartArgs ++= Seq("7011","7010"),
    //reStartArgs in reStart  ++= Seq("7011","7010"),
    reStart / mainClass := Some("com.github.vtitov.kafka.pipeline.embedded.EKafka"),
    reStartArgs ++= Seq("7031","7030"),
  )
  .settings(commonReSettings:_*)

lazy val `gatling` = project // (project in file("tfs-emulator"))
  //.dependsOn(
  //  Seq
  //  (core)
  //    .map(_ % "compile->compile;test->test"):_*)
  //.aggregate(commons, core)
  //.settings(commonSettings:_*)
  .settings(
  )
lazy val `remote-emulator` = project
  .dependsOn(
    Seq
    (core)
      .map(_ % "compile->compile;test->test"):_*)
  .aggregate(commons, core)
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= kafkaStreamsDeps ++ kafkaTestDeps ++ akkaTestDeps,
    reStart / mainClass := Some("com.github.vtitov.kafka.pipeline.emulator.RemoteEmulator"),
  )
  .settings(commonReSettings:_*)

lazy val `kafka-client` = project // (project in file("kafka-client"))
  .disablePlugins(RevolverPlugin)
  .dependsOn(
    Seq
    (commons)
      .map(_ % "compile->compile;test->test"):_*)
  .aggregate(commons)
  .settings(commonSettings:_*)
  .settings(
    //name := "kafka-client",
    libraryDependencies
      ++= kafkaClientsDeps ++ scalaModuleDeps
    ,
  )

lazy val http = project
  .disablePlugins(RevolverPlugin)
  .dependsOn(
    Seq
    (core, `kafka-client`)
      .map(_ % "compile->compile;test->test"):_*)
  .aggregate(commons,core,`kafka-client`)
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= http4sDeps ++ jsonDeps,
    //    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    //    javacOptions ++= javacOptionsValues,
    //    scalacOptions ++= scalacOptionsValues,
  )

lazy val kafka = project
  .disablePlugins(RevolverPlugin)
  .dependsOn(
    Seq
    (core)
      .map(_ % "compile->compile;test->test"):_*)
  .aggregate(commons, core)
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= kafkaStreamsDeps ++ kafkaTestDeps
      //++ akkaLoggingDeps
      ++ akkaDeps
      ++ akkaTestDeps,
  )

//import sbtassembly.AssemblyKeys._
//import com.typesafe.sbt.SbtNativePackager._
lazy val `kafka-pipeline` = project
  .disablePlugins(RevolverPlugin)
  .dependsOn(
    Seq
    (service)
      .map(_ % "compile->compile;test,it->test"):_*)
  .aggregate(service)
  .enablePlugins(JavaServerAppPackaging,UniversalPlugin,UniversalDeployPlugin)
  .configs(IntegrationTest)
  .settings(commonSettings:_*)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= itDeps ++ kafkaITDeps,

    assembleArtifact := true,
    assembly / aggregate := false,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    assembly / test := {},
    assembly / mainClass := Some("com.github.vtitov.kafka.pipeline.cli.CliMain"),

    // publish fat jar
    artifact in (Compile, assembly) := {
      val art = (artifact in (Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in (Compile, assembly), assembly),
  )
  .settings(
  )

lazy val service = project
  .enablePlugins(AsciidoctorPlugin)
  .disablePlugins(AssemblyPlugin)
  .dependsOn(
    Seq
    (http,kafka,`embedded-kafka`,`remote-emulator`)
      .map(_ % "compile->compile;test,it->test"):_*)
  .aggregate(http,kafka,`embedded-kafka`,`remote-emulator`)
  .enablePlugins(JavaServerAppPackaging,UniversalPlugin,UniversalDeployPlugin)
  .configs(IntegrationTest)
  .settings(commonSettings:_*)
  .settings(
    reStart / javaOptions ++= Seq("-Dconfig.file=src/it/resources/application.conf"),
    reStart / mainClass := Some("com.github.vtitov.kafka.pipeline.cli.CliMain"),
    reStartArgs ++= Seq("all-services")
  )
  .settings(commonReSettings:_*)
  .settings(
    Defaults.itSettings,
    //IntegrationTest /  javaOptions ++= Seq("-Dconfig.file=src/it/resources/application.conf"),
    libraryDependencies ++= itDeps ++ kafkaITDeps,
  )
  .settings(
    flatten in EditSource := false,
    //    sources in EditSource ++= (baseDirectory.value / "src" / "ansible" * "*.yml").get,
    //    targetDirectory in EditSource := baseDirectory.value / "target" / "ansible",
    sources in EditSource ++= (baseDirectory.value / "src" * "*.yml").get,
    targetDirectory in EditSource := baseDirectory.value / "target" ,
    variables in EditSource += "version" -> version.value,
    compile in Compile := ((compile in Compile) dependsOn (edit in EditSource)).value
  )


// set logLevel in compile := Level.Info
// set logLevel in compile := Level.Error
//logLevel := Level.Info
//logLevel := Level.Error // FIXME debug only
