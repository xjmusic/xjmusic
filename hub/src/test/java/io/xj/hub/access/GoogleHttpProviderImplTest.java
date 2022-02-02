// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GoogleHttpProviderImplTest extends Mockito {
  @Mock
  private GoogleCredential googleCredential;
  private GoogleHttpProvider googleHttpProvider;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(),
      new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
        bind(GoogleCredential.class).toInstance(googleCredential);
      }
    }));

    googleHttpProvider = injector.getInstance(GoogleHttpProvider.class);
  }

  @After
  public void tearDown() {
    googleHttpProvider = null;
  }

  @Test
  public void getTransport() {
    HttpTransport httpTransport = googleHttpProvider.getTransport();
    assert httpTransport != null;
  }

}
