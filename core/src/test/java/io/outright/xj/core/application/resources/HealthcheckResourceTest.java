// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.resources;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.ApplicationModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import java.io.File;
import java.io.IOException;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class HealthcheckResourceTest {

  private Application app;
  private WebTarget target;
  private File tempFile;
  private Injector injector = Guice.createInjector(new ApplicationModule());

  @Test
  public void GetHealthcheck() {
    String responseMsg = target.path("o2").request().get(String.class);
    assertEquals("OK", responseMsg);
  }

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

    app = injector.getInstance(Application.class);
    app.Configure();
    app.Start();

    // setup the client
    Client c = ClientBuilder.newClient();

    // uncomment the following line if you want to enable
    // support for JSON in the client (you also have to uncomment
    // dependency on jersey-media-json module in pom.xml and Main.startServer())
    // --
    // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

    target = c.target(app.BaseURI());
    System.out.println("[TEST] target BaseURI: " + app.BaseURI());
  }

  @After
  public void after() throws Exception {
    app.Stop();
    System.clearProperty("app.port");
    System.clearProperty("log.access.filename");
  }

}
