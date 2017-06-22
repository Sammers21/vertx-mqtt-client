package io.vertx.mqtt.impl;

import io.vertx.mqtt.MqttSubAckMessage;

import java.util.List;

/**
 * Represents an MQTT SUBACK message
 */
public class MqttSubAckMessageImpl implements MqttSubAckMessage {

  private final int messageId;
  private final List<Integer> grantedQoSLevels;

  /**
   * Constructor
   *
   * @param messageId message identifier
   * @param grantedQoSLevels  list of granted QoS levels
   */
  public MqttSubAckMessageImpl(int messageId, List<Integer> grantedQoSLevels) {
    this.messageId = messageId;
    this.grantedQoSLevels = grantedQoSLevels;
  }

  public int messageId() {
    return this.messageId;
  }

  public List<Integer> grantedQoSLevels() {
    return grantedQoSLevels;
  }
}
