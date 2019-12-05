// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.app.AppConfiguration;
import io.xj.core.testing.AppTestConfiguration;
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
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
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
