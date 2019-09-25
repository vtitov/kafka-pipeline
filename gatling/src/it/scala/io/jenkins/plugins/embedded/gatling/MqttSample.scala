//package io.jenkins.plugins.gatling
//
//import scala.concurrent.duration._
//import io.gatling.core.Predef._
//import io.gatling.frontline.mqtt.Predef._
//
//class MqttSample {
//
//  private val mqttConf = mqtt
//    .broker("localhost", 1883)
//    .correlateBy(jsonPath("$.correlationId"))
//
//  private val scn = scenario("MQTT Test")
//    .feed(csv("topics-and-payloads.csv"))
//    .exec(mqtt("Connecting").connect)
//    .exec(mqtt("Subscribing").subscribe("${myTopic}"))
//    .exec(mqtt("Publishing").publish("${myTopic}").message(StringBody("${myTextPayload}"))
//      .expect(100 milliseconds).check(jsonPath("$.error").notExists))
//
//  setUp(scn.inject(rampUsersPerSec(10) to 1000 during (2 minutes)))
//    .protocols(mqttConf)
//}
