package io.vertx.mqtt.test;


import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class TestUtil {

  private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

  static final String BROKER_ADDRESS;

  static {
    InputStream is;
    Properties p = null;

    try {
      is = new FileInputStream("./target/project.properties");
      p = new Properties();
      p.load(is);
      log.debug("Properties was loaded successfully");
    } catch (IOException e) {
      log.error("Properties was not loaded, it should be generated during mvn clean install", e);
    }

    BROKER_ADDRESS = p.getProperty("SERVER");
  }
}
