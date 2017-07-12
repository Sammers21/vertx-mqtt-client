package io.vertx.mqtt.test;


import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.mqtt.impl.MqttClientImpl.isValidTopicFilter;
import static io.vertx.mqtt.impl.MqttClientImpl.isValidTopicName;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MqttClientTopicValidaitonTest {

  @Test
  public void topicNameValidation(){
    assertTrue(isValidTopicName("/"));
    assertTrue(isValidTopicName("/hello"));
    assertTrue(isValidTopicName("sport/tennis/player1"));
    assertFalse(isValidTopicName("sport/tennis/player1#"));
    assertFalse(isValidTopicName("sport/tennis/+/player1#"));
    assertFalse(isValidTopicName("#"));
    assertFalse(isValidTopicName("+"));
    assertFalse(isValidTopicName(""));
  }

  @Test
  public void topicFilterValidation(){
    assertTrue(isValidTopicFilter("#"));
    assertTrue(isValidTopicFilter("+"));
    assertTrue(isValidTopicFilter("+/tennis/#"));
    assertTrue(isValidTopicFilter("sport/+/player1"));
    assertTrue(isValidTopicFilter("+/+"));
    assertTrue(!isValidTopicFilter("sport+"));
    assertTrue(!isValidTopicFilter("sp#ort"));
  }
}
