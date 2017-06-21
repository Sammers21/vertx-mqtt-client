package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientUnsubscribeTest extends MqttClientTestBase {


  @Test
  public void subscribe_qos_0(TestContext context) throws InterruptedException {
    Async async = context.async(3);
    new MqttClientImpl(Vertx.vertx(), new MqttClientOptions())
      .publishReceived(s -> async.countDown())
      .unsubscribeComplete(s -> async.countDown())
      //CONNECT
      .connect(ar -> {
        assertTrue(ar.succeeded());
        ar.result().subscribe("/hello", 2);
        ar.result().publish(
          "/hello",
          Buffer.buffer("hello".getBytes()),
          MqttQoS.AT_MOST_ONCE,
          false,
          false
        );
        ar.result().unsubscribe("/topic");
        async.countDown();
      })
    ;

    async.await();
  }
}
