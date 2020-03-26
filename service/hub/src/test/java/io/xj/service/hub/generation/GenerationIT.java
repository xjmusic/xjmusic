// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.digest.DigestModule;
import io.xj.service.hub.ingest.IngestFactory;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenerationIT {
  private IngestFactory ingestFactory;
  private GenerationFactory generationFactory;
  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new DigestModule(), new GenerationModule(), new IntegrationTestModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();
    fake.chain1 = Chain.create();
    ingestFactory = injector.getInstance(IngestFactory.class);
    generationFactory = injector.getInstance(GenerationFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  /**
   [#154548999] Artist wants to generate a Library Supersequence in order to of a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
   FUTURE assert more of the actual pattern entities after generation of library supersequence in integration testing
   */
  @Test
  public void generation() throws Exception {
    ProgramSequence target = new ProgramSequence().setTotal(16).setName("SUPERSEQUENCE").setDensity(0.618).setKey("C").setTempo(120.4);

    generationFactory.librarySupersequence(target, ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain1, fake.library10000001))));
  }

}
