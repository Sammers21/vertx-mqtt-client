package io.vertx.mqtt.test;


import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientPingTest extends MqttClientBaseTest {

  @Test
  public void simplePing(TestContext context) throws InterruptedException {
    Async async = context.async();

    MqttClient client = new MqttClientImpl(Vertx.vertx(), new MqttClientOptions());
    client.connect(c -> {
      assertTrue(c.succeeded());
      client.pingResponseHandler(v ->{
        async.countDown();
      });
      client.ping();
    });

    async.await();
  }
}
