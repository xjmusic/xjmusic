//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.util.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class IngestIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private IngestFactory ingestFactory;

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
    reset();
    insertFixtureA();
    ingestFactory = injector.getInstance(IngestFactory.class);
  }

  @Test
  public void ingest() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Ingest result = ingestFactory.ingest(access, ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, result.getAllInstruments().size());
  }

  @Test
  public void getAllPrograms() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));
    assertEquals(3, ingest.getAllPrograms().size());
  }

  @Test
  public void getProgramsOfType() throws Exception {
    insert(newProgram(711, 101, 10000001, ProgramType.Rhythm, ProgramState.Published, "cups", "B", 120.4, now()));
    insert(newProgram(712, 101, 10000001, ProgramType.Main, ProgramState.Published, "plates", "Bb", 120.4, now()));
    insert(newProgram(715, 101, 10000001, ProgramType.Detail, ProgramState.Published, "bowls", "A", 120.4, now()));
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(3, ingest.getProgramsOfType(ProgramType.Main).size());
    assertEquals(2, ingest.getProgramsOfType(ProgramType.Rhythm).size());
    assertEquals(1, ingest.getProgramsOfType(ProgramType.Detail).size());
  }

  @Test
  public void access() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Ingest ingest = ingestFactory.ingest(access, ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(access, ingest.getAccess());
  }

  @Test
  public void getProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals("leaves", ingest.getProgram(BigInteger.valueOf(701)).getName());
    assertEquals("coconuts", ingest.getProgram(BigInteger.valueOf(702)).getName());
    assertEquals("bananas", ingest.getProgram(BigInteger.valueOf(703)).getName());
  }

  @Test
  public void getProgram_exceptionOnMissing() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Cannot fetch entity");
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    ingest.getProgram(BigInteger.valueOf(79972));
  }

  @Test
  public void getAllInstruments() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getAllInstruments().size());
  }

  @Test
  public void getInstrumentsOfType() throws Exception {
    insert(newInstrument(1201, 101, 10000001, InstrumentType.Harmonic, InstrumentState.Published, "Dreamy", now()));
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getInstrumentsOfType(InstrumentType.Percussive).size());
    assertEquals(1, ingest.getInstrumentsOfType(InstrumentType.Harmonic).size());
  }

  @Test
  public void getInstrument() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals("808 Drums", ingest.getInstrument(BigInteger.valueOf(201)).getDescription());
  }

  @Test
  public void getAllLibraries() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(1, ingest.getAllLibraries().size());
  }

  @Test
  public void getAllEntities() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    Collection<Entity> result = ingest.getAllEntities();

    assertEquals(6, result.size());
    Map<String, Integer> classes = classTally(result);
    assertEquals(Integer.valueOf(1), classes.get("Library"));
    assertEquals(Integer.valueOf(2), classes.get("Instrument"));
    assertEquals(Integer.valueOf(3), classes.get("Program"));
  }

  @Test
  public void toStringOutput() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals("3 Program, 2 Instrument, 1 Library", ingest.toString());
  }

}
