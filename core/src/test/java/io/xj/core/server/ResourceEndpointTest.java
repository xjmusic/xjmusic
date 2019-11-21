// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.server;

import io.xj.core.CoreTest;
import io.xj.core.app.App;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.io.IOException;

/**
 // FUTURE: test the internal components of a resource, but DO NOT re-test the DAO, or test the HTTP portion at all.
 */
public class ResourceEndpointTest extends CoreTest {
  final Logger log = LoggerFactory.getLogger(ResourceEndpointTest.class);
  protected App app;
  protected WebTarget target;
  protected File tempFile;

  @Before
  public void before() throws Exception {
    // tempFile file for access log
    try {
      tempFile = File.createTempFile("access-log-", ".tmp");
    } catch (IOException e) {
      log.error("Could not open access log for writing", e);
    }

    // start the server
    System.setProperty("app.port", "8001");
    System.setProperty("log.access.filename", tempFile.getAbsolutePath());

    app = injector.getInstance(App.class);
    app.configureServer(packages());
    app.start();

    // get the client
    Client client = ClientBuilder.newClient();

    // uncomment the following line if you want to enable
    // support for JSON in the client (you also have to uncomment
    // dependency on jersey-media-json module in pom.xml and Main.startServer())
    // --
    // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

    target = client.target(app.baseURI());
    log.info("[TEST] target baseURI: {}", app.baseURI());
  }

  @After
  public void after() throws Exception {
    app.stop();
    System.clearProperty("app.port");
    System.clearProperty("log.access.filename");
  }

  protected WebTarget target() {
    return target;
  }

  protected String[] packages() {
    return new String[0];
  }
}
