package io.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.messages.MqttPublishMessage;

import java.util.Map;

/**
 * An MQTT client
 */
@VertxGen
public interface MqttClient {

  /**
   * Connects to an MQTT server using options provided through the constructor
   */
  @Fluent
  MqttClient connect();

  /**
   * Connects to an MQTT server calling connectHandler after connection and using options provided through the
   * constructor
   *
   * @param connectHandler handler called when the asynchronous connect call ends
   */
  @Fluent
  MqttClient connect(Handler<AsyncResult<MqttConnAckMessage>> connectHandler);

  /**
   * Disconnects from the server
   */
  @Fluent
  MqttClient disconnect();

  /**
   * Disconnects from the server calling disconnectHandler after disconnection
   *
   * @param disconnectHandler handler called when asynchronous disconnect call ends
   */
  @Fluent
  MqttClient disconnect(Handler<AsyncResult<Void>> disconnectHandler);

  /**
   * Sends the PUBLISH message to the remote MQTT server
   *
   * @param topic    topic on which the message is published
   * @param payload  message payload
   * @param qosLevel quality of service level
   * @param isDup    if the message is a duplicate
   * @param isRetain if the message needs to be retained
   */
  @Fluent
  MqttClient publish(String topic, Buffer payload, MqttQoS qosLevel, boolean isDup, boolean isRetain);

  /**
   * Sets handler which will be called each time publish is completed
   *
   * @param publishCompleteHandler handler to call. Integer inside is a packetId
   */
  @Fluent
  MqttClient publishCompleteHandler(Handler<Integer> publishCompleteHandler);

  /**
   * Sets handler which will be called each time server publish something to client
   *
   * @param publishHandler handler to call
   */
  @Fluent
  MqttClient publishHandler(Handler<MqttPublishMessage> publishHandler);

  /**
   * Sets handler which will be called after SUBACK packet receiving
   *
   * @param subscribeCompleteHandler handler to call. List inside is a granted QoS array
   */
  @Fluent
  MqttClient subscribeCompleteHandler(Handler<MqttSubAckMessage> subscribeCompleteHandler);

  /**
   * Subscribes to the topic
   *
   * @param topic topic you subscribe on
   * @param qos   quality of service
   */
  @Fluent
  MqttClient subscribe(String topic, int qos);

  /**
   * Subscribes to the topic
   *
   * @param topic                 topic you subscribe on
   * @param qos                   quality of service
   * @param subscribeSentComplete handler which will call after SUBSCRIBE packet sending
   */
  @Fluent
  MqttClient subscribe(String topic, int qos, Handler<AsyncResult<Integer>> subscribeSentComplete);

  /**
   * Subscribes to the topics
   *
   * @param topics topics you subscribe on
   */
  @Fluent
  MqttClient subscribe(Map<String, Integer> topics);


  /**
   * Subscribes to the topic and adds a handler which will be called each time you have a new message on a topic
   *
   * @param topics                topics you subscribe on
   * @param subscribeSentHandler  handler which will call after SUBSCRIBE packet sending
   */
  @Fluent
  MqttClient subscribe(Map<String, Integer> topics, Handler<AsyncResult<Integer>> subscribeSentHandler);


  /**
   * Sets handler which will be called after UNSUBACK packet receiving
   *
   * @param unsubscribeCompleteHandler handler to call. Integer inside is a packetId
   */
  @Fluent
  MqttClient unsubscribeCompleteHandler(Handler<Integer> unsubscribeCompleteHandler);

  /**
   * Unsubscribe from receiving messages on given topic
   *
   * @param topic Topic you wanna unsubscribe from
   */
  @Fluent
  MqttClient unsubscribe(String topic);

  /**
   * Unsubscribe from receiving messages on given topic
   *
   * @param topic Topic you wanna unsubscribe from
   * @param unsubscribeSentHandler  handler which will call after UNSUBSCRIBE packet sending
   */
  @Fluent
  MqttClient unsubscribe(String topic, Handler<AsyncResult<Integer>> unsubscribeSentHandler);

  /**
   * Sets handler which will be called after PINGRESP packet receiving
   *
   * @param pingResponseHandler handler to call
   */
  @Fluent
  MqttClient pingResponseHandler(Handler<Void> pingResponseHandler);

  /**
   * This method is needed by the client in order to avoid server closes the
   * connection due to the keep alive timeout if client has no messages to send
   */
  @Fluent
  MqttClient ping();
}
