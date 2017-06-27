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
public class MqttClientPingTest  {

  private static final int PING_NUMBER = 3;
  private static final int KEEPALIVE_TIMEOUT = 2; // seconds

  private int count = 0;

  @Test
  public void manualPing(TestContext context) throws InterruptedException {

    Vertx vertx = Vertx.vertx();

    Async async = context.async();
    MqttClientOptions options = new MqttClientOptions();
    options.setAutoKeepAlive(false);

    count = 0;
    MqttClient client = MqttClient.create(vertx, options);
    client.connect(c -> {
      assertTrue(c.succeeded());
      client.pingResponseHandler(v ->{

        count++;
        if (count == PING_NUMBER) {
          client.disconnect();
          async.countDown();
        }
      });

      vertx.setPeriodic(KEEPALIVE_TIMEOUT * 1000, t ->{
        client.ping();
      });

    });

    async.await();
  }

  @Test
  public void autoPing(TestContext context) throws InterruptedException {

    Async async = context.async();
    MqttClientOptions options = new MqttClientOptions();
    options.setKeepAliveTimeSeconds(KEEPALIVE_TIMEOUT);

    count = 0;
    MqttClient client = MqttClient.create(Vertx.vertx(), options);
    client.connect(c -> {
      assertTrue(c.succeeded());
      client.pingResponseHandler(v ->{

        count++;
        if (count == PING_NUMBER) {
          client.disconnect();
          async.countDown();
        }
      });

    });

    async.await();
  }
}
