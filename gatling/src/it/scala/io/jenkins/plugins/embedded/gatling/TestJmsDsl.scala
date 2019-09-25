//package io.jenkins.plugins.gatling
//
//import io.gatling.core.Predef._
//import io.gatling.jms.Predef._
//import javax.jms._
//import scala.concurrent.duration._
//
//class TestJmsDsl extends Simulation {
//
//  // create a ConnectionFactory for ActiveMQ
//  // search the documentation of your JMS broker
//  val connectionFactory =
//  new org.apache.activemq.ActiveMQConnectionFactory("tcp://localhost:61616")
//
//  // alternatively, you can create a ConnectionFactory from a JNDI lookup
//  val jndiBasedConnectionFactory = jmsJndiConnectionFactory
//    .connectionFactoryName("ConnectionFactory")
//    .url("tcp://localhost:61616")
//    .credentials("user", "secret")
//    .contextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory")
//
//  val jmsConfig = jms
//    .connectionFactory(connectionFactory)
//    .usePersistentDeliveryMode
//
//  val scn = scenario("JMS DSL test").repeat(1) {
//    exec(jms("req reply testing").requestReply
//      .queue("jmstestq")
//      .textMessage("hello from gatling jms dsl")
//      .property("test_header", "test_value")
//      .jmsType("test_jms_type")
//      .check(simpleCheck(checkBodyTextCorrect)))
//  }
//
//  setUp(scn.inject(rampUsersPerSec(10) to 1000 during (2 minutes)))
//    .protocols(jmsConfig)
//
//  def checkBodyTextCorrect(m: Message) = {
//    // this assumes that the service just does an "uppercase" transform on the text
//    m match {
//      case tm: TextMessage => tm.getText == "HELLO FROM GATLING JMS DSL"
//      case _               => false
//    }
//  }
//}
