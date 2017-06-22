package io.vertx.mqtt;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.mqtt.impl.MqttSubAckMessageImpl;
import io.vertx.mqtt.messages.MqttMessage;

import java.util.List;

/**
 * Represents an MQTT SUBACK message
 */
@VertxGen
public interface MqttSubAckMessage extends MqttMessage {

  /**
   * Create a concrete instance of a Vert.x suback message
   *
   * @param messageId message identifier
   * @param grantedQosLevels  list of granted QoS levels
   * @return
   */
  @GenIgnore
  static MqttSubAckMessage create(int messageId, List<Integer> grantedQosLevels) {
    return new MqttSubAckMessageImpl(messageId, grantedQosLevels);
  }

  /**
   * @return  list of granted QoS levels
   */
  @CacheReturn
  List<Integer> grantedQoSLevels();
}
