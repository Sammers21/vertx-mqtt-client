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
   * This method returns current status of connection
   * The status is an Integer that could have one of these values:
   * -  value: -1, Disconnected.Initial state of client when you have not tried to connect
   * or after calling of {@link #disconnect()} method
   * -  value: 0, description: Connected.Connection accepted
   * -  value: 1, description: Disconnected.Connection Refused, unacceptable protocol version
   * -  value: 2, description: Disconnected.The Client identifier is correct UTF-8 but not allowed by the Server
   * -  value: 3, description: Disconnected.The Network Connection has been made but the MQTT service is unavailable
   * -  value: 4, description: Disconnected.The data in the user name or password is malformed
   * -  value: 5, description: Disconnected.The Client is not authorized to connect
   *
   * @return current status
   */
  int connectionStatus();

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
  MqttClient connect(Handler<AsyncResult<MqttClient>> connectHandler);

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
  MqttClient publishComplete(Handler<Integer> publishCompleteHandler);

  /**
   * Sets handler which will be called each time server publish something to client
   *
   * @param publishReceivedHandler handler to call
   */
  @Fluent
  MqttClient publishReceived(Handler<MqttPublishMessage> publishReceivedHandler);

  /**
   * Sets handler which will be called after SUBACK packet receiving
   *
   * @param subscribeCompleteHandler handler to call. List inside is a granted QoS array
   */
  @Fluent
  MqttClient subscribeComplete(Handler<MqttSubAckMessage> subscribeCompleteHandler);

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
   * @param subscribeSentComplete handler which will call after SUBSCRIBE packet sending
   */
  @Fluent
  MqttClient subscribe(Map<String, Integer> topics, Handler<AsyncResult<Integer>> subscribeSentComplete);


  /**
   * @param unsubscribeCompleteHandler handler to call. Integer inside is a packetId
   */
  @Fluent
  MqttClient unsubscribeComplete(Handler<Integer> unsubscribeCompleteHandler);

  /**
   * Unsubscribe from receiving messages on given topic
   *
   * @param topic Topic you wanna unsubscribe from
   */
  @Fluent
  MqttClient unsubscribe(String topic);


  /**
   * This method is needed by the client in order to avoid server closes the
   * connection due to the keep alive timeout if client has no messages to send
   *
   * @param pingResultHandler handler with result
   */
  @Fluent
  MqttClient ping(Handler<AsyncResult<Void>> pingResultHandler);

}
