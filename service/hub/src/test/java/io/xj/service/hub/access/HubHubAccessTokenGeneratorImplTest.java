// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(MockitoJUnitRunner.class)
public class HubHubAccessTokenGeneratorImplTest {
  private HubAccessTokenGenerator hubAccessTokenGenerator;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(),
      new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(HubAccessTokenGenerator.class).to(HubAccessTokenGeneratorImpl.class);
        }
      })));

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
