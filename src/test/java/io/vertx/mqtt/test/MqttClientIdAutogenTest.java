package io.vertx.mqtt.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientIdAutogenTest {

  @Test
  public void afterConnectClientIdGenerated(TestContext context) throws InterruptedException {
    Async async = context.async();

    MqttClientOptions options = new MqttClientOptions();
    MqttClient client = MqttClient.create(Vertx.vertx(), options);

    assertThat(options.getClientId(), nullValue());

    client.connect(c -> {

      assertTrue(c.succeeded());
      assertTrue(options.getClientId().length() == 36);
      assertThat(options.getClientId(), notNullValue());
      assertFalse(options.getClientId().isEmpty());

      async.countDown();
    });
    async.await();
  }
}
