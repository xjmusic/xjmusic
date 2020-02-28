// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.dao.ProgramDAO;
import io.xj.lib.core.ingest.IngestFactory;
import io.xj.lib.core.model.Chain;
import io.xj.lib.core.model.ChainBinding;
import io.xj.lib.core.model.ProgramSequence;
import io.xj.lib.core.model.ProgramSequenceBinding;
import io.xj.lib.core.model.ProgramSequenceChord;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestProgramStyleIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private ProgramDAO programDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureB1();
    fake.insertFixtureB2();
    fake.insertFixtureB3();

    fake.chain1 = Chain.create();

    programDAO = injector.getInstance(ProgramDAO.class);
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void digest() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist");
    // Add two sequences to Main program 15
    fake.program15 = programDAO.readOne(Access.internal(), fake.program15.getId());
    ProgramSequence sequence15c = test.insert(ProgramSequence.create(fake.program15, 32, "Encore", 0.5, "A major", 135.0));
    test.insert(ProgramSequenceChord.create(sequence15c, 0.0, "NC"));
    test.insert(ProgramSequenceBinding.create(sequence15c, 2));
    ProgramSequence sequence15d = test.insert(ProgramSequence.create(fake.program15, 32, "Encore", 0.5, "A major", 135.0));
    test.insert(ProgramSequenceChord.create(sequence15d, 0.0, "NC"));
    test.insert(ProgramSequenceBinding.create(sequence15d, 3));
    programDAO.update(Access.internal(), fake.program15.getId(), fake.program15);


    DigestProgramStyle result = digestFactory.programStyle(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(fake.chain1, fake.library2))));

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
