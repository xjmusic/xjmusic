// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.*;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.util.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HubIngestIT {

  private HubIngestFactory ingestFactory;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private static Map<String, Integer> classTally(Collection<Object> allEntities) {
    Map<String, Integer> out = Maps.newHashMap();
    allEntities.forEach(entity -> {
      String name = Text.getSimpleName(entity);
      out.put(name, out.containsKey(name) ? out.get(name) + 1 : 1);
    });
    return out;
  }

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();
    ingestFactory = injector.getInstance(HubIngestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void ingest() throws Exception {
    HubIngest result = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(2, result.getAllInstruments().size());
  }

  @Test
  public void getAllPrograms() throws Exception {
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(3, ingest.getAllPrograms().size());
  }

  @Test
  public void getAllPrograms_fromTwoDifferentLibraries() throws Exception {
    test.insert(buildTemplateBinding(fake.template1, fake.library10000002));

    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(4, ingest.getAllPrograms().size());
  }

  @Test
  public void getProgramsOfType() throws Exception {
    test.insert(buildProgram(fake.library10000001, ProgramType.Beat, ProgramState.Published, "cups", "B", 120.4f, 0.6f));
    test.insert(buildProgram(fake.library10000001, ProgramType.Main, ProgramState.Published, "plates", "Bb", 120.4f, 0.6f));
    test.insert(buildProgram(fake.library10000001, ProgramType.Detail, ProgramState.Published, "bowls", "A", 120.4f, 0.6f));
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(3, ingest.getProgramsOfType(ProgramType.Main).size());
    assertEquals(2, ingest.getProgramsOfType(ProgramType.Beat).size());
    assertEquals(1, ingest.getProgramsOfType(ProgramType.Detail).size());
  }

  @Test
  public void access() throws Exception {
    HubAccess access = HubAccess.internal();
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertArrayEquals(access.getRoleTypes().toArray(), ingest.getAccess().getRoleTypes().toArray());
  }

  @Test
  public void getProgram() throws Exception {
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals("leaves", ingest.getProgram(fake.program701.getId()).getName());
    assertEquals("coconuts", ingest.getProgram(fake.program702.getId()).getName());
    assertEquals("bananas", ingest.getProgram(fake.program703.getId()).getName());
  }

  @Test
  public void getProgram_exceptionOnMissing() {
    var e = assertThrows(HubIngestException.class, () -> {
      HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());
      ingest.getProgram(UUID.randomUUID());
    });
    assertEquals("No such Program", e.getMessage().substring(0, 15));
  }

  @Test
  public void getAllInstruments() throws Exception {
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(2, ingest.getAllInstruments().size());
  }

  @Test
  public void getInstrumentsOfType() throws Exception {
    test.insert(buildInstrument(fake.library10000001, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "Dreamy"));
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals(2, ingest.getInstrumentsOfType(InstrumentType.Drum).size());
    assertEquals(1, ingest.getInstrumentsOfType(InstrumentType.Pad).size());
  }

  @Test
  public void getInstrument() throws Exception {
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    assertEquals("808 Drums", ingest.getInstrument(fake.instrument201.getId()).getName());
  }

  @Test
  public void getAllEntities() throws Exception {
    HubIngest ingest = ingestFactory.ingest(HubAccess.internal(), fake.template1.getId());

    Collection<Object> result = ingest.getAllEntities();

    assertEquals(45, result.size());
    Map<String, Integer> classes = classTally(result);
    assertEquals(Integer.valueOf(1), classes.get("Template"));
    assertEquals(Integer.valueOf(1), classes.get("TemplateBinding"));
    assertEquals(Integer.valueOf(3), classes.get("Program"));
    assertEquals(Integer.valueOf(2), classes.get("InstrumentAudio"));
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

}
