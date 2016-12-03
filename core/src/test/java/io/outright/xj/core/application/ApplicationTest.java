package io.outright.xj.core.application;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

//import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ApplicationTest {

  private Application app;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
      // start the server
      int testPort = 8001;
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
    }

    @After
    public void tearDown() throws Exception {
        app.Stop();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetHealthcheck() {
        String responseMsg = target.path("o2").request().get(String.class);
        assertEquals("OK", responseMsg);
    }
}
