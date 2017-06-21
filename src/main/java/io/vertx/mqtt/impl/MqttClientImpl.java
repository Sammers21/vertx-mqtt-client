package io.vertx.mqtt.impl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttSubAckMessage;
import io.vertx.mqtt.messages.MqttPublishMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.netty.handler.codec.mqtt.MqttQoS.*;


/**
 * probably {@link io.vertx.core.net.impl.NetClientImpl} is an example of how it can be implemented ?
 */
public class MqttClientImpl extends NetClientBase<MqttClientConnection> implements MqttClient {

  private static final int MAX_MESSAGE_ID = 65535;
  private static final String PROTOCOL_NAME = "MQTT";
  private static final int PROTOCOL_VERSION = 4;

  /**
   * The client pass some options for connection
   */
  private MqttClientOptions options;

  /**
   * Connection with server
   */
  private MqttClientConnection connection;


  Handler<Integer> publishCompleteHandler;
  Handler<Integer> unsubscribeCompleteHandler;
  Handler<MqttPublishMessage> publishReceivedHandler;
  Handler<MqttSubAckMessage> subscribeCompleteHandler;
  Handler<AsyncResult<MqttClient>> connectHandler;
  Handler<AsyncResult<Void>> pingResultHandler;
  int connectionStatus = -1;
  // counter for the message identifier
  private int messageIdCounter;

  public MqttClientImpl(Vertx vertx, MqttClientOptions options) {
    super((VertxInternal) vertx, options, true);
    this.options = options;
  }

  @Override
  public int connectionStatus() {
    return connectionStatus;
  }

  /**
   * See {@link MqttClient#connect()} for more details
   */
  @Override
  public MqttClient connect() {
    return connect(null);
  }

