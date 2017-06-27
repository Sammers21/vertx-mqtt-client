package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

/**
 * Exception raised when a connection request fails at MQTT level
 */
public class MqttConnectionException extends Throwable {

  private final MqttConnectReturnCode code;

  /**
   * @return Return code from the CONNACK message
   */
  public MqttConnectReturnCode code() {
    return this.code;
  }

  /**
   * Constructor
   *
   * @param code  return code from the CONNACK message
   */
  public MqttConnectionException(MqttConnectReturnCode code) {
    this.code = code;
  }
}
