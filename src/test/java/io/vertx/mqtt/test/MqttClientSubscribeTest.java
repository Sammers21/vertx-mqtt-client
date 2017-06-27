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
public class MqttClientSubscribeTest {

  private static final String MQTT_TOPIC = "/my_topic";
  private static final String MQTT_MESSAGE = "Hello Vert.x MQTT Client";

  private int messageId = 0;

  @Test
  @Ignore
  public void subscribeQos2AndReceive(TestContext context) throws InterruptedException {
    this.subscribeAndReceive(context, MqttQoS.EXACTLY_ONCE);
  }

  @Test
  public void subscribeQos1AndReceive(TestContext context) throws InterruptedException {
    this.subscribeAndReceive(context, MqttQoS.AT_LEAST_ONCE);
  }

  @Test
  public void subscribeQoS0AndReceive(TestContext context) throws InterruptedException {
    this.subscribeAndReceive(context, MqttQoS.AT_MOST_ONCE);
  }

  @Test
  public void subscribeQoS0(TestContext context) throws InterruptedException {
    this.subscribe(context, MqttQoS.AT_MOST_ONCE);
  }

  @Test
  public void subscribeQoS1(TestContext context) throws InterruptedException {
    this.subscribe(context, MqttQoS.AT_LEAST_ONCE);
  }

  @Test
  public void subscribeQoS2(TestContext context) throws InterruptedException {
    this.subscribe(context, MqttQoS.EXACTLY_ONCE);
  }

  private void subscribeAndReceive(TestContext context, MqttQoS qos) {

    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.publishHandler(publish -> {
        assertTrue(publish.qosLevel() == qos);
        client.disconnect();
        async.countDown();
      });

    client.connect(ar -> {
      assertTrue(ar.succeeded());
      client.subscribe(MQTT_TOPIC, qos.value());
      client.publish(
        MQTT_TOPIC,
        Buffer.buffer(MQTT_MESSAGE.getBytes()),
        qos,
        false,
        false
      );

    });

    async.await();
  }

  private void subscribe(TestContext context, MqttQoS qos) {

    this.messageId = 0;

    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.subscribeCompleteHandler(suback -> {
      assertTrue(suback.messageId() == messageId);
      assertTrue(suback.grantedQoSLevels().contains(qos.value()));
      client.disconnect();
      async.countDown();
    });

    client.connect(ar -> {
      assertTrue(ar.succeeded());

      client.subscribe(MQTT_TOPIC, qos.value(), done -> {
        assertTrue(done.succeeded());
        messageId = done.result();
      });
    });

    async.await();
  }
}
