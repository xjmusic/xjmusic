// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.resources;

import io.outright.xj.core.app.App;
import io.outright.xj.core.CoreModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.io.IOException;

public class ResourceEndpointTest {
  private App app;
  private WebTarget target;
  private File tempFile;
  private Injector injector = Guice.createInjector(new CoreModule());

  @Before
  public void before() throws Exception {
    // tempFile file for access log
    try {
      tempFile = File.createTempFile("access-log-", ".tmp");
    } catch (IOException e) {
      e.printStackTrace();
    }

    // start the server
    System.setProperty("app.port", "8001");
    System.setProperty("log.access.filename", tempFile.getAbsolutePath());

    app = injector.getInstance(App.class);
    app.configure(packages());
    app.start();

    // get the client
    Client c = ClientBuilder.newClient();

    // uncomment the following line if you want to enable
    // support for JSON in the client (you also have to uncomment
    // dependency on jersey-media-json module in pom.xml and Main.startServer())
    // --
    // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

    target = c.target(app.baseURI());
    System.out.println("[TEST] target baseURI: " + app.baseURI());
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

  protected String[] packages() { return new String[0]; }
}