  /**
   * See {@link MqttClient#connect()} for more details
   */
  @Override
  public MqttClient connect(Handler<AsyncResult<MqttClient>> connectHandler) {

    doConnect(options.getPort(), options.getHost(), "some server", s -> {
      if (s.failed()) {
        callHandleFail(connectHandler, s.cause());
      }
      connection = s.result();

      MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT,
        false,
        EXACTLY_ONCE,
        false,
        0);
      MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
        PROTOCOL_NAME,                        // Protocol Name
        PROTOCOL_VERSION,                     // Protocol Level
        options.hasUsername(),                // Has Username
        options.hasPassword(),                // Has Password
        options.isWillRetain(),
        options.getWillQoS(),
        options.isWillFlag(),                 // Has Will
        options.isCleanSession(),             // Clean Session
        options.getKeepAliveTimeSeconds()     // Timeout
      );
      MqttConnectPayload payload = new MqttConnectPayload(
        options.getClientId() == null ? "" : options.getClientId(),        // Client Identifier
        options.getWillTopic() == null ? "" : options.getWillTopic(),      // Will Topic
        options.getWillMessage() == null ? "" : options.getWillMessage(),  // Will Message
        options.hasUsername() ? options.getUsername() : "",                // Username
        options.hasPassword() ? options.getPassword() : ""                 // Password
      );
      connection.writeToChannel(new MqttConnectMessage(fixedHeader, variableHeader, payload));
      this.connectHandler = connectHandler;
    });
    return this;
  }

  /**
   * See {@link MqttClient#disconnect()} for more details
   */
  @Override
  public MqttClient disconnect() {
    return disconnect(null);
  }

  /**
   * See {@link MqttClient#disconnect()} for more details
   */
  @Override
  public MqttClient disconnect(Handler<AsyncResult<Void>> disconnectHandler) {

    MqttFixedHeader fixedHeader = new MqttFixedHeader(
      MqttMessageType.DISCONNECT,
      false,
      EXACTLY_ONCE,
      false,
      0
    );

    connection.writeToChannel(new MqttMessage(fixedHeader));

    if (disconnectHandler != null) {
      disconnectHandler.handle(
        Future.succeededFuture());
    }
    connection.close();
    return this;
  }

  @Override
  public MqttClient publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain) {
    MqttFixedHeader fixedH = new MqttFixedHeader(
      MqttMessageType.PUBLISH,
      isDup,
      qosLevel,
      isRetain,
      0
    );

    MqttPublishVariableHeader varH = new MqttPublishVariableHeader(topic, nextMessageId());

    io.netty.handler.codec.mqtt.MqttPublishMessage mqttPublishMessage = new io.netty.handler.codec.mqtt
      .MqttPublishMessage(fixedH, varH, payload.getByteBuf());

    connection.writeToChannel(mqttPublishMessage);
    connectionStatus = -1;
    return this;
  }

  @Override
  public MqttClient publishComplete(Handler<Integer> publishCompleteHandler) {
    this.publishCompleteHandler = publishCompleteHandler;
    return this;
  }

  @Override
  public MqttClient publishReceived(Handler<MqttPublishMessage> publishReceivedHandler) {
    this.publishReceivedHandler = publishReceivedHandler;
    return this;
  }

  @Override
  public MqttClient subscribeComplete(Handler<MqttSubAckMessage> subscribeCompleteHandler) {
    this.subscribeCompleteHandler = subscribeCompleteHandler;
    return this;
  }


  @Override
  public MqttClient subscribe(String topic, int qos) {
    return subscribe(topic, qos, null);
  }

  @Override
  public MqttClient subscribe(String topic, int qos, Handler<AsyncResult<Integer>> subscribeSentComplete) {
    HashMap<String, Integer> map = new HashMap<>();
    map.put(topic, qos);

    return subscribe(map, subscribeSentComplete);
  }

  @Override
  public MqttClient subscribe(Map<String, Integer> topics) {
    return subscribe(topics, null);
  }

  @Override
  public MqttClient subscribe(Map<String, Integer> topics, Handler<AsyncResult<Integer>> subscribeSentComplete) {
    MqttFixedHeader fixedH = new MqttFixedHeader(
      MqttMessageType.SUBSCRIBE,
      false,
      AT_LEAST_ONCE,
      false,
      0);

    MqttMessageIdVariableHeader varibaleH = MqttMessageIdVariableHeader.from(nextMessageId());
    List<MqttTopicSubscription> ts = topics.entrySet()
      .stream()
      .map(e -> new MqttTopicSubscription(e.getKey(), valueOf(e.getValue())))
      .collect(Collectors.toList());

    MqttSubscribePayload payload = new MqttSubscribePayload(ts);
    MqttMessage mqttMessage = MqttMessageFactory.newMessage(fixedH, varibaleH, payload);

    connection.writeToChannel(mqttMessage);
    if (subscribeSentComplete != null) {
      subscribeSentComplete.handle(Future.succeededFuture(varibaleH.messageId()));
    }
    return this;
  }

  @Override
  public MqttClient unsubscribeComplete(Handler<Integer> unsubscribeCompleteHandler) {
    this.unsubscribeCompleteHandler = unsubscribeCompleteHandler;
    return this;
  }


  /**
   * See {@link MqttClient#unsubscribe(String)} )} for more details
   */
  @Override
  public MqttClient unsubscribe(String topic) {
    MqttFixedHeader fixedH = new MqttFixedHeader(
      MqttMessageType.UNSUBSCRIBE,
      false,
      AT_LEAST_ONCE,
      false,
      0);

    MqttMessageIdVariableHeader varibaleH = MqttMessageIdVariableHeader.from(nextMessageId());

    MqttUnsubscribePayload payload = new MqttUnsubscribePayload(Stream.of(topic).collect(Collectors.toList()));
    MqttMessage mqttMessage = MqttMessageFactory.newMessage(fixedH, varibaleH, payload);

    connection.writeToChannel(mqttMessage);
    return this;
  }

  @Override
  public MqttClient ping(Handler<AsyncResult<Void>> pingResultHandler) {

    MqttFixedHeader fixedHeader =
      new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);

    io.netty.handler.codec.mqtt.MqttMessage pingreq = MqttMessageFactory.newMessage(fixedHeader, null, null);

    connection.writeToChannel(pingreq);
    this.pingResultHandler = pingResultHandler;

    return this;
  }


  /**
   * Call handler with success
   *
   * @param handler handler to call
   */
  void callHandlerSuccess(Handler<AsyncResult<MqttClient>> handler) {
    if (handler != null) {
      handler.handle(Future.succeededFuture(this));
    }
  }

  /**
   * Call handler with error
   *
   * @param handler      handler to call
   * @param errorMessage message passed in cause()
   */
  void callHandleFail(Handler<AsyncResult<MqttClient>> handler, String errorMessage) {
    if (handler != null) {
      handler.handle(Future.failedFuture(errorMessage));
    }
  }

  /**
   * Call handler with error
   *
   * @param handler handler to call
   * @param cause   the reason of fail
   */
  void callHandleFail(Handler<AsyncResult<MqttClient>> handler, Throwable cause) {
    if (handler != null) {
      handler.handle(Future.failedFuture(cause));
    }
  }

  /**
   * Sends PUBACK packet to server
   */
  void puback(int packetId) {
    MqttFixedHeader fixedHeader =
      new MqttFixedHeader(MqttMessageType.PUBACK, false, AT_MOST_ONCE, false, 0);
    MqttMessageIdVariableHeader variableHeader =
      MqttMessageIdVariableHeader.from(packetId);

    io.netty.handler.codec.mqtt.MqttMessage puback = MqttMessageFactory.newMessage(fixedHeader, variableHeader, null);

    this.connection.writeToChannel(puback);
  }

  /**
   * Sends PUBREC packet to server
   */
  void pubrec(int packetId) {
    MqttFixedHeader fixedHeader =
      new MqttFixedHeader(MqttMessageType.PUBREC, false, AT_MOST_ONCE, false, 0);
    MqttMessageIdVariableHeader variableHeader =
      MqttMessageIdVariableHeader.from(packetId);

    io.netty.handler.codec.mqtt.MqttMessage puback = MqttMessageFactory.newMessage(fixedHeader, variableHeader, null);

    this.connection.writeToChannel(puback);
  }

  /**
   * Sends PUBCOMP packet to server
   */
  void pubcomp(int packetId) {
    MqttFixedHeader fixedHeader =
      new MqttFixedHeader(MqttMessageType.PUBCOMP, false, AT_MOST_ONCE, false, 0);
    MqttMessageIdVariableHeader variableHeader =
      MqttMessageIdVariableHeader.from(packetId);

    io.netty.handler.codec.mqtt.MqttMessage puback = MqttMessageFactory.newMessage(fixedHeader, variableHeader, null);

    this.connection.writeToChannel(puback);
  }

  @Override
  protected MqttClientConnection createConnection(VertxInternal vertx, Channel channel, String host, int port,
                                                  ContextImpl context, SSLHelper helper, TCPMetrics metrics) {
    return new MqttClientConnection(vertx, channel, vertx.getOrCreateContext(), metrics, this.options, this);
  }

  @Override
  protected void handleMsgReceived(MqttClientConnection conn, Object msg) {
    connection = conn;
    conn.handleMessage(msg);
  }

  @Override
  protected void initChannel(ChannelPipeline pipeline) {
    // add into pipeline netty's (en/de)coder
    pipeline.addLast("mqttEncoder", MqttEncoder.INSTANCE);
    pipeline.addLast("mqttDecoder", new MqttDecoder());

  }

  @Override
  protected Object safeObject(Object msg, ByteBufAllocator allocator) {
    return msg;
  }

  /**
   * Update and return the next message identifier
   *
   * @return message identifier
   */
  private int nextMessageId() {

    // if 0 or MAX_MESSAGE_ID, it becomes 1 (first valid messageId)
    this.messageIdCounter = ((this.messageIdCounter % MAX_MESSAGE_ID) != 0) ? this.messageIdCounter + 1 : 1;
    return this.messageIdCounter;
  }

}
