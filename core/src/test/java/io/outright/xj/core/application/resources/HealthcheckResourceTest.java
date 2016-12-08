package io.outright.xj.core.application.resources;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.ApplicationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HealthcheckResourceTest {

  private Application app;
    private WebTarget target;
  private File tempFile;

    @Before
    public void setUp() throws Exception {
      // tempFile file for access log
      try {
        tempFile = File.createTempFile("access-log-", ".tmp");
      } catch (IOException e) {
        e.printStackTrace();
      }

      // start the server
      int testPort = 8001;
      System.setProperty("log.access.filename",tempFile.getAbsolutePath());
      app = new ApplicationImpl(
        new String[0],
        testPort
      );
      app.Start();

      // create the client
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
    public void tearDown() throws Exception {
        app.Stop();
    }

    /**
     * Test basic Healthcheck endpoint
     */
    @Test
    public void testGetHealthcheck() {
      String responseMsg = target.path("o2").request().get(String.class);
      assertEquals("OK", responseMsg);
    }
}
