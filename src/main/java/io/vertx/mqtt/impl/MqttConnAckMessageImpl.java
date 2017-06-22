package io.vertx.mqtt.impl;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.mqtt.MqttConnAckMessage;

/**
 * Represents an MQTT CONNACK message
 */
public class MqttConnAckMessageImpl implements MqttConnAckMessage {

  private final MqttConnectReturnCode code;
  private final boolean isSessionPresent;

  /**
   * Constructor
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   */
  public MqttConnAckMessageImpl(MqttConnectReturnCode code, boolean isSessionPresent) {
    this.code = code;
    this.isSessionPresent = isSessionPresent;
  }

  public MqttConnectReturnCode code() {
    return this.code;
  }

  public boolean isSessionPresent() {
    return this.isSessionPresent;
  }
}
