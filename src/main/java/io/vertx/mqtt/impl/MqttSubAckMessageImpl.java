package io.vertx.mqtt.impl;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttSubAckMessage;

import java.util.List;

@DataObject
public class MqttSubAckMessageImpl implements MqttSubAckMessage {

  private List<Integer> grantedQoSLevels;
  private int packetId;

  public MqttSubAckMessageImpl(List<Integer> grantedQoSLevels, int packetId) {
    this.grantedQoSLevels = grantedQoSLevels;
    this.packetId = packetId;
  }

  public MqttSubAckMessageImpl(JsonObject json){
    //TODO
  }

  public List<Integer> grantedQoSLevels() {
    return grantedQoSLevels;
  }


  public int packetId() {
    return packetId;
  }


}
