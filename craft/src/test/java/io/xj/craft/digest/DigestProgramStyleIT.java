// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestProgramStyleIT extends FixtureIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private ProgramDAO programDAO;

  @Before
  public void setUp() throws Exception {
    reset();
    insertFixtureB1();
    insertFixtureB2();
    insertFixtureB3();

    chain1 = Chain.create();

    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    programDAO = injector.getInstance(ProgramDAO.class);
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digest() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Artist");
    // Add two sequences to Main program 15
    program15 = programDAO.readOne(internal, program15.getId());
    ProgramSequence sequence15c = insert(ProgramSequence.create(program15, 32, "Encore", 0.5, "A major", 135.0));
    insert(ProgramSequenceChord.create(sequence15c, 0.0, "NC"));
    insert(ProgramSequenceBinding.create(sequence15c, 2));
    ProgramSequence sequence15d = insert(ProgramSequence.create(program15, 32, "Encore", 0.5, "A major", 135.0));
    insert(ProgramSequenceChord.create(sequence15d, 0.0, "NC"));
    insert(ProgramSequenceBinding.create(sequence15d, 3));
    programDAO.update(Access.internal(), program15.getId(), program15);


    DigestProgramStyle result = digestFactory.programStyle(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(chain1, library2))));

    assertNotNull(result);
    assertEquals(2.0, result.getMainSequencesPerProgramStats().min(), 0.1);
    assertEquals(4.0, result.getMainSequencesPerProgramStats().max(), 0.1);
    assertEquals(3.0, result.getMainSequencesPerProgramStats().mean(), 0.1);
    assertEquals(2.0, result.getMainSequencesPerProgramStats().count(), 0.1);
    assertEquals(1, result.getMainSequencesPerProgramHistogram().count(2));
    assertEquals(1, result.getMainSequencesPerProgramHistogram().count(4));
    assertEquals(0, result.getMainSequencesPerProgramHistogram().count(1));
    assertEquals(16.0, result.getMainSequenceTotalStats().min(), 0.1);
    assertEquals(32.0, result.getMainSequenceTotalStats().max(), 0.1);
    assertEquals(26.6, result.getMainSequenceTotalStats().mean(), 0.1);
    assertEquals(6.0, result.getMainSequenceTotalStats().count(), 0.1);
    assertEquals(2, result.getMainSequenceTotalHistogram().count(16));
    assertEquals(4, result.getMainSequenceTotalHistogram().count(32));
  }

}
