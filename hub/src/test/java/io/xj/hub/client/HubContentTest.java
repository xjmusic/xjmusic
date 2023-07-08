// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.client;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.UUID;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class HubContentTest {
  HubContent subject;
  ProgramSequencePatternEvent program702_pattern901_boomEvent;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @BeforeEach
  public void setUp() throws Exception {
    // account
    Account account1 = buildAccount("testing");

    // Library content all created at this known time
    Library library10000001 = buildLibrary(account1, "leaves");

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    Template template1 = buildTemplate(account1, "test", UUID.randomUUID().toString());
    TemplateBinding templateBinding1 = buildTemplateBinding(template1, library10000001);

    // Instrument 201
    Instrument instrument201 = buildInstrument(library10000001, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums");
    InstrumentMeme instrument201_meme0 = buildInstrumentMeme(instrument201, "Ants");
    InstrumentAudio instrument201_audio402 = buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    Program program701 = buildProgram(library10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4f, 0.6f);
    ProgramMeme program701_meme0 = buildProgramMeme(program701, "Ants");
    ProgramSequence sequence902 = buildProgramSequence(program701, (short) 16, "decay", 0.25f, "F#");
    ProgramSequenceChord sequence902_chord0 = buildProgramSequenceChord(sequence902, 0.0, "G minor");
    ProgramSequenceBinding binding902_0 = buildProgramSequenceBinding(sequence902, 0);
    ProgramSequenceBinding sequence902_binding0 = buildProgramSequenceBinding(sequence902, 5);
    ProgramSequenceBindingMeme sequence902_binding0_meme0 = buildProgramSequenceBindingMeme(binding902_0, "Gravel");

    // Program 702, beat-type, has unbound sequence with pattern with events
    Program program702 = buildProgram(library10000001, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f);
    ProgramMeme program702_meme0 = buildProgramMeme(program702, "Ants");
    ProgramVoice program702_voice1 = buildProgramVoice(program702, InstrumentType.Drum, "Drums");
    ProgramSequence sequence702a = buildProgramSequence(program702, (short) 16, "Base", 0.5f, "C");
    ProgramSequencePattern pattern901 = buildProgramSequencePattern(sequence702a, program702_voice1, (short) 16, "growth");
    ProgramVoiceTrack trackBoom = buildProgramVoiceTrack(program702_voice1, "BOOM");
    ProgramVoiceTrack trackSmack = buildProgramVoiceTrack(program702_voice1, "BOOM");
    program702_pattern901_boomEvent = buildProgramSequencePatternEvent(pattern901, trackBoom, 0.0f, 1.0f, "C", 1.0f);

    // ingest all content
    subject = new HubContent(List.of(
      library10000001,
      template1,
      templateBinding1,
      instrument201,
      instrument201_meme0,
      instrument201_audio402,
      program701,
      program701_meme0,
      sequence902,
      sequence902_chord0,
      binding902_0,
      sequence902_binding0,
      sequence902_binding0_meme0,
      program702,
      program702_meme0,
      program702_voice1,
      sequence702a,
      pattern901,
      trackBoom,
      trackSmack,
      program702_pattern901_boomEvent
    ));
  }

  @Test
  public void getInstrumentTypeForEvent() throws HubClientException {
    assertEquals(InstrumentType.Drum, subject.getInstrumentTypeForEvent(program702_pattern901_boomEvent));
  }

  @Test
  public void hasInstruments() {
    assertTrue(subject.hasInstruments(InstrumentType.Drum));
    assertFalse(subject.hasInstruments(InstrumentType.Percussion));
    assertFalse(subject.hasInstruments(InstrumentType.Bass));
    assertFalse(subject.hasInstruments(InstrumentType.Pad));
    assertFalse(subject.hasInstruments(InstrumentType.Sticky));
    assertFalse(subject.hasInstruments(InstrumentType.Stripe));
    assertFalse(subject.hasInstruments(InstrumentType.Stab));
    assertFalse(subject.hasInstruments(InstrumentMode.Transition));
    assertFalse(subject.hasInstruments(InstrumentMode.Background));
    assertFalse(subject.hasInstruments(InstrumentType.Hook));
  }
}
