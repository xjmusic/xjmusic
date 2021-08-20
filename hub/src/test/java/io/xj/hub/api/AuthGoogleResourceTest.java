// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.hub.HubApp;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public class AuthGoogleResourceTest {
  final Logger log = LoggerFactory.getLogger(AuthGoogleResourceTest.class);
  protected HubApp app;
  protected WebTarget subject;
  protected File tempFile;

  @Mock
  private HubAccessControlProvider hubAccessControlProvider;

  @Mock
  private HubDatabaseProvider hubDatabaseProvider;

  @Before
  public void before() throws Exception {
    // tempFile file for access log
    try {
      tempFile = File.createTempFile("access-log-", ".tmp");
    } catch (IOException e) {
      log.error("Could not open access log for writing", e);
    }

    var env = Environment.from(ImmutableMap.of(
      "GOOGLE_CLIENT_ID", "12345",
      "GOOGLE_CLIENT_SECRET", "ab1cd2ef3",
      "APP_BASE_URL", "https://xj.io/"
    ));
    var config = HubTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903));
    var injector = Guice.createInjector(ImmutableSet.of(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
          bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
        }
      })));
    app = injector.getInstance(HubApp.class);
    app.start();

    // get the client
    Client client = ClientBuilder.newClient();

    subject = client.target(app.getBaseURI());
    log.debug("[TEST] subject baseURI: {}", app.getBaseURI());
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
        "&redirect_uri=https://xj.io/auth/google/callback" +
        "&response_type=code" +
        "&scope=profile%20email" +
        "&state=xj-music",
      redirectLocation
    );
  }
}
