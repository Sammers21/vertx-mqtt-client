package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.mqtt.impl.MqttConnAckMessageImpl;

/**
 * Represents an MQTT CONNACK message
 */
@VertxGen
public interface MqttConnAckMessage {

  /**
   * Create a concrete instance of a Vert.x connack message
   *
   * @param code  return code from the connection request
   * @param isSessionPresent  is an old session is present
   * @return
   */
  static MqttConnAckMessage create(MqttConnectReturnCode code, boolean isSessionPresent) {
    return new MqttConnAckMessageImpl(code, isSessionPresent);
  }

  /**
   * @return  return code from the connection request
   */
  @CacheReturn
  MqttConnectReturnCode code();

  /**
   * @return  is an old session is present
   */
  @CacheReturn
  boolean isSessionPresent();
}
