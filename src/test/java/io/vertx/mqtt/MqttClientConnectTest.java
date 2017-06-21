package io.vertx.mqtt;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientConnectTest extends MqttClientTestBase {


  @Test
  public void simple_connect_disconnect(TestContext context) throws InterruptedException {
    Async async = context.async();
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .connect(c -> {
        assertTrue(c.succeeded());
        c.result()
          .disconnect(ar -> {
            assertTrue(ar.succeeded());
            async.countDown();
          });
      });
    async.await();
  }
}
