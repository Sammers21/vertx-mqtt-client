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
public class MqttClientPublishTest extends MqttClientTestBase {


  @Test
  public void publish_qos_2_test(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishComplete(comp -> {
        async.countDown();
      })
      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        //QOS 2
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
  public void publish_qos_1_test(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishComplete(comp -> {
        async.countDown();
      })

      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        // QOS 1
        ar.result().publish(
          "/hello",
          Buffer.buffer("hello".getBytes()),
          MqttQoS.AT_LEAST_ONCE,
          false,
          false
        );
      });
  }
}
