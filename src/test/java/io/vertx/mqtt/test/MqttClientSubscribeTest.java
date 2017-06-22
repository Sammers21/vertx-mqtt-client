package io.vertx.mqtt.test;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientSubscribeTest extends MqttClientBaseTest {

  @Test
  @Ignore
  public void subscribeQos2(TestContext context) throws InterruptedException {
    Async async = context.async();
    MqttClient client = new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishHandler(s -> async.countDown());
    //CONNECT
    client.connect(ar -> {
      assertTrue(ar.succeeded());
      client.subscribe("/hello", 2);
      client.publish(
        "/hello",
        Buffer.buffer("hello".getBytes()),
        MqttQoS.EXACTLY_ONCE,
        false,
        false
      );
    });

    async.await();
  }

  @Test
  public void subscribeQos1(TestContext context) throws InterruptedException {
    Async async = context.async();
    MqttClient client = new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishHandler(s -> async.countDown());
    //CONNECT
    client.connect(ar -> {
        assertTrue(ar.succeeded());
        client.subscribe("/hello", 2);
        client.publish(
          "/hello",
          Buffer.buffer("hello".getBytes()),
          MqttQoS.AT_LEAST_ONCE,
          false,
          false
        );

      });

    async.await();
  }


  @Test
  public void subscribeQoS0(TestContext context) throws InterruptedException {
    Async async = context.async();
    MqttClient client = new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishHandler(s -> async.countDown());
    //CONNECT
    client.connect(ar -> {
      assertTrue(ar.succeeded());
      client.subscribe("/hello", 2);
      client.publish(
        "/hello",
        Buffer.buffer("hello".getBytes()),
        MqttQoS.AT_MOST_ONCE,
        false,
        false
      );

    });

    async.await();
  }
}
