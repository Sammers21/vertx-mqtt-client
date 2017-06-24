package examples;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.impl.MqttClientImpl;

public class VertxMqttClientExamples {

  /**
   * Example for demonstration of how {@link MqttClient#connect()} and  {@link MqttClient#disconnect()} methods
   * should be used
   *
   * @param vertx
   */
  public void example1(Vertx vertx) {
    MqttClientOptions options = new MqttClientOptions()
      .setHost("iot.eclipse.org")
      .setPort(1883);

    MqttClient client = MqttClient.create(vertx, options);

    client.connect(s -> client.disconnect());
  }

  /**
   * Example for handling publish messages from server
   *
   * @param client
   */
  public void example2(MqttClient client) {
    client.publishHandler(s -> {
      System.out.println("There are new message in topic: " + s.topicName());
      System.out.println("Content(as string) of the message: " + s.payload().toString());
      System.out.println("QoS: " + s.qosLevel());
    })
      .subscribe("rpi2/temp", 2);
  }

  /**
   * Example for sending publish message
   *
   * @param client
   */
  public void example3(MqttClient client) {
    client.publish("temperature",
      Buffer.buffer("hello".getBytes()),
      MqttQoS.AT_LEAST_ONCE,
      false,
      false);
  }
}
