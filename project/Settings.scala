//import sbt.Keys.{javacOptions, scalacOptions, target, testOptions, updateOptions}
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport.assembly
import spray.revolver.RevolverPlugin.autoImport.reStart

object Settings {

  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
  lazy val testScalastyle = taskKey[Unit]("testScalastyle")

  lazy val javacOptionsValues = Seq(
    "-Xlint:deprecation"
  )
  lazy val scalacOptionsValues = Seq(
    "-deprecation"
    ,"-encoding"
    ,"UTF-8" // yes, this is 2 args
    ,"-feature"
    ,"-unchecked"
    //,"-Xlint" // TODO enable lint
    ,"-Ywarn-dead-code"
    ,"-Ywarn-numeric-widen"
    ,"-Ypartial-unification" // for cats, http4s
    //,"-Xmacro-settings:print-codecs" // To see generated code for codecs
  )

  lazy val commonSettings:Seq[Def.Setting[_]] = Seq(
    // Resolving a snapshot version. It's going to be slow unless you use `updateOptions := updateOptions.value.withLatestSnapshots(false)` options.
    updateOptions := updateOptions.value.withLatestSnapshots(false),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    javacOptions ++= javacOptionsValues,
    scalacOptions ++= scalacOptionsValues,
    assembly / test := {}, // skip the test during assembly

    // scalastyle TODO enable scalastyle
    //compileScalastyle := scalastyle.in(Compile).toTask("").value,
    //(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
    //testScalastyle := scalastyle.in(Test).toTask("").value,
    //(test in Test) := ((test in Test) dependsOn testScalastyle).value,

    Test / parallelExecution  := true,
    //Test / fork  := true,
    IntegrationTest / fork  := true,
    testOptions ++= Seq (
      Tests.Argument("-oF"), // dump full stask
      Tests.Argument("-h", (target.value.getAbsoluteFile / "test-reports" / "html").getAbsolutePath),
      //Tests.Argument("-h", "target/test-reports/html/"),
    )
  )
  lazy val commonReSettings = {
    Seq(
      //reStart / javaOptions ++= Seq(
      //  "-Dsbt.abs.baseDirectory=" + baseDirectory.value.absolutePath,
      //  "-Dsbt.baseDirectory=" + baseDirectory.value.getPath,
      //  "-Dsbt.abs.target=" + target.value.absolutePath,
      //  "-Dsbt.target=" + target.value.getPath
      //),
      reStart / javaOptions ++= Seq(
        "-Dconfig.file=../service/src/it/resources/application.conf",
        "-Dlogback.configurationFile=../commons/src/test/resources/logback-revolver.xml",
        "-Dlog.name=" + thisProject.value.id
        //"-Dlog.name=" + baseDirectory.value.name,
      )
    )
  }
}
