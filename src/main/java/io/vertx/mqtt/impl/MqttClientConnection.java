package io.vertx.mqtt.impl;


import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.*;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.NetworkMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.impl.MqttPublishMessageImpl;

public class MqttClientConnection extends ConnectionBase {

  private TCPMetrics metrics;
  private MqttClientOptions options;
  private MqttClientImpl client;
  private io.netty.handler.codec.mqtt.MqttPublishMessage lastMessage;

  MqttClientConnection(VertxInternal vertx, Channel channel, ContextImpl context, TCPMetrics metrics,
                       MqttClientOptions options, MqttClientImpl currentState) {
    super(vertx, channel, context);
    this.options = options;
    this.client = currentState;
  }

  public MqttClientConnection(VertxInternal vertx, Channel channel, String host,
                              int port, ContextImpl context, SSLHelper helper, TCPMetrics metrics) {
    super(vertx, channel, context);
    this.metrics = metrics;
  }

  @Override
  public NetworkMetrics metrics() {
    // what to do...
    // probably value must be received via constructor?

    return metrics; //null
  }

  @Override
  protected void handleInterestedOpsChanged() {
    //#TODO implementation here
    System.out.println("handle options");
    // what to do...
  }

  /**
   * See for more information http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csprd02/mqtt-v3.1.1-csprd02.html#_Toc385349763
   *
   * @param msg Incoming Packet
   */
  void handleMessage(Object msg) {

    if (msg instanceof MqttMessage) {

      MqttMessage mqttMessage = (MqttMessage) msg;

      DecoderResult result = mqttMessage.decoderResult();
      if (result.isFailure()) {
        channel.pipeline().fireExceptionCaught(result.cause());
        return;
      }
      if (!result.isFinished()) {
        channel.pipeline().fireExceptionCaught(new Exception("Unfinished message"));
        return;
      }
      if (result.isSuccess() && result.isFinished()) {
        switch (mqttMessage.fixedHeader().messageType()) {
          case CONNACK: handleConnack((MqttConnAckMessage) mqttMessage); break;
          case PUBLISH: handlePublish((io.netty.handler.codec.mqtt.MqttPublishMessage) mqttMessage); break;
          case PUBACK: handlePuback((MqttPubAckMessage) mqttMessage); break;
          case PUBREC: handlePubrec(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId()); break;
          case PUBREL: handlePubrel(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId()); break;
          case PUBCOMP: handlePubcomp(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId()); break;
          case SUBACK: handleSuback((MqttSubAckMessage) mqttMessage); break;
          case UNSUBACK: handleUnsuback((MqttUnsubAckMessage) mqttMessage); break;
          case PINGRESP: handlePingresp(); break;
          default: {
            // TODO handle another packets
          }
          break;

        }
      }
    }
  }

  private void handlePingresp() {
    if (client.pingResultHandler != null) {
      client.pingResultHandler.handle(Future.succeededFuture());
    }
  }


  private void handleUnsuback(MqttUnsubAckMessage mqttMessage) {
    if (client.unsubscribeCompleteHandler != null)
      client.unsubscribeCompleteHandler.handle(mqttMessage.variableHeader().messageId());

  }

  private void handleSuback(MqttSubAckMessage message) {
    if (client.subscribeCompleteHandler != null)
      client.subscribeCompleteHandler.handle(
        new MqttSubAckMessageImpl(message.payload().grantedQoSLevels(), message.variableHeader().messageId())
      );
  }

  private void handlePubcomp(int publishPacketId) {
    if (client.publishCompleteHandler != null)
      client.publishCompleteHandler.handle(publishPacketId);
  }

  private void handlePuback(MqttPubAckMessage mqttMessage) {
    if (client.publishCompleteHandler != null)
      client.publishCompleteHandler.handle(mqttMessage.variableHeader().messageId());

  }

  private void handlePubrel(int publishPacketId) {
    client.pubcomp(publishPacketId);
    handle_immediately(lastMessage);
  }

  private void handlePublish(io.netty.handler.codec.mqtt.MqttPublishMessage mqttMessage) {
    lastMessage = mqttMessage;
    if (mqttMessage.fixedHeader().qosLevel().value() == 0) {
      handle_immediately(mqttMessage);
    } else if (mqttMessage.fixedHeader().qosLevel().value() == 1) {
      client.puback(mqttMessage.fixedHeader().messageType().value());
      handle_immediately(mqttMessage);
    } else if (mqttMessage.fixedHeader().qosLevel().value() == 2) {
      client.pubrec(mqttMessage.fixedHeader().messageType().value());
    }
  }

  private void handle_immediately(io.netty.handler.codec.mqtt.MqttPublishMessage mqttMessage) {

    MqttPublishMessage message =
      new MqttPublishMessageImpl(
        mqttMessage.fixedHeader().messageType().value(),
        mqttMessage.fixedHeader().qosLevel(),
        mqttMessage.fixedHeader().isDup(),
        mqttMessage.fixedHeader().isRetain(),
        mqttMessage.variableHeader().topicName(),
        mqttMessage.payload().duplicate());


    if (client.publishReceivedHandler != null) {
      client.publishReceivedHandler.handle(message);
    }
  }

  private void handlePubrec(int publishPacketId) {
    MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0);
    MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(publishPacketId);
    MqttMessage pubrel = MqttMessageFactory.newMessage(fixedHeader, variableHeader, null);
    this.writeToChannel(pubrel);
  }

  private void handleConnack(MqttConnAckMessage mqttMessage) {

    MqttConnectReturnCode code = mqttMessage.variableHeader().connectReturnCode();

    final String warnMessage;
    if (MqttConnectReturnCode.CONNECTION_ACCEPTED == code) {
      warnMessage = "code: 0 Connection accepted";
    } else if (code == MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION) {
      warnMessage = "code: 1 Connection Refused, unacceptable protocol version";
    } else if (code == MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED) {
      warnMessage = "code: 2 The Client identifier is correct UTF-8 but not allowed by the Server";
    } else if (code == MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE) {
      warnMessage = "code: 3 The Network Connection has been made but the MQTT service is unavailable";
    } else if (code == MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD) {
      warnMessage = "code: 4 The data in the user name or password is malformed";
    } else if (code == MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED) {
      warnMessage = "code: 5  The Client is not authorized to connect";
    } else {
      warnMessage = "code: " + code.byteValue();
    }

    if (code.byteValue() == 0) {
      client.callHandlerSuccess(client.connectHandler);
    } else {
      client.callHandleFail(client.connectHandler, warnMessage);
    }
    client.connectionStatus = code.byteValue();
  }
}
