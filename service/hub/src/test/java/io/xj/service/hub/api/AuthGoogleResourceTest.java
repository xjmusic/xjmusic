// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AuthGoogleResourceTest {
  final Logger log = LoggerFactory.getLogger(AuthGoogleResourceTest.class);
  protected HubApp app;
  protected WebTarget subject;
  protected File tempFile;

  @Before
  public void before() throws Exception {
    // tempFile file for access log
    try {
      tempFile = File.createTempFile("access-log-", ".tmp");
    } catch (IOException e) {
      log.error("Could not open access log for writing", e);
    }

    Config config = HubTestConfiguration.getDefault()
      .withValue("google.clientId", ConfigValueFactory.fromAnyRef("12345"))
      .withValue("google.clientSecret", ConfigValueFactory.fromAnyRef("abcdef"))
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903))
      .withValue("app.baseUrl", ConfigValueFactory.fromAnyRef("https://xj.io/"))
      .withValue("app.apiUrl", ConfigValueFactory.fromAnyRef("api/69/"));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule()));
    app = new HubApp(injector);
    app.start();

    // get the client
    Client client = ClientBuilder.newClient();

    // uncomment the following line if you want to enable
    // support for JSON in the client (you also have to uncomment
    // dependency on jersey-media-json module in pom.xml and Main.startServer())
    // --
    // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());
    subject = client.target(app.getBaseURI());
    log.info("[TEST] subject baseURI: {}", app.getBaseURI());
  }

  @After
  public void after() {
    app.finish();
  }

  @Test
  public void GetAuthGoogle() {
    Response response = subject.path("auth/google").request().get(Response.class);

    assertEquals(307, response.getStatus());
    MultivaluedMap<String, Object> headers = response.getHeaders();
    Object redirectLocation = headers.getFirst("Location");
    assertEquals(
      "https://accounts.google.com/o/oauth2/auth" +
        "?client_id=12345" +
        "&redirect_uri=https://xj.io/api/69/auth/google/callback" +
        "&response_type=code" +
        "&scope=profile%20email" +
        "&state=xj-music",
      redirectLocation
    );
  }
}
