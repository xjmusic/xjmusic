// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.ingest.HubIngestFactory;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordProgressionIT {
  private HubIngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private IntegrationTestingFixtures fake;
  private HubIntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubDigestModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();


    ingestFactory = injector.getInstance(HubIngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void digestChordProgression() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(hubAccess, ImmutableSet.of(fake.library10000001.getId()), ImmutableSet.of(), ImmutableSet.of()));

    assertNotNull(result);
  }

  @Test
  public void digestChordProgression_ofLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");
    Set<UUID> entities = ImmutableSet.of(fake.library10000001.getId());

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(hubAccess, entities, ImmutableSet.of(), ImmutableSet.of()));

    assertNotNull(result);
  }

}
