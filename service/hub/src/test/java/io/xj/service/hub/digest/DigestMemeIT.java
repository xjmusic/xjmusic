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

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DigestMemeIT {
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
  public void digestMeme() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");

    DigestMeme result = digestFactory.meme(ingestFactory.ingest(hubAccess, ImmutableSet.of(fake.library10000001.getId()), ImmutableSet.of(), ImmutableSet.of()));

    // Fuzz
    DigestMemeImpl.DigestMemesItem result1 = result.getMemes().get("Fuzz");
    assertEquals(0, result1.getInstrumentIds().size());
    assertEquals(1, result1.getProgramIds().size());
    assertEquals(1, result1.getSequenceIds(fake.program701.getId()).size());

    // Ants
    DigestMemeImpl.DigestMemesItem result2 = result.getMemes().get("Ants");
    assertEquals(1, result2.getInstrumentIds().size());
    assertEquals(2, result2.getProgramIds().size());

    // Peel
    DigestMemeImpl.DigestMemesItem result3 = result.getMemes().get("Peel");
    assertEquals(1, result3.getInstrumentIds().size());
    assertEquals(1, result3.getProgramIds().size());

    // Gravel
    DigestMemeImpl.DigestMemesItem result4 = result.getMemes().get("Gravel");
    assertEquals(0, result4.getInstrumentIds().size());
    assertEquals(1, result4.getProgramIds().size());
    assertEquals(1, result4.getSequenceIds(fake.program701.getId()).size());

    // Mold
    DigestMemeImpl.DigestMemesItem result5 = result.getMemes().get("Mold");
    assertEquals(1, result5.getInstrumentIds().size());
  }

}
