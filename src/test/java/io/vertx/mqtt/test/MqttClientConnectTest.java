/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.mqtt.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * MQTT client testing about connection
 */
@RunWith(VertxUnitRunner.class)
public class MqttClientConnectTest {

  @Test
  public void connectDisconnect(TestContext context) throws InterruptedException {

    Async async = context.async();
    MqttClient client = MqttClient.create(Vertx.vertx(), new MqttClientOptions());

    client.connect(c -> {

      assertTrue(c.succeeded());

      client
        .disconnect(ar -> {
          assertTrue(ar.succeeded());
          async.countDown();
        });
    });

    async.await();
  }
}
