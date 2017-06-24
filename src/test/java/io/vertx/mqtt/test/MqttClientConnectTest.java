package io.vertx.mqtt.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientConnectTest {

  @Test
  public void simpleConnectDisconnect(TestContext context) throws InterruptedException {
    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());
    client.connect(c -> {
      assertTrue(c.succeeded());
      client
        .disconnect(ar -> {
          assertTrue(ar.succeeded());
          async.countDown();
        });
    });

    async.await();
  }
}
