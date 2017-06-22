package io.vertx.mqtt.test;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import org.junit.After;
import org.junit.Before;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for MQTT client unit tests
 */
public class MqttClientBaseTest {

  MqttServer server;
  Set<String> topics = new HashSet<>();

  @Before
  public void before() {
    server = MqttServer.create(Vertx.vertx());
    server.endpointHandler(endpoint -> {


      // shows main connect info
      System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " +
        endpoint.isCleanSession());

      if (endpoint.auth() != null) {
        System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password()
          + "]");
      }
      if (endpoint.will() != null) {
        System.out.println("[will topic = " + endpoint.will().willTopic() + " msg = " + endpoint.will().willMessage() +
          " QoS = " + endpoint.will().willQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
      }

      System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

      // accept connection from the remote client
      endpoint.accept(true);

      endpoint.publishAutoAck(true);
      endpoint.subscriptionAutoAck(true);
      endpoint.publishHandler(ar -> {
        System.out.println(String.format("published topic=%s, message=%s, qos=%d, isdup=%s, isretain=%s",
          ar.topicName(), ar.payload().toString(Charset.forName("UTF-8")),
          ar.qosLevel().value(), ar.isDup(), ar.isRetain()));
        if (topics.contains(ar.topicName())) {
          endpoint.publish(ar.topicName(), ar.payload(), ar.qosLevel(), ar.isDup(), ar.isRetain());
        }
      });

      endpoint.subscribeHandler(ar -> {
        System.out.println("subscribed topic=" + ar.topicSubscriptions().get(0).topicName() + " qos=" + ar
          .topicSubscriptions().get(0).qualityOfService());
        topics.add(ar.topicSubscriptions().get(0).topicName());
      });
    })
      .listen(ar -> {

        if (ar.succeeded()) {

          System.out.println("MQTT server is listening on port " + ar.result().actualPort());
        } else {

          System.out.println("Error on starting the server");
          ar.cause().printStackTrace();
        }
      });


  }

  @After
  public void after() {
    server.close();
  }
}
