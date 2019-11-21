// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ProgramSequence;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenerationIT extends FixtureIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private GenerationFactory generationFactory;

  @Before
  public void setUp() throws Exception {
    reset();
    insertFixtureA();
    chain1 = Chain.create();
    ingestFactory = injector.getInstance(IngestFactory.class);
    generationFactory = injector.getInstance(GenerationFactory.class);
  }

  /**
   [#154548999] Artist wants to generate a Library Supersequence in order to of a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
   FUTURE assert more of the actual pattern entities after generation of library supersequence in integration testing
   */
  @Test
  public void generation() throws Exception {
    ProgramSequence target = new ProgramSequence().setTotal(16).setName("SUPERSEQUENCE").setDensity(0.618).setKey("C").setTempo(120.4);

    generationFactory.librarySupersequence(target, ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(chain1, library10000001))));
  }

}
