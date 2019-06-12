// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.program.sub.Sequence;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.program_style.DigestProgramStyle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

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

    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    programDAO = injector.getInstance(ProgramDAO.class);
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digest() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    // Add two sequences to Main program 15
    program15 = programDAO.readOne(internal, BigInteger.valueOf(15));
    Sequence sequence15c = program15.add(newSequence(32, "Encore", 0.5, "A major", 135.0));
    program15.add(newSequenceChord(sequence15c, 0.0, "NC"));
    program15.add(newSequenceBinding(sequence15c, 2));
    Sequence sequence15d = program15.add(newSequence(32, "Encore", 0.5, "A major", 135.0));
    program15.add(newSequenceChord(sequence15d, 0.0, "NC"));
    program15.add(newSequenceBinding(sequence15d, 3));
    programDAO.update(Access.internal(), BigInteger.valueOf(15), program15);


    DigestProgramStyle result = digestFactory.programStyle(ingestFactory.ingest(access, ImmutableList.of(newChainBinding("Library", 2))));

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
