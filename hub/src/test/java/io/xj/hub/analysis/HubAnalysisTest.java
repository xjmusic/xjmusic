// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.analysis;


import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.ingest.HubIngest;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrument;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentAudio;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBindingMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChord;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequencePattern;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequencePatternEvent;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoiceTrack;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HubAnalysisTest {
  @Mock
  HubIngestFactory hubIngestFactory;
  @Mock
  HubIngest hubIngest;
  @MockBean
  NotificationProvider notificationProvider;
  @MockBean
  FileStoreProvider fileStoreProvider;
  @MockBean
  HttpClientProvider httpClientProvider;
  Template template;
  HubAccess access;

  @BeforeEach
  public void setUp() throws Exception {
    // account
    Account account1 = buildAccount("testing");

    // Library content all created at this known time
    Library library1 = buildLibrary(account1, "leaves");

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    TemplateBinding templateBinding_library1 = buildTemplateBinding(template, library1);

    // Instrument 201
    Instrument instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums");
    InstrumentMeme instrument1_meme1 = buildInstrumentMeme(instrument1, "Ants");
    InstrumentAudio instrument1_audio1 = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "X", 1.0f);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    Program program1 = buildProgram(library1, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4f, 0.6f);
    ProgramMeme program1_meme1 = buildProgramMeme(program1, "Ants");
    ProgramSequence program1_sequence1 = buildProgramSequence(program1, (short) 16, "decay", 0.25f, "F#");
    ProgramSequenceChord program1_sequence1_chord0 = buildProgramSequenceChord(program1_sequence1, 0.0, "G minor");
    ProgramSequenceBinding program1_binding1 = buildProgramSequenceBinding(program1_sequence1, 0);
    ProgramSequenceBinding program1_binding2 = buildProgramSequenceBinding(program1_sequence1, 5);
    ProgramSequenceBindingMeme program1_binding1_meme0 = buildProgramSequenceBindingMeme(program1_binding1, "Gravel");

    // Program 702, beat-type, has unbound sequence with pattern with events
    Program program2 = buildProgram(library1, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f);
    ProgramMeme program2_meme1 = buildProgramMeme(program2, "Ants");
    ProgramVoice program2_voice1 = buildProgramVoice(program2, InstrumentType.Drum, "Drums");
    ProgramSequence program2_sequence1 = buildProgramSequence(program2, (short) 16, "Base", 0.5f, "C");
    ProgramSequencePattern program2_sequence1_pattern1 = buildProgramSequencePattern(program2_sequence1, program2_voice1, (short) 16, "growth");
    ProgramVoiceTrack program2_trackBoom = buildProgramVoiceTrack(program2_voice1, "BOOM");
    ProgramVoiceTrack program2_trackSmack = buildProgramVoiceTrack(program2_voice1, "BOOM");
    ProgramSequencePatternEvent program2_sequence1_pattern1_event1 = buildProgramSequencePatternEvent(program2_sequence1_pattern1, program2_trackBoom, 0.0f, 1.0f, "C", 1.0f);

    List<Object> entities = List.of(
      template,
      templateBinding_library1,
      library1,
      instrument1,
      instrument1_meme1,
      instrument1_audio1,
      program1,
      program1_binding1,
      program1_binding1_meme0,
      program1_binding2,
      program1_meme1,
      program1_sequence1,
      program1_sequence1_chord0,
      program2,
      program2_meme1,
      program2_sequence1,
      program2_sequence1_pattern1,
      program2_sequence1_pattern1_event1,
      program2_trackBoom,
      program2_trackSmack,
      program2_voice1
    );

    access = HubAccess.internal();

    when(hubIngestFactory.ingest(same(access), eq(template.getId()))).thenReturn(hubIngest);
    when(hubIngest.getAllEntities()).thenReturn(entities);
  }

  @Test
  public void analysis_toHTML() throws Exception {
    Report subject = new HubAnalysisFactoryImpl(hubIngestFactory).report(access, template.getId(), Report.Type.Memes);

    var result = subject.toHTML();

    assertTrue(result.contains("<!DOCTYPE html>"));
    assertTrue(result.contains("<html lang=\"en\">"));
    assertTrue(result.contains("<meta charset=\"UTF-8\">"));
    assertTrue(result.contains("<title>Content Analysis</title>"));
    assertTrue(result.contains("Memes"));
  }

}
