// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ProgramSequence;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftModule;
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
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule()));
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
