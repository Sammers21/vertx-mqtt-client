/**
 * = Vert.x MQTT client
 *
 * == Using Vert.x MQTT client
 * == Getting started
 *
 * === Connect/Disconnect
 * The client gives you opportunity to connect to a server and disconnect from it.
 * Also, you could specify things like the host and port of a server you would like
 * to connect to passing instance of {@link io.vertx.mqtt.MqttClientOptions} as a param through constructor.
 *
 * This example shows how you could connect to a server and disconnect from it using Vert.x MQTT client and calling {@link io.vertx.mqtt.MqttClient#connect()} and {@link io.vertx.mqtt.MqttClient#disconnect()} methods.
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttClientExamples#example1}
 * ----
 * NOTE: default address of server provided by {@link io.vertx.mqtt.MqttClientOptions} is localhost:1883 and localhost:8883 if you are using SSL/TSL.
 * === Subscribe to a topic
 *
 * Now, lest go deeper and take look at this example:
 *
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttClientExamples#example2}
 * ----
 *
 * Here we have the example of usage of {@link io.vertx.mqtt.MqttClient#subscribe(java.lang.String, int)} method. In order to receive messages from rpi2/temp topic we call {@link io.vertx.mqtt.MqttClient#subscribe(java.lang.String, int)} method.
 * Although, to handle received messages from server you need to provide a handler, which will be called each time you have a new messages in the topics you subscribe on.
 * As this example shows, handler could be provided via {@link io.vertx.mqtt.MqttClient#publishHandler(io.vertx.core.Handler)} method.
 *
 * === Publishing message to a topic
 *
 * If you would like to publish some message into topic then {@link io.vertx.mqtt.MqttClient#publish(java.lang.String, io.vertx.core.buffer.Buffer, io.netty.handler.codec.mqtt.MqttQoS, boolean, boolean)} should be called.
 * Let's take a look at the example:
 * [source,$lang]
 * ----
 * {@link examples.VertxMqttClientExamples#example3}
 * ----
 *
 * In the example we send message to topic with name "temperature".
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-mqtt-client", groupPackage = "io.vertx")
package io.vertx.mqtt;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
