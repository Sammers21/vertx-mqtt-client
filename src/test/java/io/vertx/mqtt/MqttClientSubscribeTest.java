package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientSubscribeTest extends MqttClientTestBase {

  @Test
  public void subscribe_qos_2(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishReceived(s -> async.countDown())
      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        ar.result().subscribe("/hello", 2);
        ar.result().publish(
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
  public void subscribe_qos_1(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishReceived(s -> async.countDown())
      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        ar.result().subscribe("/hello", 2);
        ar.result().publish(
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
  public void subscribe_qos_0(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishReceived(s -> async.countDown())
      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        ar.result().subscribe("/hello", 2);
        ar.result().publish(
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
