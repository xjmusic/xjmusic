// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GoogleHttpProviderImplTest extends Mockito {
  @Mock
  private GoogleCredential googleCredential;
  private Injector injector;
  private GoogleHttpProvider googleHttpProvider;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(),
      new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
          bind(GoogleCredential.class).toInstance(googleCredential);
        }
      })));

    googleHttpProvider = injector.getInstance(GoogleHttpProvider.class);
  }

  @After
  public void tearDown() {
    googleHttpProvider = null;
  }

  @Test
  public void getTransport() throws Exception {
    HttpTransport httpTransport = googleHttpProvider.getTransport();
    assert httpTransport != null;
  }

}
