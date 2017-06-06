// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.external.google;

import io.xj.core.CoreModule;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GoogleHttpProviderImplTest extends Mockito {
  @Mock private GoogleCredential googleCredential;
  private Injector injector;
  private GoogleHttpProvider googleHttpProvider;

  @Before
  public void setUp() throws Exception {
    createInjector();
    googleHttpProvider = injector.getInstance(GoogleHttpProvider.class);
  }

  @After
  public void tearDown() throws Exception {
    googleHttpProvider = null;
  }

  @Test
  public void getTransport() throws Exception {
    HttpTransport httpTransport = googleHttpProvider.getTransport();
    assert httpTransport != null;
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
          bind(GoogleCredential.class).toInstance(googleCredential);
        }
      }));
  }

}
