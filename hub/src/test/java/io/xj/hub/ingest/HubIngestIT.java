// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.common.collect.Maps;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.*;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.manager.*;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.util.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.Mock;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
public class HubIngestIT {

  private HubIngestFactory ingestFactory;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;
  @Mock
  private PreviewNexusAdmin previewNexusAdmin;

  private static Map<String, Integer> classTally(Collection<Object> allEntities) {
    Map<String, Integer> out = Maps.newHashMap();
    allEntities.forEach(entity -> {
      String name = Text.getSimpleName(entity);
      out.put(name, out.containsKey(name) ? out.get(name) + 1 : 1);
    });
    return out;
  }

  @BeforeEach
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();
    InstrumentManager instrumentManager = new InstrumentManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    ProgramManager programManager = new ProgramManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    TemplateBindingManager templateBindingManager = new TemplateBindingManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    TemplatePlaybackManager templatePlaybackManager = new TemplatePlaybackManagerImpl(env, test.getEntityFactory(), test.getSqlStoreProvider(), previewNexusAdmin);
    TemplatePublicationManager templatePublicationManager = new TemplatePublicationManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    TemplateManager templateManager = new TemplateManagerImpl(test.getEnv(), test.getEntityFactory(), test.getSqlStoreProvider(), templateBindingManager, templatePlaybackManager, templatePublicationManager);
    ingestFactory = new HubIngestFactoryImpl(test.getJsonProvider(), test.getEntityStore(), instrumentManager, programManager, templateManager, templateBindingManager);
  }

  @AfterEach
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
