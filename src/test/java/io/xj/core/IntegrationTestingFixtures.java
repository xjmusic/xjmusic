// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramPatternType;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentState;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.testing.InternalResources;
import io.xj.core.util.Text;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.core.Tables.PROGRAM_VOICE;
import static io.xj.core.Tables.PROGRAM_VOICE_TRACK;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class IntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(IntegrationTestingFixtures.class);
  private final IntegrationTestProvider test;

  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public Chain chain1;
  public Chain chain2;
  public Chain chain3;
  public Chain chain5;
  public FileInputStream audioStreamOne;
  public FileInputStream audioStreamTwo;
  public Instrument instrument1;
  public Instrument instrument201;
  public Instrument instrument202;
  public Instrument instrument251;
  public Instrument instrument8;
  public Instrument instrument9;
  public InstrumentAudio audio1;
  public InstrumentAudio audio2;
  public InstrumentAudio audio8bleep;
  public InstrumentAudio audio8kick;
  public InstrumentAudio audio8snare;
  public InstrumentAudio audio8toot;
  public InstrumentAudio audioHihat;
  public InstrumentAudio audioKick;
  public InstrumentAudio audioSnare;
  public InstrumentAudio instrument201_audio402;
  public InstrumentAudioChord audioChord1;
  public InstrumentAudioEvent audioEvent1;
  public InstrumentAudioEvent audioEvent2;
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
  public Program program25;
  public Program program2;
  public Program program35;
  public Program program3;
  public Program program4;
  public Program program5;
  public Program program6;
  public Program program701;
  public Program program702;
  public Program program703;
  public Program program751;
  public Program program7;
  public Program program9;
  public ProgramMeme program701_meme0;
  public ProgramSequence programSequence1;
  public ProgramSequence programSequence3;
  public ProgramSequenceBinding program15_binding0;
  public ProgramSequenceBinding program15_binding1;
  public ProgramSequenceBinding program3_binding0;
  public ProgramSequenceBinding program3_binding1;
  public ProgramSequenceBinding program4_binding0;
  public ProgramSequenceBinding program4_binding1;
  public ProgramSequenceBinding program4_binding2;
  public ProgramSequenceBinding program5_binding0;
  public ProgramSequenceBinding program5_binding1;
  public ProgramSequencePatternEvent program702_pattern901_boomEvent;
  public ProgramVoice voiceDrums;
  public Segment segment0;
  public Segment segment17;
  public Segment segment1;
  public Segment segment2;
  public Segment segment3;
  public Segment segment4;
  public Segment segment5;
  public Segment segment6;
  public User user101;
  public User user1;
  public User user2;
  public User user3;
  public User user4;
  public User user53;
  public User user5;

  /**
   Create a new Integration Testing Fixtures instance by providing the integration test provider
   */
  public IntegrationTestingFixtures(IntegrationTestProvider integrationTestProvider) {
    test = integrationTestProvider;
  }

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  protected static ProgramPatternType randomRhythmPatternType() {
    return new ProgramPatternType[]{
      ProgramPatternType.Intro,
      ProgramPatternType.Loop,
      ProgramPatternType.Outro
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  protected static Double[] listOfRandomValues(int N, double from, double to) {
    Double[] result = new Double[N];
    for (int i = 0; i < N; i++) {
      result[i] = random(from, to);
    }
    return result;
  }

  /**
   Create a N-magnitude list of unique Strings at random of a source list of Strings

   @param N           size of list
   @param sourceItems source Strings
   @return list of unique random Strings
   */
  protected static String[] listOfUniqueRandom(long N, String[] sourceItems) {
    long count = 0;
    Collection<String> items = Lists.newArrayList();
    while (count < N) {
      String p = random(sourceItems);
      if (!items.contains(p)) {
        items.add(p);
        count++;
      }
    }
    return items.toArray(new String[0]);
  }

  /**
   Random value between A and B

   @param A floor
   @param B ceiling
   @return A <= value <= B
   */
  protected static Double random(double A, double B) {
    return A + StrictMath.random() * (B - A);
  }

  /**
   Get random String of array

   @param array to get String of
   @return random String
   */
  protected static String random(String[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Get random long of array

   @param array to get long of
   @return random long
   */
  protected static Integer random(Integer[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Library of Content A (shared test fixture)
   */
  public void insertFixtureA() throws CoreException {
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
    voiceDrums = test.insert(ProgramVoice.create(program702, InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence702a = test.insert(ProgramSequence.create(program702, 16, "Base", 0.5, "C", 110.3));
    ProgramSequencePattern pattern901 = test.insert(ProgramSequencePattern.create(sequence702a, voiceDrums, ProgramPatternType.Loop, 16, "growth"));
    ProgramVoiceTrack trackBoom = test.insert(ProgramVoiceTrack.create(voiceDrums, "BOOM"));
    ProgramVoiceTrack trackSmack = test.insert(ProgramVoiceTrack.create(voiceDrums, "BOOM"));
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
    ProgramSequencePattern pattern951 = test.insert(ProgramSequencePattern.create(sequence751a, voiceGarbage, ProgramPatternType.Loop, 16, "Garbage"));
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
  public void insertFixtureB1() throws CoreException {
    // Account "bananas"
    account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(user3, UserRoleType.User));
    test.insert(AccountUser.create(account1, user3));

    // Library "house"
    library2 = test.insert(Library.create(account1, "house", InternalResources.now()));

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = test.insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", "C", 120.0, 0.6));
    test.insert(ProgramMeme.create(program4, "Tropical"));
    //
    ProgramSequence sequence4a = test.insert(ProgramSequence.create(program4, 0, "Start Wild", 0.6, "C", 125.0));
    program4_binding0 = test.insert(ProgramSequenceBinding.create(sequence4a, 0));
    test.insert(ProgramSequenceBindingMeme.create(program4_binding0, "Wild"));
    //
    ProgramSequence sequence4b = test.insert(ProgramSequence.create(program4, 0, "Intermediate", 0.4, "Bb minor", 115.0));
    program4_binding1 = test.insert(ProgramSequenceBinding.create(sequence4b, 1));
    test.insert(ProgramSequenceBindingMeme.create(program4_binding1, "Cozy"));
    test.insert(ProgramSequenceBindingMeme.create(program4_binding1, "Wild"));
    //
    ProgramSequence sequence4c = test.insert(ProgramSequence.create(program4, 0, "Finish Cozy", 0.4, "Ab minor", 125.0));
    program4_binding2 = test.insert(ProgramSequenceBinding.create(sequence4c, 2));
    test.insert(ProgramSequenceBindingMeme.create(program4_binding2, "Cozy"));

    // Main program
    program5 = test.insert(Program.create(user3, library2, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140, 0.6));
    test.insert(ProgramMeme.create(program5, "Outlook"));
    //
    ProgramSequence sequence5a = test.insert(ProgramSequence.create(program5, 16, "Intro", 0.5, "G major", 135.0));
    test.insert(ProgramSequenceChord.create(sequence5a, 0.0, "G major"));
    test.insert(ProgramSequenceChord.create(sequence5a, 8.0, "Ab minor"));
    test.insert(ProgramSequenceChord.create(sequence5a, 75.0, "G-9")); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total
    program5_binding0 = test.insert(ProgramSequenceBinding.create(sequence5a, 0));
    test.insert(ProgramSequenceBindingMeme.create(program5_binding0, "Optimism"));
    //
    ProgramSequence sequence5b = test.insert(ProgramSequence.create(program5, 32, "Drop", 0.5, "G minor", 135.0));
    test.insert(ProgramSequenceChord.create(sequence5b, 0.0, "C major"));
    test.insert(ProgramSequenceChord.create(sequence5b, 8.0, "Bb minor"));
    program5_binding1 = test.insert(ProgramSequenceBinding.create(sequence5b, 1));
    test.insert(ProgramSequenceBindingMeme.create(program5_binding1, "Pessimism"));

    // A basic beat
    program35 = test.insert(Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6));
    test.insert(ProgramMeme.create(program35, "Basic"));
    voiceDrums = test.insert(ProgramVoice.create(program35, InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence35a = test.insert(ProgramSequence.create(program35, 16, "Base", 0.5, "C", 110.3));
    //
    ProgramSequencePattern pattern35a1 = test.insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop"));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "CLOCK")), 0.0, 1.0, "C2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNORT")), 1.0, 1.0, "G5", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "KICK")), 2.5, 1.0, "C2", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNARL")), 3.0, 1.0, "G5", 0.9));
    //
    ProgramSequencePattern pattern35a2 = test.insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop Alt"));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "CLACK")), 0.0, 1.0, "B5", 0.9));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNARn")), 1.0, 1.0, "D2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "CLICK")), 2.5, 1.0, "E4", 0.7));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNAP")), 3.0, 1.0, "c3", 0.5));

    // Detail Sequence
    program6 = test.insert(Program.create(user3, library2, ProgramType.Detail, ProgramState.Published, "Beat Jam", "D#", 150, 0.6));
    program7 = test.insert(Program.create(user3, library2, ProgramType.Detail, ProgramState.Published, "Detail Jam", "Cb minor", 170, 0.6));
  }

  /**
   Library of Content B-2 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB2() throws CoreException {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = test.insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Tangy, Chunky to Smooth", "G minor", 120.0, 0.6));
    test.insert(ProgramMeme.create(program3, "Tangy"));
    //
    ProgramSequence sequence3a = test.insert(ProgramSequence.create(program3, 0, "Start Chunky", 0.4, "G minor", 115.0));
    program3_binding0 = test.insert(ProgramSequenceBinding.create(sequence3a, 0));
    test.insert(ProgramSequenceBindingMeme.create(program3_binding0, "Chunky"));
    //
    ProgramSequence sequence3b = test.insert(ProgramSequence.create(program3, 0, "Finish Smooth", 0.6, "C", 125.0));
    program3_binding1 = test.insert(ProgramSequenceBinding.create(sequence3b, 1));
    test.insert(ProgramSequenceBindingMeme.create(program3_binding1, "Smooth"));

    // Main program
    program15 = test.insert(Program.create(user3, library2, ProgramType.Main, ProgramState.Published, "Next Jam", "Db minor", 140, 0.6));
    test.insert(ProgramMeme.create(program15, "Hindsight"));
    //
    ProgramSequence sequence15a = test.insert(ProgramSequence.create(program15, 16, "Intro", 0.5, "G minor", 135.0));
    test.insert(ProgramSequenceChord.create(sequence15a, 0.0, "G minor"));
    test.insert(ProgramSequenceChord.create(sequence15a, 8.0, "Ab minor"));
    program15_binding0 = test.insert(ProgramSequenceBinding.create(sequence15a, 0));
    test.insert(ProgramSequenceBindingMeme.create(program15_binding0, "Regret"));
    //
    ProgramSequence sequence15b = test.insert(ProgramSequence.create(program15, 32, "Outro", 0.5, "A major", 135.0));
    test.insert(ProgramSequenceChord.create(sequence15b, 0.0, "C major"));
    test.insert(ProgramSequenceChord.create(sequence15b, 8.0, "Bb major"));
    program15_binding1 = test.insert(ProgramSequenceBinding.create(sequence15b, 1));
    test.insert(ProgramSequenceBindingMeme.create(program15_binding1, "Pride"));
    test.insert(ProgramSequenceBindingMeme.create(program15_binding1, "Shame"));
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
  public void insertFixtureB3() throws CoreException {
    // A basic beat
    program9 = test.insert(Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6));
    test.insert(ProgramMeme.create(program9, "Basic"));
    voiceDrums = test.insert(ProgramVoice.create(program9, InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence9a = test.insert(ProgramSequence.create(program9, 16, "Base", 0.5, "C", 110.3));
    //
    ProgramSequencePattern pattern9a1 = test.insert(ProgramSequencePattern.create(sequence9a, voiceDrums, ProgramPatternType.Intro, 4, "Intro"));
    test.insert(ProgramSequencePatternEvent.create(pattern9a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "BLEEP")), 0, 1, "C2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern9a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "BLEIP")), 1, 1, "G5", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern9a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "BLEAP")), 2.5, 1, "C2", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern9a1, test.insert(ProgramVoiceTrack.create(voiceDrums, "BLEEEP")), 3, 1, "G5", 0.9));
    //
    ProgramSequencePattern pattern9a2 = test.insert(ProgramSequencePattern.create(sequence9a, voiceDrums, ProgramPatternType.Loop, 4, "Loop A"));
    test.insert(ProgramSequencePatternEvent.create(pattern9a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "CLOCK")), 0, 1, "C2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern9a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNORT")), 1, 1, "G5", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern9a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "KICK")), 2.5, 1, "C2", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern9a2, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNARL")), 3, 1, "G5", 0.9));
    //
    ProgramSequencePattern pattern9a3 = test.insert(ProgramSequencePattern.create(sequence9a, voiceDrums, ProgramPatternType.Loop, 4, "Loop B"));
    test.insert(ProgramSequencePatternEvent.create(pattern9a3, test.insert(ProgramVoiceTrack.create(voiceDrums, "KIICK")), 0, 1, "B5", 0.9));
    test.insert(ProgramSequencePatternEvent.create(pattern9a3, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNARR")), 1, 1, "D2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern9a3, test.insert(ProgramVoiceTrack.create(voiceDrums, "KEICK")), 2.5, 1, "E4", 0.7));
    test.insert(ProgramSequencePatternEvent.create(pattern9a3, test.insert(ProgramVoiceTrack.create(voiceDrums, "SNAER")), 3, 1, "C3", 0.5));
    //
    ProgramSequencePattern pattern9a4 = test.insert(ProgramSequencePattern.create(sequence9a, voiceDrums, ProgramPatternType.Outro, 4, "Outro"));
    test.insert(ProgramSequencePatternEvent.create(pattern9a4, test.insert(ProgramVoiceTrack.create(voiceDrums, "TOOT")), 0, 1, "C2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern9a4, test.insert(ProgramVoiceTrack.create(voiceDrums, "TOOOT")), 1, 1, "G5", 0.8));
    test.insert(ProgramSequencePatternEvent.create(pattern9a4, test.insert(ProgramVoiceTrack.create(voiceDrums, "TOOTE")), 2.5, 1, "C2", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern9a4, test.insert(ProgramVoiceTrack.create(voiceDrums, "TOUT")), 3, 1, "G5", 0.9));

    // Instrument "808"
    instrument8 = test.insert(Instrument.create(user3, library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(instrument8, "heavy"));
    //
    audio8kick = test.insert(InstrumentAudio.create(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.62));
    test.insert(InstrumentAudioEvent.create(audio8kick, 0, 1, "KICK", "Eb", 1.0));
    //
    audio8snare = test.insert(InstrumentAudio.create(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    test.insert(InstrumentAudioEvent.create(audio8snare, 0, 1, "SNARE", "Ab", 0.8));
    //
    audio8bleep = test.insert(InstrumentAudio.create(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    test.insert(InstrumentAudioEvent.create(audio8bleep, 0, 1, "BLEEP", "Ab", 0.8));
    //
    audio8toot = test.insert(InstrumentAudio.create(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    test.insert(InstrumentAudioEvent.create(audio8toot, 0, 1, "TOOT", "Ab", 0.8));
  }

  /**
   Library of Content B: Instruments (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB_Instruments() throws CoreException {
    instrument201 = test.insert(Instrument.create(user3, library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(instrument201, "Ants"));
    test.insert(InstrumentMeme.create(instrument201, "Mold"));
    //
    InstrumentAudio audio401 = test.insert(InstrumentAudio.create(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    test.insert(InstrumentAudioEvent.create(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
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

  /**
   Shared fixtures for tests that require a library and some entities
   */
  public void insertFixtureC() throws CoreException {
    // User "bill"
    user2 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));

    // Library "test sounds"
    library1 = test.insert(Library.create(account1, "test sounds", InternalResources.now()));

    // Program: Epic Beat
    program1 = test.insert(Program.create(user2, library1, ProgramType.Rhythm, ProgramState.Published, "epic beat", "C#", 0.286, 0.6));
    ProgramSequence sequence1 = test.insert(ProgramSequence.create(program1, 16, "epic beat part 1", 0.342, "C#", 0.286));
    ProgramSequenceBinding binding1_0 = test.insert(ProgramSequenceBinding.create(sequence1, 0));
    ProgramVoice voice1x = test.insert(ProgramVoice.create(program1, InstrumentType.Percussive, "This is a percussive create"));
    ProgramSequencePattern pattern1a = test.insert(ProgramSequencePattern.create(sequence1, voice1x, ProgramPatternType.Loop, 16, "Ants"));
    test.insert(ProgramSequencePatternEvent.create(pattern1a, test.insert(ProgramVoiceTrack.create(voice1x, "KICK")), 0.0, 1.0, "C", 1.0));

    // Library has Instrument with Audio
    instrument9 = test.insert(Instrument.create(user2, library1, InstrumentType.Percussive, InstrumentState.Published, "jams"));
    test.insert(InstrumentAudio.create(instrument9, "Kick", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440.0, 0.62));

    // Chain "Test Print #1" has one segment
    chain3 = test.insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    // segment-17 at offset-0 of chain-3
    segment17 = test.insert(new Segment()
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D Major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create()
      .setSegmentId(segment17.getId())
      .setProgramId(program1.getId())
      .setSequenceBinding(binding1_0)
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));
  }

  /**
   Insert a generated library

   @param N magnitude of library to generate
   */
  public void insertGeneratedFixture(int N) throws CoreException {
    account1 = test.insert(Account.create("Generated"));
    user1 = test.insert(User.create("generated", "generated@email.com", "http://pictures.com/generated.gif"));
    test.insert(UserRole.create(user1, UserRoleType.Admin));
    library1 = test.insert(Library.create(account1, "generated", InternalResources.now()));

    Collection<Entity> inserts = Lists.newArrayList();

    // Create a N-magnitude set of unique major memes
    String[] majorMemeNames = listOfUniqueRandom(N, LoremIpsum.COLORS);
    String[] minorMemeNames = listOfUniqueRandom((long) StrictMath.ceil(N / 2), LoremIpsum.VARIANTS);
    String[] percussiveNames = listOfUniqueRandom(N, LoremIpsum.PERCUSSIVE_NAMES);

    // Generate a Percussive Instrument for each meme
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      String minorMemeName = random(minorMemeNames);
      //
      Instrument instrument = add(inserts, Instrument.create(user1, library1, InstrumentType.Percussive, InstrumentState.Published, String.format("%s Drums", majorMemeName)));
      add(inserts, InstrumentMeme.create(instrument, majorMemeName));
      add(inserts, InstrumentMeme.create(instrument, minorMemeName));
      // audios of instrument
      for (int k = 0; k < N; k++) {
        InstrumentAudio audio = add(inserts, InstrumentAudio.create(instrument, Text.toProper(percussiveNames[k]), String.format("%s.wav", Text.toLowerSlug(percussiveNames[k])), random(0, 0.05), random(0.25, 2), random(80, 120), random(100, 4000), 0.62));
        add(inserts, InstrumentAudioEvent.create(audio, 0.0, 1.0, percussiveNames[k], "X", random(0.8, 1)));
      }
      //
      log.info("Generated Percussive-type Instrument id={}, minorMeme={}, majorMeme={}", instrument.getId(), minorMemeName, majorMemeName);
    }

    // Generate N*2 total Macro-type programs, each transitioning of one MemeEntity to another
    for (int i = 0; i < N << 1; i++) {
      String[] twoMemeNames = listOfUniqueRandom(2, majorMemeNames);
      String majorMemeFromName = twoMemeNames[0];
      String majorMemeToName = twoMemeNames[1];
      String minorMemeName = random(minorMemeNames);
      String[] twoKeys = listOfUniqueRandom(2, LoremIpsum.MUSICAL_KEYS);
      String keyFrom = twoKeys[0];
      String keyTo = twoKeys[1];
      double densityFrom = random(0.3, 0.9);
      double tempoFrom = random(80, 120);
      //
      Program program = add(inserts, Program.create(user1, library1, ProgramType.Macro, ProgramState.Published, String.format("%s, create %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, 0.6));
      add(inserts, ProgramMeme.create(program, minorMemeName));
      // of offset 0
      ProgramSequence sequence0 = add(inserts, ProgramSequence.create(program, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom));
      ProgramSequenceBinding binding0 = add(inserts, ProgramSequenceBinding.create(sequence0, 0));
      add(inserts, ProgramSequenceBindingMeme.create(binding0, majorMemeFromName));
      // to offset 1
      double densityTo = random(0.3, 0.9);
      double tempoTo = random(803, 120);
      ProgramSequence sequence1 = add(inserts, ProgramSequence.create(program, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo));
      ProgramSequenceBinding binding1 = add(inserts, ProgramSequenceBinding.create(sequence1, 1));
      add(inserts, ProgramSequenceBindingMeme.create(binding1, majorMemeToName));
      //
      log.info("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", program.getId(), minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    ProgramSequence[] sequences = new ProgramSequence[N];
    for (int i = 0; i < N << 2; i++) {
      String majorMemeName = random(majorMemeNames);
      String[] sequenceNames = listOfUniqueRandom(N, LoremIpsum.ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, LoremIpsum.MUSICAL_KEYS);
      Double[] subDensities = listOfRandomValues(N, 0.3, 0.8);
      double tempo = random(80, 120);
      //
      Program program = add(inserts, Program.create(user1, library1, ProgramType.Main, ProgramState.Published, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, 0.6));
      add(inserts, ProgramMeme.create(program, majorMemeName));
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = add(inserts, ProgramSequence.create(program, total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP], tempo));
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            add(inserts, ProgramSequenceChord.create(sequences[iP], StrictMath.floor(total * iPC / N << 2), random(LoremIpsum.MUSICAL_CHORDS)));
          }
        }
      }
      // sequence sequence binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        ProgramSequenceBinding binding = add(inserts, ProgramSequenceBinding.create(sequences[num], offset));
        add(inserts, ProgramSequenceBindingMeme.create(binding, random(minorMemeNames)));
      }
      log.info("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.getId(), majorMemeName, N, N << 2);
    }

    // Generate N total Rhythm-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    ProgramVoice[] voices = new ProgramVoice[N];
    Map<String, ProgramVoiceTrack> trackMap = Maps.newHashMap();
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      double tempo = random(80, 120);
      String key = random(LoremIpsum.MUSICAL_KEYS);
      double density = random(0.4, 0.9);
      //
      Program program = add(inserts, Program.create(user1, library1, ProgramType.Rhythm, ProgramState.Published, String.format("%s Beat", majorMemeName), key, tempo, 0.6));
      trackMap.clear();
      add(inserts, ProgramMeme.create(program, majorMemeName));
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = add(inserts, ProgramVoice.create(program, InstrumentType.Percussive, String.format("%s %s", majorMemeName, percussiveNames[iV])));
      }
      ProgramSequence sequenceBase = add(inserts, ProgramSequence.create(program, random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key, tempo));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        ProgramPatternType type = 0 == iP ? ProgramPatternType.Loop : randomRhythmPatternType();
        ProgramSequencePattern pattern = add(inserts, ProgramSequencePattern.create(sequenceBase, voices[num], type, total, String.format("%s %s %s", majorMemeName, type.toString(), random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            String name = percussiveNames[num];
            if (!trackMap.containsKey(name))
              trackMap.put(name, add(inserts, ProgramVoiceTrack.create(voices[num], name)));
            add(inserts, ProgramSequencePatternEvent.create(pattern, trackMap.get(name), StrictMath.floor(total * iPE / N << 2), random(0.25, 1.0), "X", random(0.4, 0.9)));
          }
        }
      }
      log.info("Generated Rhythm-type Program id={}, majorMeme={} with {} patterns", program.getId(), majorMemeName, N);
    }

    long startTime = System.nanoTime();
    test.batchInsert(inserts);
    long endTime = System.nanoTime();
    long millis = (endTime - startTime) / 1000000;
    double seconds = (double) millis / 1000.0;
    log.info("Batch inserted {} records in {}s into library id={}", inserts.size(), seconds, library1.getId());
  }

  /**
   Add an entity to a collection, then return that entity

   @param to     collection
   @param entity to add
   @param <N>    type of entity
   @return entity that's been added
   */
  private <N extends Entity> N add(Collection<Entity> to, N entity) {
    to.add(entity);
    return entity;
  }

  /**
   Destroy inner entities of program

   @param program to destroy inner entities of
   */
  public void destroyInnerEntities(Program program) {
    UUID id = program.getId();
    DSLContext db = test.getDSL();
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.eq(id)).execute();
    db.deleteFrom(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.eq(id)).execute();
  }
}
