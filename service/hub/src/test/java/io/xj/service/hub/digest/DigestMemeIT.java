// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.ingest.IngestFactory;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DigestMemeIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new DigestModule(), new IntegrationTestModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();

    fake.chain1 = Chain.create();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void digestMeme() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist");

    DigestMeme result = digestFactory.meme(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(fake.chain1, fake.library10000001))));

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
