package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

/**
 * Exception raised when a connection request fails at MQTT level
 */
public class MqttConnectionException extends Throwable {

  private final MqttConnectReturnCode code;

  public MqttConnectionException(MqttConnectReturnCode code) {
    this.code = code;
  }
}
