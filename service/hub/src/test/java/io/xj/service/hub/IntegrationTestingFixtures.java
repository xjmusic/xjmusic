// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentAudio;
import io.xj.service.hub.entity.InstrumentAudioChord;
import io.xj.service.hub.entity.InstrumentAudioEvent;
import io.xj.service.hub.entity.InstrumentMeme;
import io.xj.service.hub.entity.InstrumentState;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramMeme;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequenceBinding;
import io.xj.service.hub.entity.ProgramSequenceBindingMeme;
import io.xj.service.hub.entity.ProgramSequenceChord;
import io.xj.service.hub.entity.ProgramSequencePattern;
import io.xj.service.hub.entity.ProgramSequencePatternEvent;
import io.xj.service.hub.entity.ProgramSequencePatternType;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.hub.entity.ProgramVoice;
import io.xj.service.hub.entity.ProgramVoiceTrack;
import io.xj.service.hub.entity.User;
import io.xj.service.hub.entity.UserRole;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class IntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(IntegrationTestingFixtures.class);
  private final HubIntegrationTestProvider test;

  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public Instrument instrument201;
  public Instrument instrument202;
  public Instrument instrument251;
  public Instrument instrument8;
  public Instrument instrument9;
  public InstrumentAudio audio1;
  public InstrumentAudio audio2;
  public InstrumentAudio audio401;
  public InstrumentAudio audio8bleep;
  public InstrumentAudio audio8kick;
  public InstrumentAudio audio8snare;
  public InstrumentAudio audio8toot;
  public InstrumentAudio instrument201_audio402;
  public InstrumentAudioChord audioChord1;
  public InstrumentAudioEvent audioEvent1;
  public InstrumentAudioEvent audioEvent2;
  public InstrumentAudioEvent audioEvent401a;
  public Library library10000001;
  public Library library10000002;
  public Library library1;
  public Library library1a;
  public Library library1b;
  public Library library2;
  public Library library2a;
  public Library library2b;
  public Program program15;
  public Program program1;
  public Program program2;
  public Program program35;
  public Program program3;
  public Program program4;
  public Program program701;
  public Program program702;
  public Program program703;
  public Program program751;
  public Program program9;
  public ProgramMeme program701_meme0;
  public ProgramMeme programMeme1;
  public ProgramMeme programMeme35;
  public ProgramMeme programMeme3;
  public ProgramSequence program1_sequence1;
  public ProgramSequence program3_sequence1;
  public ProgramSequence programSequence35;
  public ProgramSequenceBinding program15_binding0;
  public ProgramSequenceBinding program15_binding1;
  public ProgramSequenceBinding program3_binding0;
  public ProgramSequenceBinding program3_binding1;
  public ProgramSequenceChord program3_chord1;
  public ProgramSequencePattern program2_sequence1_pattern1;
  public ProgramSequencePatternEvent program2_sequence1_pattern1_event0;
  public ProgramSequencePatternEvent program2_sequence1_pattern1_event1;
  public ProgramSequencePatternEvent program702_pattern901_boomEvent;
  public ProgramVoice program2_voice1;
  public ProgramVoiceTrack program2_voice1_track0;
  public ProgramVoiceTrack program2_voice1_track1;
  public User user101;
  public User user1;
  public User user2;
  public User user3;
  public User user4;
  public User user53;
  public User user5;
  //
  public HubContentFixtures content;


  /**
   Create a new Integration Testing Fixtures instance by providing the integration test provider
   */
  public IntegrationTestingFixtures(HubIntegrationTestProvider hubIntegrationTestProvider) {
    test = hubIntegrationTestProvider;
    content = new HubContentFixtures();
  }

  /**
   Create a new Integration Testing Fixtures instance by providing the integration test provider and content fixtures
   */
  public IntegrationTestingFixtures(HubIntegrationTestProvider hubIntegrationTestProvider, HubContentFixtures content) {
    test = hubIntegrationTestProvider;
    this.content = content;
  }

  /**
   Library of Content A (shared test fixture)
   */
  public void insertFixtureA() throws HubException, JsonApiException {
    // account
    account1 = test.insert(Account.create("testing"));
    user101 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(user101, UserRoleType.Admin));

    // Library content all created at this known time
    Instant at = Instant.parse("2014-08-12T12:17:02.527142Z");
    library10000001 = test.insert(Library.create(account1, "leaves", at));

    // Instrument 201
    instrument201 = test.insert(Instrument.create(user101, library10000001, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(instrument201, "Ants"));
    test.insert(InstrumentMeme.create(instrument201, "Mold"));
    //
    instrument201_audio402 = test.insert(InstrumentAudio.create(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 0.0, "E minor"));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 4.0, "A major"));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 8.0, "B minor"));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 12.0, "F# major"));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 16.0, "Ab7"));
    test.insert(InstrumentAudioChord.create(instrument201_audio402, 20.0, "Bb7"));
    //
    InstrumentAudio audio401 = test.insert(InstrumentAudio.create(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    test.insert(InstrumentAudioEvent.create(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    test.insert(InstrumentAudioEvent.create(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    test.insert(InstrumentAudioEvent.create(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    test.insert(InstrumentAudioEvent.create(audio401, 3.0, 1.0, "SNARE", "B", 0.8));

    // Instrument 202
    instrument202 = test.insert(Instrument.create(user101, library10000001, InstrumentType.Percussive, InstrumentState.Published, "909 Drums"));
    test.insert(InstrumentMeme.create(instrument202, "Peel"));

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program701 = test.insert(Program.create(user101, library10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, 0.6));
    program701_meme0 = test.insert(ProgramMeme.create(program701, "Ants"));
    ProgramSequence sequence902 = test.insert(ProgramSequence.create(program701, 16, "decay", 0.25, "F#", 110.3));
    test.insert(ProgramSequenceChord.create(sequence902, 0.0, "G minor"));
    test.insert(ProgramSequenceChord.create(sequence902, 4.0, "C major"));
    test.insert(ProgramSequenceChord.create(sequence902, 8.0, "F7"));
    test.insert(ProgramSequenceChord.create(sequence902, 12.0, "G7"));
    test.insert(ProgramSequenceChord.create(sequence902, 16.0, "F minor"));
    test.insert(ProgramSequenceChord.create(sequence902, 20.0, "Bb major"));
    ProgramSequenceBinding binding902_0 = test.insert(ProgramSequenceBinding.create(sequence902, 0));
    ProgramSequenceBinding binding902_1 = test.insert(ProgramSequenceBinding.create(sequence902, 1));
    ProgramSequenceBinding binding902_2 = test.insert(ProgramSequenceBinding.create(sequence902, 2));
    ProgramSequenceBinding binding902_3 = test.insert(ProgramSequenceBinding.create(sequence902, 3));
    ProgramSequenceBinding binding902_4 = test.insert(ProgramSequenceBinding.create(sequence902, 4));
    test.insert(ProgramSequenceBinding.create(sequence902, 5));
    test.insert(ProgramSequenceBindingMeme.create(binding902_0, "Gravel"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_1, "Gravel"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_2, "Gravel"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_3, "Rocks"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_1, "Fuzz"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_2, "Fuzz"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_3, "Fuzz"));
    test.insert(ProgramSequenceBindingMeme.create(binding902_4, "Noise"));

    // Program 702, rhythm-type, has unbound sequence with pattern with events
    program702 = test.insert(Program.create(user101, library10000001, ProgramType.Rhythm, ProgramState.Published, "coconuts", "F#", 110.3, 0.6));
    test.insert(ProgramMeme.create(program702, "Ants"));
    program2_voice1 = test.insert(ProgramVoice.create(program702, InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence702a = test.insert(ProgramSequence.create(program702, 16, "Base", 0.5, "C", 110.3));
    ProgramSequencePattern pattern901 = test.insert(ProgramSequencePattern.create(sequence702a, program2_voice1, ProgramSequencePatternType.Loop, 16, "growth"));
    ProgramVoiceTrack trackBoom = test.insert(ProgramVoiceTrack.create(program2_voice1, "BOOM"));
    ProgramVoiceTrack trackSmack = test.insert(ProgramVoiceTrack.create(program2_voice1, "BOOM"));
    program702_pattern901_boomEvent = test.insert(ProgramSequencePatternEvent.create(pattern901, trackBoom, 0.0, 1.0, "C", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern901, trackSmack, 1.0, 1.0, "G", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern901, trackBoom, 2.5, 1.0, "C", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern901, trackSmack, 3.0, 1.0, "G", 0.9));

    // Program 703
    program703 = test.insert(Program.create(user101, library10000001, ProgramType.Main, ProgramState.Published, "bananas", "Gb", 100.6, 0.6));
    test.insert(ProgramMeme.create(program703, "Peel"));

    // DELIBERATELY UNUSED stuff that should not get used because it's in a different library
    library10000002 = test.insert(Library.create(account1, "Garbage Library", at));
    //
    instrument251 = test.insert(Instrument.create(user101, library10000002, InstrumentType.Percussive, InstrumentState.Published, "Garbage Instrument"));
    test.insert(InstrumentMeme.create(instrument251, "Garbage MemeEntity"));
    //
    program751 = test.insert(Program.create(user101, library10000002, ProgramType.Rhythm, ProgramState.Published, "coconuts", "F#", 110.3, 0.6));
    test.insert(ProgramMeme.create(program751, "Ants"));
    ProgramVoice voiceGarbage = test.insert(ProgramVoice.create(program751, InstrumentType.Percussive, "Garbage"));
    ProgramSequence sequence751a = test.insert(ProgramSequence.create(program751, 16, "Base", 0.5, "C", 110.3));
    ProgramSequencePattern pattern951 = test.insert(ProgramSequencePattern.create(sequence751a, voiceGarbage, ProgramSequencePatternType.Loop, 16, "Garbage"));
    ProgramVoiceTrack trackGr = test.insert(ProgramVoiceTrack.create(voiceGarbage, "GR"));
    ProgramVoiceTrack trackBag = test.insert(ProgramVoiceTrack.create(voiceGarbage, "BAG"));
    test.insert(ProgramSequencePatternEvent.create(pattern951, trackGr, 0.0, 1.0, "C", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern951, trackBag, 1.0, 1.0, "G", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern951, trackGr, 2.5, 1.0, "C", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern951, trackBag, 3.0, 1.0, "G", 0.9));
  }

  /**
   Library of Content B-1 (shared test fixture)
   */
  public void insertFixtureB1() throws HubException {
    Collection<Entity> entities = content.setupFixtureB1(true);
    for (Entity entity : entities) {
      test.insert(entity);
    }
  }

  /**
   Library of Content B-1 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB2() throws HubException {
    Collection<Entity> entities = content.setupFixtureB2();
    for (Entity entity : entities) {
      test.insert(entity);
    }
  }

  /**
   Library of Content B-3 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   <p>
   [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   */
  public void insertFixtureB3() throws HubException {
    Collection<Entity> entities = content.setupFixtureB3();
    for (Entity entity : entities) {
      test.insert(entity);
    }
  }

  /**
   Library of Content B: Instruments (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB_Instruments() throws HubException, JsonApiException {
    instrument201 = test.insert(Instrument.create(user3, library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(instrument201, "Ants"));
    test.insert(InstrumentMeme.create(instrument201, "Mold"));
    //
    audio401 = test.insert(InstrumentAudio.create(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    audioEvent401a = test.insert(InstrumentAudioEvent.create(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    test.insert(InstrumentAudioEvent.create(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    test.insert(InstrumentAudioEvent.create(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    test.insert(InstrumentAudioEvent.create(audio401, 3.0, 1.0, "SNARE", "B", 0.8));
    //
    InstrumentAudio audio402 = test.insert(InstrumentAudio.create(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    test.insert(InstrumentAudioChord.create(audio402, 0.0, "E minor"));
    test.insert(InstrumentAudioChord.create(audio402, 4.0, "A major"));
    test.insert(InstrumentAudioChord.create(audio402, 8.0, "B minor"));
    test.insert(InstrumentAudioChord.create(audio402, 12.0, "F# major"));
    test.insert(InstrumentAudioChord.create(audio402, 16.0, "Ab7"));
    test.insert(InstrumentAudioChord.create(audio402, 20.0, "Bb7"));
  }
}
