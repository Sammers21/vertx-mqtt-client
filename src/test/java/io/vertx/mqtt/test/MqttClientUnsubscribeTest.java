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

  private static final String MQTT_TOPIC = "/my_topic";

  private int messageId = 0;

  @Test
  public void unsubscribeQoS0(TestContext context) throws InterruptedException {
    this.unsubscribe(context, MqttQoS.AT_MOST_ONCE);
  }

  @Test
  public void unsubscribeQoS1(TestContext context) throws InterruptedException {
    this.unsubscribe(context, MqttQoS.AT_LEAST_ONCE);
  }

  @Test
  public void unsubscribeQoS2(TestContext context) throws InterruptedException {
    this.unsubscribe(context, MqttQoS.EXACTLY_ONCE);
  }

  private void unsubscribe(TestContext context, MqttQoS qos) {

    this.messageId = 0;

    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.unsubscribeCompleteHandler(unsubackid -> {
      assertTrue(unsubackid == messageId);
      client.disconnect();
      async.countDown();
    });

    client.subscribeCompleteHandler(suback -> {
      assertTrue(suback.messageId() == messageId);
      assertTrue(suback.grantedQoSLevels().contains(qos.value()));

      client.unsubscribe(MQTT_TOPIC, ar2 -> {
        assertTrue(ar2.succeeded());
        messageId = ar2.result();
      });
    });

    client.connect(ar -> {
      assertTrue(ar.succeeded());

      client.subscribe(MQTT_TOPIC, qos.value(), ar1 -> {
        assertTrue(ar1.succeeded());
        messageId = ar1.result();
      });
    });

    async.await();
  }
}
