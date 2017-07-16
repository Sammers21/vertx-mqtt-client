package io.vertx.mqtt.test;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class MqttClientTopicValidationTest {

  private static final Logger log = LoggerFactory.getLogger(MqttClientTopicValidationTest.class);
  private static final String MQTT_MESSAGE = "Hello Vert.x MQTT Client";

  private static final String utf65535bytes = IntStream.range(0, 65535)
    .mapToObj(i -> "h")
    .reduce((one, another) -> one + another)
    .get();

  private static final String utf65536bytes = IntStream.range(0, 65536)
    .mapToObj(i -> "h")
    .reduce((one, another) -> one + another)
    .get();



  @Test
  public void topicNameValidation(TestContext context) {
    testPublish("/", true, context);
    testPublish("/hello", true, context);
    testPublish("sport/tennis/player1", true, context);
    testPublish("sport/tennis/player1#", false, context);
    testPublish("sport/tennis/+/player1#", false, context);
    testPublish("#", false, context);
    testPublish("+", false, context);
    testPublish("", false, context);
    testPublish(utf65535bytes, true, context);
    testPublish(utf65536bytes, false, context);
  }

  @Test
  public void topicFilterValidation(TestContext context) {
    testSubscribe("#", true, context);
    testSubscribe("+", true, context);
    testSubscribe("+/tennis/#", true, context);
    testSubscribe("sport/+/player1", true, context);
    testSubscribe("+/+", true, context);
    testSubscribe("sport+", false, context);
    testSubscribe("sp#ort", false, context);
    testSubscribe("+/tennis#", false, context);
    testSubscribe(utf65535bytes, true, context);
    testSubscribe(utf65536bytes, false, context);
  }

  public void testPublish(String topicName, boolean mustBeValid, TestContext context) {
    log.info(String.format("test publishing in \"%s\" topic", topicName));
    Async async = context.async(2);
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.connect(c -> {
      Assert.assertTrue(c.succeeded());

      client.publish(
        topicName,
        Buffer.buffer(MQTT_MESSAGE.getBytes()),
        MqttQoS.AT_MOST_ONCE,
        false,
        false,
        ar1 -> {
          assertThat(ar1.succeeded(), is(mustBeValid));
          log.info("publishing message id = " + ar1.result());
          async.countDown();
          client
            .disconnect(ar -> {
              Assert.assertTrue(ar.succeeded());
              async.countDown();
            });
        });
    });

    async.await();
  }

  public void testSubscribe(String topicFilter, boolean mustBeValid, TestContext context) {
    log.info(String.format("test subscribing for \"%s\" topic", topicFilter));

    Async async = context.async(2);
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.connect(c -> {
      Assert.assertTrue(c.succeeded());

      client.subscribe(
        topicFilter,
        0,
        ar -> {
          assertThat(ar.succeeded(), is(mustBeValid));
          log.info("subscribe message id = " + ar.result());
          async.countDown();
          client
            .disconnect(ar1 -> {
              Assert.assertTrue(ar1.succeeded());
              async.countDown();
            });
        });
    });

    async.await();
  }
}
