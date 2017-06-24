package io.vertx.mqtt.test;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientUnsubscribeTest {

  @Test
  public void unsubscribeQoS0(TestContext context) throws InterruptedException {
    Async async = context.async(3);
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions())
      .publishHandler(s -> async.countDown())
      .unsubscribeCompleteHandler(s -> async.countDown());
      //CONNECT
      client.connect(ar -> {
        assertTrue(ar.succeeded());
        client.subscribe("/hello", 0);
        client.publish(
          "/hello",
          Buffer.buffer("hello".getBytes()),
          MqttQoS.AT_MOST_ONCE,
          false,
          false
        );
        client.unsubscribe("/topic");
        async.countDown();
      });

    async.await();
  }
}
