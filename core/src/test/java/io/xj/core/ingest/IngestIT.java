//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.util.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class IngestIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private IngestFactory ingestFactory;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private static Map<String, Integer> classTally(Collection<Entity> allEntities) {
    Map<String, Integer> out = Maps.newHashMap();
    allEntities.forEach(entity -> {
      String name = Text.getSimpleName(entity);
      out.put(name, out.containsKey(name) ? out.get(name) + 1 : 1);
    });
    return out;
  }

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();
    fake.chain3 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    ingestFactory = injector.getInstance(IngestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void ingest() throws Exception {
    Ingest result = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(2, result.getAllInstruments().size());
  }

  @Test
  public void getAllPrograms() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(3, ingest.getAllPrograms().size());
  }

  @Test
  public void getProgramsOfType() throws Exception {
    test.insert(Program.create(fake.user101, fake.library10000001, ProgramType.Rhythm, ProgramState.Published, "cups", "B", 120.4, 0.6));
    test.insert(Program.create(fake.user101, fake.library10000001, ProgramType.Main, ProgramState.Published, "plates", "Bb", 120.4, 0.6));
    test.insert(Program.create(fake.user101, fake.library10000001, ProgramType.Detail, ProgramState.Published, "bowls", "A", 120.4, 0.6));
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(3, ingest.getProgramsOfType(ProgramType.Main).size());
    assertEquals(2, ingest.getProgramsOfType(ProgramType.Rhythm).size());
    assertEquals(1, ingest.getProgramsOfType(ProgramType.Detail).size());
  }

  @Test
  public void access() throws Exception {
    Access access = Access.internal();
    Ingest ingest = ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(access, ingest.getAccess());
  }

  @Test
  public void getProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals("leaves", ingest.getProgram(fake.program701.getId()).getName());
    assertEquals("coconuts", ingest.getProgram(fake.program702.getId()).getName());
    assertEquals("bananas", ingest.getProgram(fake.program703.getId()).getName());
  }

  @Test
  public void getProgram_exceptionOnMissing() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("No such Program");
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    ingest.getProgram(UUID.randomUUID());
  }

  @Test
  public void getAllInstruments() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(2, ingest.getAllInstruments().size());
  }

  @Test
  public void getInstrumentsOfType() throws Exception {
    test.insert(Instrument.create(fake.user101, fake.library10000001, InstrumentType.Harmonic, InstrumentState.Published, "Dreamy"));
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals(2, ingest.getInstrumentsOfType(InstrumentType.Percussive).size());
    assertEquals(1, ingest.getInstrumentsOfType(InstrumentType.Harmonic).size());
  }

  @Test
  public void getInstrument() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals("808 Drums", ingest.getInstrument(fake.instrument201.getId()).getName());
  }

  @Test
  public void getAllEntities() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    Collection<Entity> result = ingest.getAllEntities();

    assertEquals(53, result.size());
    Map<String, Integer> classes = classTally(result);
    assertEquals(Integer.valueOf(3), classes.get("Program"));
    assertEquals(Integer.valueOf(2), classes.get("InstrumentAudio"));
    assertEquals(Integer.valueOf(6), classes.get("InstrumentAudioChord"));
    assertEquals(Integer.valueOf(4), classes.get("InstrumentAudioEvent"));
    assertEquals(Integer.valueOf(3), classes.get("InstrumentMeme"));
    assertEquals(Integer.valueOf(4), classes.get("ProgramSequencePatternEvent"));
    assertEquals(Integer.valueOf(3), classes.get("ProgramMeme"));
    assertEquals(Integer.valueOf(1), classes.get("ProgramSequencePattern"));
    assertEquals(Integer.valueOf(2), classes.get("ProgramSequence"));
    assertEquals(Integer.valueOf(6), classes.get("ProgramSequenceBinding"));
    assertEquals(Integer.valueOf(8), classes.get("ProgramSequenceBindingMeme"));
    assertEquals(Integer.valueOf(6), classes.get("ProgramSequenceChord"));
    assertEquals(Integer.valueOf(2), classes.get("ProgramVoiceTrack"));
    assertEquals(Integer.valueOf(1), classes.get("ProgramVoice"));
    assertEquals(Integer.valueOf(2), classes.get("Instrument"));
  }

  @Test
  public void toStringOutput() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(ChainBinding.create(fake.chain3, fake.library10000001)));

    assertEquals("2 Instrument, 2 InstrumentAudio, 6 InstrumentAudioChord, 4 InstrumentAudioEvent, 3 InstrumentMeme, 3 Program, 3 ProgramMeme, 2 ProgramSequence, 6 ProgramSequenceBinding, 8 ProgramSequenceBindingMeme, 6 ProgramSequenceChord, 1 ProgramSequencePattern, 4 ProgramSequencePatternEvent, 1 ProgramVoice, 2 ProgramVoiceTrack", ingest.toString());
  }

}
