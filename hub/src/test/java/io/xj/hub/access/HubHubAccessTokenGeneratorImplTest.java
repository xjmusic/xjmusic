// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(MockitoJUnitRunner.class)
public class HubHubAccessTokenGeneratorImplTest {
  private HubAccessTokenGenerator hubAccessTokenGenerator;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(),
      new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(HubAccessTokenGenerator.class).to(HubAccessTokenGeneratorImpl.class);
      }
    }));
    hubAccessTokenGenerator = injector.getInstance(HubAccessTokenGenerator.class);
  }

  @Test
  public void generate_UniqueTokens() {
    String t1 = hubAccessTokenGenerator.generate();
    String t2 = hubAccessTokenGenerator.generate();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }
}
