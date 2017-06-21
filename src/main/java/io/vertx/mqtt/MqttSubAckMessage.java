package io.vertx.mqtt;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

@VertxGen
public interface MqttSubAckMessage {

  @CacheReturn
  List<Integer> grantedQoSLevels();

  @CacheReturn
  int packetId();

}
