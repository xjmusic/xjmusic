// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftModule;
import io.xj.core.ingest.IngestFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class GenerationIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private GenerationFactory generationFactory;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryA();
    ingestFactory = injector.getInstance(IngestFactory.class);
    generationFactory = injector.getInstance(GenerationFactory.class);
  }

  /**
   [#154548999] Artist wants to generate a Library Supersequence in order to create a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
   FUTURE assert more of the actual pattern entities after generation of library supersequence in integration testing
   */
  @Test
  public void generation() throws Exception {
    Sequence target = IntegrationTestEntity.insertSequence(2702, 101, 10000001, SequenceType.Detail, SequenceState.Published, "SUPERSEQUENCE", 0.618, "C", 120.4);

    generationFactory.librarySupersequence(target, ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001))));

    Collection<Pattern> generatedPatterns = injector.getInstance(PatternDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(2702)));
    assertFalse(generatedPatterns.isEmpty());
  }

}
