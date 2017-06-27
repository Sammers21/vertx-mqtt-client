package io.vertx.mqtt.test;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientPublishTest {

  private static final String MQTT_TOPIC = "/my_topic";
  private static final String MQTT_MESSAGE = "Hello Vert.x MQTT Client";

  private int messageId = 0;

  @Test
  @Ignore
  public void publishQoS2(TestContext context) throws InterruptedException {
    this.publish(context, MqttQoS.EXACTLY_ONCE);
  }

  @Test
  public void publishQoS1(TestContext context) throws InterruptedException {
    this.publish(context, MqttQoS.AT_LEAST_ONCE);
  }

  private void publish(TestContext context, MqttQoS qos) {

    this.messageId = 0;

    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.publishCompleteHandler(pubid -> {
      assertTrue(pubid == messageId);
      client.disconnect();
      async.countDown();
    });

    client.connect(ar -> {

      assertTrue(ar.succeeded());

      client.publish(
        MQTT_TOPIC,
        Buffer.buffer(MQTT_MESSAGE.getBytes()),
        qos,
        false,
        false,
        ar1 -> {
          assertTrue(ar.succeeded());
          messageId = ar1.result();
        }
      );
    });

    async.await();
  }
}
