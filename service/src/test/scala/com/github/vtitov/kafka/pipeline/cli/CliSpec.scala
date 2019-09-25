package com.github.vtitov.kafka.pipeline.cli

import java.io.ByteArrayOutputStream

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{Matchers, Outcome, fixture}

object CliSpec {

}

class CliSpec
  extends StrictLogging
    with fixture.WordSpecLike
    with Matchers
{
  import scopt.{DefaultOParserSetup, OParserSetup}
  implicit val setup: OParserSetup = new DefaultOParserSetup {
    override def showUsageOnError = Some(true)
    // Overriding the termination handler to no-op.
    override def terminate(exitState: Either[String, Unit]): Unit = ()
  }
  //override type FixtureParam = this.type
  case class FixtureParam(
                           outCapture:ByteArrayOutputStream = new ByteArrayOutputStream
                           ,errCapture:ByteArrayOutputStream = new ByteArrayOutputStream
                         )
  override protected def withFixture(test: OneArgTest): Outcome = {
    val f = FixtureParam()
    import f._
    Console.withOut(outCapture) {Console.withErr(errCapture) {
      test(f)}}
  }

  "cli" can {
    "args" should {
      "parse-args" in { f =>
        import f._
        val cfgOpt = parseArgs(List("--help"))
        execApp(List("--help"))
        logger.debug(s"stdout: ${outCapture}")
        logger.debug(s"stderr: ${errCapture}")
      }
    }
    "template" should {
      lazy val simpleTemplate = s"""Foo Bar is {{foobar}}!"""
      lazy val extTemplate = s"""Foo Bar is {{foobar}} or {{foo.bar}}!"""
      lazy val contextContents =
        s"""
           |foobar=FooBar
           |foo.bar=foo-dot-far
           |""".stripMargin

      "render-template" in { f =>
        import f._
        val tempDir = os.temp.dir(dir = os.pwd / "target", prefix = "jinja-")
        val outPath = tempDir / "foo-bar.out"
        val templateFile = os.temp(
          contents = simpleTemplate,
          dir = tempDir,
          prefix = "tpl-",
          suffix = ".j2")
        val contextFile = os.temp(
          contents = contextContents,
          dir = tempDir,
          prefix = "ctx-",
          suffix = ".properties")
        val cliConf = CliConfig(
          jinjaTemplate = templateFile.toIO,
          jinjaVars = contextFile.toIO,
          jinjaOutput = outPath.toIO,
        )
        doProcessTemplate(cliConf)
        val outContent = os.read(outPath)
        outContent shouldEqual "Foo Bar is FooBar!"
        os.remove(contextFile)
        os.remove(templateFile)
        os.remove(outPath)
        os.remove(tempDir)
      }
      "render-template-ext" ignore { f =>
        import f._
        val tempDir = os.temp.dir(dir = os.pwd / "target", prefix = "jinja-")
        val outPath = tempDir / "foo-bar.out"
        val templateFile = os.temp(
          contents = extTemplate,
          dir = tempDir,
          prefix = "tpl-",
          suffix = ".j2")
        val contextFile = os.temp(
          contents = contextContents,
          dir = tempDir,
          prefix = "ctx-",
          suffix = ".properties")
        val cliConf = CliConfig(
          jinjaTemplate = templateFile.toIO,
          jinjaVars = contextFile.toIO,
          jinjaOutput = outPath.toIO,
        )
        doProcessTemplate(cliConf)
        val outContent = os.read(outPath)
        outContent shouldEqual "Foo Bar is FooBar!"
        os.remove(contextFile)
        os.remove(templateFile)
        os.remove(outPath)
        os.remove(tempDir)
      }
    }
  }
}
