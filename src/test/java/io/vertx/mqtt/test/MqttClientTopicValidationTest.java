package io.vertx.mqtt.test;


import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.impl.MqttClientImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientTopicValidationTest {

  private MqttClientImpl mqttClientImpl = (MqttClientImpl) MqttClient.create(Vertx.vertx());

  @Test
  public void topicNameValidation() {
    assertTrue(mqttClientImpl.isValidTopicName("/"));
    assertTrue(mqttClientImpl.isValidTopicName("/hello"));
    assertTrue(mqttClientImpl.isValidTopicName("sport/tennis/player1"));
    assertFalse(mqttClientImpl.isValidTopicName("sport/tennis/player1#"));
    assertFalse(mqttClientImpl.isValidTopicName("sport/tennis/+/player1#"));
    assertFalse(mqttClientImpl.isValidTopicName("#"));
    assertFalse(mqttClientImpl.isValidTopicName("+"));
    assertFalse(mqttClientImpl.isValidTopicName(""));
  }

  @Test
  public void topicFilterValidation() {
    assertTrue(mqttClientImpl.isValidTopicFilter("#"));
    assertTrue(mqttClientImpl.isValidTopicFilter("+"));
    assertTrue(mqttClientImpl.isValidTopicFilter("+/tennis/#"));
    assertTrue(mqttClientImpl.isValidTopicFilter("sport/+/player1"));
    assertTrue(mqttClientImpl.isValidTopicFilter("+/+"));
    assertFalse(mqttClientImpl.isValidTopicFilter("sport+"));
    assertFalse(mqttClientImpl.isValidTopicFilter("sp#ort"));
  }
}
