package io.vertx.mqtt.impl;


import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.NetworkMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttConnAckMessage;
import io.vertx.mqtt.MqttSubAckMessage;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class MqttClientConnection extends ConnectionBase {

  private TCPMetrics metrics;
  private MqttClientOptions options;
  private MqttClientImpl client;

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
    return metrics;
  }

  @Override
  protected void handleInterestedOpsChanged() {
  }

  /**
   * See for more information http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csprd02/mqtt-v3.1.1-csprd02.html#_Toc385349763
   *
   * @param msg Incoming Packet
   */
  synchronized void handleMessage(Object msg) {

    // handling directly native Netty MQTT messages because we don't need to
    // expose them at higher level (so no need for polyglotization)
    if (msg instanceof io.netty.handler.codec.mqtt.MqttMessage) {

      io.netty.handler.codec.mqtt.MqttMessage mqttMessage = (io.netty.handler.codec.mqtt.MqttMessage) msg;

      DecoderResult result = mqttMessage.decoderResult();
      if (result.isFailure()) {
        channel.pipeline().fireExceptionCaught(result.cause());
        return;
      }
      if (!result.isFinished()) {
        channel.pipeline().fireExceptionCaught(new Exception("Unfinished message"));
        return;
      }

      switch (mqttMessage.fixedHeader().messageType()) {

        case PUBACK:
          handlePuback(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId());
          break;

        case PUBREC:
          handlePubrec(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId());
          break;

        case PUBREL:
          handlePubrel(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId());
          break;

        case PUBCOMP:
          handlePubcomp(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId());
          break;

        case UNSUBACK:
          handleUnsuback(((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId());
          break;

        case PINGRESP:
          handlePingresp();
          break;

        default:

          this.channel.pipeline().fireExceptionCaught(new Exception("Wrong message type " + msg.getClass().getName()));
          break;
      }

      // handling mapped Vert.x MQTT messages (from Netty ones) because they'll be provided
      // to the higher layer (so need for ployglotization)
    } else {

      if (msg instanceof MqttConnAckMessage) {

        handleConnack((MqttConnAckMessage) msg);

      } else if (msg instanceof MqttSubAckMessage) {

        handleSuback((MqttSubAckMessage) msg);

      } else if (msg instanceof MqttPublishMessage) {

        handlePublish((MqttPublishMessage) msg);

      } else {

        this.channel.pipeline().fireExceptionCaught(new Exception("Wrong message type"));
      }
    }
  }

  /**
   * Used for calling the pingresp handler when the server replies to the ping
   */
  synchronized private void handlePingresp() {
    this.client.handlePingresp();
  }

  /**
   * Used for calling the unsuback handler when the server acks an unsubscribe
   *
   * @param unsubackMessageId identifier of the subscribe acknowledged by the server
   */
  synchronized private void handleUnsuback(int unsubackMessageId) {
    this.client.handleUnsuback(unsubackMessageId);
  }

  /**
   * Used for calling the suback handler when the server acknoweldge subscribe to topics
   *
   * @param msg message with suback information
   */
  synchronized private void handleSuback(MqttSubAckMessage msg) {
    this.client.handleSuback(msg);
  }

  /**
   * Used for calling the pubcomp handler when the server client acknowledge a QoS 2 message with pubcomp
   *
   * @param pubcompMessageId identifier of the message acknowledged by the server
   */
  synchronized private void handlePubcomp(int pubcompMessageId) {
    this.client.handlePubcomp(pubcompMessageId);
  }

  /**
   * Used for calling the puback handler when the server acknowledge a QoS 1 message with puback
   *
   * @param pubackMessageId identifier of the message acknowledged by the server
   */
  synchronized private void handlePuback(int pubackMessageId) {
    this.client.handlePuback(pubackMessageId);
  }

  /**
   * Used for calling the pubrel handler when the server acknowledge a QoS 2 message with pubrel
   *
   * @param pubrelMessageId identifier of the message acknowledged by the server
   */
  synchronized private void handlePubrel(int pubrelMessageId) {
    this.client.handlePubrel(pubrelMessageId);
  }

  /**
   * Used for calling the publish handler when the server publishes a message
   *
   * @param msg published message
   */
  synchronized private void handlePublish(MqttPublishMessage msg) {
    this.client.handlePublish(msg);
  }

  /**
   * Used for sending the pubrel when a pubrec is received from the server
   *
   * @param pubrecMessageId identifier of the message acknowledged by server
   */
  synchronized private void handlePubrec(int pubrecMessageId) {
    this.client.handlePubrec(pubrecMessageId);
  }

  /**
   * Used for calling the connect handler when the server replies to the request
   *
   * @param msg  connection response message
   */
  synchronized private void handleConnack(MqttConnAckMessage msg) {
    this.client.handleConnack(msg);
  }
}
