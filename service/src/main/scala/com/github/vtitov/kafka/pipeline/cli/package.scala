package com.github.vtitov.kafka.pipeline

import java.io.{File => JFile}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.streams.KafkaStreams
import com.github.vtitov.kafka.pipeline.config.BuildInfo
import com.github.vtitov.kafka.pipeline.embedded.EKafka
import com.github.vtitov.kafka.pipeline.emulator.RemoteEmulator
import com.github.vtitov.kafka.pipeline.kafka.{Admin, Producer, StreamUtils}

import scala.util.{Failure, Success, Try}
import com.github.vtitov.kafka.pipeline.helpers._
import com.github.vtitov.kafka.pipeline.rest.RestService
import com.github.vtitov.kafka.pipeline.topology.DedupTopology
import scopt.{DefaultOParserSetup, OParserSetup}

import scala.collection.JavaConverters._
import resource._

package object cli extends StrictLogging {
  import CliCommand._
  import com.github.vtitov.kafka.pipeline.config.globalConfig

  implicit def cliCommand2String: CliCommand => String = _.entryName

  def parseArgs(args: Seq[String])(implicit setup: OParserSetup = new DefaultOParserSetup{}):Option[CliConfig] = {
    import scopt.OParser

    lazy val newLine = sys.props("line.separator")


    logger.debug(BuildInfo.toString)
    val builder = OParser.builder[CliConfig]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName(BuildInfo.name)
        ,head(BuildInfo.name, BuildInfo.version)
        ,help("help").text("kafka pipelines")
        ,opt[Unit]("verbose")
          .hidden()
          .action( (_, c) =>
            c.copy(verbose = true) )
          .text("verbose")

        ,opt[Unit]("debug").hidden().action( (_, c) =>
          c.copy(debug = true) ).text("debug")
        ,note(newLine + "run or query streams." + newLine)
        ,note(
          """To install run: ( JARFILE=path/to/kafka-pipeline-assembly.jar && mkdir -p lib/ logs/ && unzip $JARFILE 'conf/*' 'scripts/*' && cp $JARFILE lib/ )
            |Then edit conf/local-rc.sh
            |""".stripMargin
          + newLine)
        ,note(
          """Run http service with command: ( scripts/run-svc.sh http )""")
        ,note(
          """Run streams service with command: ( scripts/run-svc.sh streams )"""
            + newLine)
        ,note(
          """Debug with embedded kafka: ( scripts/run-svc.sh embedded-kafka )""")
        ,note(
          """Debug with remote emulator: ( scripts/run-svc.sh remote-emulator )"""
            + newLine)
        ,note(
          """Stop all with command: ( scripts/kill-all.sh )"""
          + newLine)

        ,cmd(All_Services)
          .action((_, c) => c.copy(mode = All_Services))
          .text("  run all services.")
        .children(
          checkConfig(
            c =>
              success)
        )

        ,cmd(Http)
          .action((_, c) => c.copy(mode = Http))
          .text("  run http server.")
        .children(
          checkConfig(
            c =>
              success)
        )

        ,cmd(Streams)
          .action((_, c) => c.copy(mode = Streams))
          .text("  run kafka streams.")
        .children(
          checkConfig(
            c =>
              success)
        )

        ,cmd(Create_Topics)
          .action((_, c) => c.copy(mode = Create_Topics))
          .text("  create topics.")
        .children(
          checkConfig(
            c =>
              success)
        )

//      randomLines: Option[Int] = None,
//      minRandomWords: Option[Int] = None,
//      maxRandomWords: Option[Int] = None,
        ,cmd(Random_Text)
          .action((_, c) => c.copy(mode = Random_Text))
          .text("  generate random text.")
        .children(
          opt[Int]("lines")
            .action( (n, c) => c.copy(randomLines = n)).text("random text lines"),
          opt[Int]("min-words")
          .action( (n, c) => c.copy(minRandomWords = n)).text("min words per sentence"),
          opt[Int]("max-words")
            .action( (n, c) => c.copy(maxRandomWords = n)).text("max words per sentence"),
          opt[JFile]("out")
            .action( (f, c) =>
              c.copy(randomOutput = f)).text("random text output file"),
          opt[String]("out-topic")
            .action( (f, c) =>
              c.copy(randomOutTopic = Some(f))).text("write random text to topic"),
          checkConfig(
            c =>
              success)
        )

        ,cmd(Process_Template)
          .action((_, c) => c.copy(mode = Process_Template))
          .text("  process template.")
          .children(
            opt[JFile]("vars")
              .action( (f, c) =>
                c.copy(jinjaVars = f)).text("jinja vars"),
            opt[JFile]("template")
              .action( (f, c) =>
                c.copy(jinjaTemplate = f)).text("jinja template"),
            opt[JFile]("out")
              .action( (f, c) =>
                c.copy(jinjaOutput = f)).text("jinja output"),
            checkConfig(
              { c =>
                val filesList = Seq(c.jinjaOutput, c.jinjaTemplate, c.jinjaVars)
                if (filesList.size == filesList.toSet.size) success
                else failure(s"files names should differ: ${filesList.mkString(" ")}")
              })
          )

        ,cmd(Remote_Emulator)
          .action((_, c) => c.copy(mode = Remote_Emulator))
          .text("  remote emulator.")

        ,cmd(Embedded_Kafka)
          .action((_, c) => c.copy(mode = Embedded_Kafka))
          .text("  embedded kafka.")
        .children(
          opt[Int]("kafkaPort")
            //.required()
            .action( (port, c) =>
              c.copy(kafkaPort = Some(port))).text("kafka port"),
          opt[Int]("zkPort")
            .action( (port, c) =>
              c.copy(zkPort = Some(port)) ).text("zookeeper port"),
          checkConfig(
            c =>
              // FIXME reenable checks
              //if(c.zkPort.isDefined && c.zkPort != c.kafkaPort) success
              //else failure("zkPort should not equal kafkaPort")
            success
          )
        )
        ,cmd(Query)
          .action((_, c) => c.copy(mode = Query))
          .text("  quiery tables.")
          .children(
            opt[Unit]("debug-quiery")
              .hidden()
              .action((_, c) => c.copy(debug = true))
              .text("this option is hidden in the usage text"),
            checkConfig(
            c =>
              success)
          )
        ,checkConfig(c =>
            if (CliCommand.withNameOption(c.mode).isEmpty) failure(s"bad or no command [${c.mode}]")
            else success)
      )
    }
    OParser.parse(parser1, args, CliConfig(), setup)
  }

  //def execApp(singleArg: String)(implicit setup: OParserSetup = new DefaultOParserSetup{}) = execApp(Seq(singleArg))
  def execApp(args: Seq[String])(implicit setup: OParserSetup = new DefaultOParserSetup{}) = {
    parseArgs(args) match {
      case Some(config) =>
        logger.info(s"parsed args: $config")
        val mode = CliCommand.withNameOption(config.mode)
        logger.info(s"run mode: $mode")
        mode match {
          case Some(All_Services) => doAllServices(config)
          case Some(Embedded_Kafka) => EKafka.run(
            kafkaPort = config.kafkaPort.getOrElse(globalConfig.kafka.embeddedKafka.kafkaPort.value),
            zkPort = config.zkPort.getOrElse(globalConfig.kafka.embeddedKafka.zookeeperPort.value),
          )
          case Some(Http) => doHttp(config)
          case Some(Process_Template) => doProcessTemplate(config)
          case Some(Random_Text) => doRandomText(config)
          case Some(Query) => doQuery(config)
          case Some(Streams) => doRunStreams(config)
          case Some(Create_Topics) => doCreateTopics(config)
          case Some(Remote_Emulator) => RemoteEmulator.run()
          case Some(_) =>
            logger.error(s"unsupported command")
          case None =>
            logger.info(s"arguments [${args}] are bad, error message will have been displayed")
        }
      case None =>
        logger.info(s"arguments [${args}] are bad, error message will have been displayed")
    }
  }

  def doAllServices(config: CliConfig) = {
    startRestService(config)
    doRunStreams(config)
  }

  def startStreamsWithShutdownHookThread(implicit config: CliConfig = null):KafkaStreams = {
    StreamUtils.startStreamsWithShutdownHookThread(
      topology = DedupTopology.buildTopology(),
      props = globalConfig.kafka.common
        ++ globalConfig.kafka.producer
        ++ globalConfig.kafka.consumer
        ++ globalConfig.kafka.streams,
      "kafka-pipeline"
    )
  }

  def doRunStreams(config: CliConfig) = {
    startStreamsWithShutdownHookThread(config: CliConfig)
  }
  def doQuery(config: CliConfig) = {
    logger.info(figlet.duplicateOneLine(s"Query Streams"))
    ???
  }
  def doCreateTopics(config: CliConfig) = {
    import com.github.vtitov.kafka.pipeline.config.allTopics
    logger.info(s"Create Topics ${allTopics}")
    managed(new Admin) foreach { admin =>
      allTopics.foreach { topic =>
        Try {
          admin.createCustomTopic(topic)
        } match {
          case Success(s) => logger.info(s"createsd topic [${topic}]: $s")
          case Failure(e) => logger.warn(s"couldn't create topic [${topic}]", e)
        }
      }
    }
  }

  def doProcessTemplate(config: CliConfig) = {
    import com.github.vtitov.kafka.pipeline.jinjava.renderJinja
    import os.{GlobSyntax, /}
    import os._
    val parsedVarsFile = ConfigFactory.parseFile(config.jinjaVars).resolve()
    val context:Map[String,String] = parsedVarsFile.entrySet().asScala.map{ entry =>
      entry.getKey -> parsedVarsFile.getString(entry.getKey)
    }.toMap
    val template = os.read(os.Path(config.jinjaTemplate.getAbsolutePath))
    val output = renderJinja(template,context)
    os.write.over(Path(config.jinjaOutput.getAbsolutePath),output)
  }

  def doRandomText(config: CliConfig) = {
    import com.github.javafaker.Faker

    val faker = new Faker()
    val totalLines = config.randomLines
    val minWords:Int = config.minRandomWords
    val additionalWords:Int = config.maxRandomWords-config.minRandomWords
    val producer = config.randomOutTopic.map(_ => new Producer)
    (1 to totalLines).foreach{n=>
      val sentence = faker.lorem().sentence(minWords, additionalWords)
      os.write.append(
        os.Path(config.randomOutput.getAbsolutePath),
        sentence)
      os.write.append(
        os.Path(config.randomOutput.getAbsolutePath),
        "\n")
      logger.debug(s"${n}th sentece of ${totalLines} written to file")
      producer.foreach{pr=>
        pr.send(config.randomOutTopic.get, null, sentence)
        logger.debug(s"${n}th sentece of ${totalLines} sent to topic")
      }
    }
  }

  def doHttp(config: CliConfig) = doRestServiceSleep(config)

  def doRestServiceSleep(config: CliConfig) = {
    startRestService(config)
    Thread.sleep(Long.MaxValue)
    logger.info(s"exitting http RestService")
    ()
  }

  def startRestService(config: CliConfig) = {
    logger.info(s"serving http RestService")
    val restService = new RestService()
    logger.info(s"started http RestService")
    val fiber = restService.fiber()
    logger.info(s"started http RestService fiber")
    sys.ShutdownHookThread {
      fiber.cancel.map(_=>logger.info("cancelled http RestService cancelled"))
    }
    restService
  }
}
package cli {
  import enumeratum.EnumEntry.{Hyphencase, Lowercase}
  import enumeratum._

  case class CliConfig(
                        verbose: Boolean = false,
                        debug: Boolean = false,
                        kafkaPort: Option[Int] = None,
                        zkPort: Option[Int] = None,
                        jinjaVars: JFile = new JFile("vars"),
                        jinjaTemplate: JFile = new JFile("template.j2"),
                        jinjaOutput: JFile = new JFile("template.out"),
                        randomOutput: JFile = new JFile("lorem.out"),
                        randomOutTopic: Option[String] = None,
                        randomLines: Int = 1000,
                        minRandomWords: Int = 10,
                        maxRandomWords: Int = 1000,
                        mode: String = "",
                        kwargs: Map[String, String] = Map(),
                      )

  sealed trait CliCommand extends EnumEntry with Lowercase with Hyphencase

  object CliCommand extends Enum[CliCommand] {

    /*
     `findValues` is a protected method that invokes a macro to find all `Greeting` object declarations inside an `Enum`

     You use it to implement the `val values` member
    */
    val values = findValues

    case object Http              extends CliCommand
    case object Query             extends CliCommand
    case object Streams           extends CliCommand
    case object All_Services      extends CliCommand
    case object Embedded_Kafka    extends CliCommand
    case object Remote_Emulator   extends CliCommand
    case object Process_Template  extends CliCommand
    case object Create_Topics     extends CliCommand
    case object Random_Text       extends CliCommand
  }

}
