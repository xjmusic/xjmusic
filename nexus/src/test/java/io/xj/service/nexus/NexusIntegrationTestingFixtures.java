// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class NexusIntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(NexusIntegrationTestingFixtures.class);
  private static final double RANDOM_VALUE_FROM = 0.3;
  private static final double RANDOM_VALUE_TO = 0.8;

  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public AccountUser accountUser1a;
  public Instrument instrument8;
  public Instrument instrument9;
  public InstrumentAudio instrument8_audio8bleep;
  public InstrumentAudio instrument8_audio8kick;
  public InstrumentAudio instrument8_audio8snare;
  public InstrumentAudio instrument8_audio8toot;
  public InstrumentAudio instrument9_audio8;
  public InstrumentAudioEvent instrument8_audio8bleep_event0;
  public InstrumentAudioEvent instrument8_audio8kick_event0;
  public InstrumentAudioEvent instrument8_audio8snare_event0;
  public InstrumentAudioEvent instrument8_audio8toot_event0;
  public InstrumentAudioEvent instrument9_audio8_event0;
  public InstrumentMeme instrument8_meme0;
  public InstrumentMeme instrument9_meme0;
  public Library library1;
  public Library library2;
  public Program program10;
  public Program program15;
  public Program program1;
  public Program program2;
  public Program program35;
  public Program program3;
  public Program program4;
  public Program program5;
  public Program program6;
  public Program program7;
  public Program program9;
  public ProgramMeme program10_meme0;
  public ProgramMeme program15_meme0;
  public ProgramMeme program35_meme0;
  public ProgramMeme program3_meme0;
  public ProgramMeme program4_meme0;
  public ProgramMeme program5_meme0;
  public ProgramMeme program9_meme0;
  public ProgramSequence program10_sequence0;
  public ProgramSequence program15_sequence0;
  public ProgramSequence program15_sequence1;
  public ProgramSequence program35_sequence0;
  public ProgramSequence program3_sequence0;
  public ProgramSequence program3_sequence1;
  public ProgramSequence program4_sequence0;
  public ProgramSequence program4_sequence1;
  public ProgramSequence program4_sequence2;
  public ProgramSequence program5_sequence0;
  public ProgramSequence program5_sequence1;
  public ProgramSequence program9_sequence0;
  public ProgramSequenceBinding program15_sequence0_binding0;
  public ProgramSequenceBinding program15_sequence1_binding0;
  public ProgramSequenceBinding program3_sequence0_binding0;
  public ProgramSequenceBinding program3_sequence1_binding0;
  public ProgramSequenceBinding program4_sequence0_binding0;
  public ProgramSequenceBinding program4_sequence1_binding0;
  public ProgramSequenceBinding program4_sequence2_binding0;
  public ProgramSequenceBinding program5_sequence0_binding0;
  public ProgramSequenceBinding program5_sequence1_binding0;
  public ProgramSequenceBindingMeme program15_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program15_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program15_sequence1_binding0_meme1;
  public ProgramSequenceBindingMeme program3_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program3_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence1_binding0_meme1;
  public ProgramSequenceBindingMeme program4_sequence2_binding0_meme0;
  public ProgramSequenceBindingMeme program5_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program5_sequence1_binding0_meme0;
  public ProgramSequenceChord program15_sequence0_chord0;
  public ProgramSequenceChord program15_sequence0_chord1;
  public ProgramSequenceChord program15_sequence1_chord0;
  public ProgramSequenceChord program15_sequence1_chord1;
  public ProgramSequenceChord program5_sequence0_chord0;
  public ProgramSequenceChord program5_sequence0_chord1;
  public ProgramSequenceChord program5_sequence0_chord2;
  public ProgramSequenceChord program5_sequence1_chord0;
  public ProgramSequenceChord program5_sequence1_chord1;
  public ProgramSequenceChordVoicing program15_sequence0_chord0_voicing;
  public ProgramSequenceChordVoicing program15_sequence0_chord1_voicing;
  public ProgramSequenceChordVoicing program15_sequence1_chord0_voicing;
  public ProgramSequenceChordVoicing program15_sequence1_chord1_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord0_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord1_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord2_voicing;
  public ProgramSequenceChordVoicing program5_sequence1_chord0_voicing;
  public ProgramSequenceChordVoicing program5_sequence1_chord1_voicing;
  public ProgramSequencePattern program10_sequence0_pattern0;
  public ProgramSequencePattern program10_sequence0_pattern1;
  public ProgramSequencePattern program10_sequence0_pattern2;
  public ProgramSequencePattern program10_sequence0_pattern3;
  public ProgramSequencePattern program35_sequence0_pattern0;
  public ProgramSequencePattern program35_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern0;
  public ProgramSequencePattern program9_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern2;
  public ProgramSequencePattern program9_sequence0_pattern3;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event3;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event3;
  public ProgramVoice program10_voice0;
  public ProgramVoice program35_voice0;
  public ProgramVoice program9_voice0;
  public ProgramVoiceTrack program10_voice0_track0;
  public ProgramVoiceTrack program35_voice0_track0;
  public ProgramVoiceTrack program35_voice0_track1;
  public ProgramVoiceTrack program35_voice0_track2;
  public ProgramVoiceTrack program35_voice0_track3;
  public ProgramVoiceTrack program9_voice0_track0;
  public ProgramVoiceTrack program9_voice0_track10;
  public ProgramVoiceTrack program9_voice0_track11;
  public ProgramVoiceTrack program9_voice0_track12;
  public ProgramVoiceTrack program9_voice0_track13;
  public ProgramVoiceTrack program9_voice0_track14;
  public ProgramVoiceTrack program9_voice0_track15;
  public ProgramVoiceTrack program9_voice0_track1;
  public ProgramVoiceTrack program9_voice0_track2;
  public ProgramVoiceTrack program9_voice0_track3;
  public ProgramVoiceTrack program9_voice0_track4;
  public ProgramVoiceTrack program9_voice0_track5;
  public ProgramVoiceTrack program9_voice0_track6;
  public ProgramVoiceTrack program9_voice0_track7;
  public ProgramVoiceTrack program9_voice0_track8;
  public ProgramVoiceTrack program9_voice0_track9;
  public User user1;
  public User user2;
  public User user3;
  public UserRole userRole2a;
  public UserRole userRole3a;

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  protected static ProgramSequencePattern.Type randomRhythmPatternType() {
    return new ProgramSequencePattern.Type[]{
      ProgramSequencePattern.Type.Intro,
      ProgramSequencePattern.Type.Loop,
      ProgramSequencePattern.Type.Outro
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  protected static Double[] listOfRandomValues(int N) {
    Double[] result = new Double[N];
    for (int i = 0; i < N; i++) {
      result[i] = random(RANDOM_VALUE_FROM, RANDOM_VALUE_TO);
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

  public static SegmentChoice buildSegmentChoice(Segment segment, Program.Type programType, ProgramSequenceBinding programSequenceBinding) {
    return SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setProgramId(programSequenceBinding.getProgramId())
      .setProgramSequenceBindingId(programSequenceBinding.getId())
      .setProgramType(programType)
      .build();
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program.Type programType, Program program) {
    return SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setProgramId(program.getId())
      .setProgramType(programType)
      .build();
  }

  public static SegmentMeme buildSegmentMeme(Segment segment, String name) {
    return SegmentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setName(name)
      .build();
  }

  public static SegmentChord buildSegmentChord(Segment segment, Double position, String name) {
    return SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setPosition(position)
      .setName(name)
      .build();
  }

  public static SegmentChoiceArrangement buildSegmentChoiceArrangement(SegmentChoice segmentChoice, ProgramVoice programVoice, Instrument instrument) {
    return SegmentChoiceArrangement.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentChoice.getSegmentId())
      .setSegmentChoiceId(segmentChoice.getId())
      .setProgramVoiceId(programVoice.getId())
      .setInstrumentId(instrument.getId())
      .build();
  }

  public static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent programSequencePatternEvent, InstrumentAudio instrumentAudio, double position, double duration, double velocity, String note, String name) {
    return SegmentChoiceArrangementPick.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentChoiceArrangement.getSegmentId())
      .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .setProgramSequencePatternEventId(programSequencePatternEvent.getId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setStart(position)
      .setLength(duration)
      .setAmplitude(velocity)
      .setNote(note)
      .setName(name)
      .build();
  }

  /**
   A whole library of mock content

   @return collection of entities
   */
  public Collection<Object> setupFixtureB1() throws EntityException {

    // Account "bananas"
    account1 = buildAccount("bananas");

    // Library "house"
    library2 = buildLibrary(account1, "house");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = buildUser("john", "john@email.com", "http://pictures.com/john.gif");
    userRole2a = buildUserRole(user2, UserRole.Type.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    userRole3a = buildUserRole(user3, UserRole.Type.User);
    accountUser1a = buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = buildProgram(library2, Program.Type.Macro, Program.State.Published, "Tropical, Wild to Cozy", "C", 120.0, 0.6);
    program4_meme0 = buildProgramMeme(program4, "Tropical");
    //
    program4_sequence0 = buildProgramSequence(program4, 0, "Start Wild", 0.6, "C", 125.0);
    program4_sequence0_binding0 = buildProgramSequenceBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program4_sequence0_binding0, "Wild");
    //
    program4_sequence1 = buildProgramSequence(program4, 0, "Intermediate", 0.4, "Bb minor", 115.0);
    program4_sequence1_binding0 = buildProgramSequenceBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = buildProgramSequence(program4, 0, "Finish Cozy", 0.4, "Ab minor", 125.0);
    program4_sequence2_binding0 = buildProgramSequenceBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = buildProgramSequenceBindingMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = buildProgram(library2, Program.Type.Main, Program.State.Published, "Main Jam", "C minor", 140, 0.6);
    program5_meme0 = buildProgramMeme(program5, "Outlook");
    //
    program5_sequence0 = buildProgramSequence(program5, 16, "Intro", 0.5, "G major", 135.0);
    program5_sequence0_chord0 = buildProgramSequenceChord(program5_sequence0, 0.0, "G major");

    program5_sequence0_chord0_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program5_sequence0_chord0, "G3, B3, D4");
    program5_sequence0_chord1 = buildProgramSequenceChord(program5_sequence0, 8.0, "Ab minor");

    program5_sequence0_chord1_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program5_sequence0_chord1, "Ab3, Db3, F4");
    program5_sequence0_chord2 = buildProgramSequenceChord(program5_sequence0, 75.0, "G-9"); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total

    program5_sequence0_chord2_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program5_sequence0_chord2, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = buildProgramSequenceBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = buildProgramSequence(program5, 32, "Drop", 0.5, "G minor", 135.0);
    program5_sequence1_chord0 = buildProgramSequenceChord(program5_sequence1, 0.0, "C major");

    program5_sequence1_chord0_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program5_sequence1_chord0, "Ab3, Db3, F4");
    program5_sequence1_chord1 = buildProgramSequenceChord(program5_sequence1, 8.0, "Bb minor");

    program5_sequence1_chord1_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program5_sequence1_chord1, "Ab3, Db3, F4");
    program5_sequence1_binding0 = buildProgramSequenceBinding(program5_sequence1, 1);
    program5_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program5_sequence1_binding0, "Pessimism");

    // A basic beat
    program35 = buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "Basic Beat", "C", 121, 0.6);
    program35_meme0 = buildProgramMeme(program35, "Basic");
    program35_voice0 = buildProgramVoice(program35, Instrument.Type.Percussive, "Drums");
    program35_voice0_track0 = buildProgramVoiceTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = buildProgramVoiceTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = buildProgramVoiceTrack(program35_voice0, "KICK");
    program35_voice0_track3 = buildProgramVoiceTrack(program35_voice0, "SNARL");
    //
    program35_sequence0 = buildProgramSequence(program35, 16, "Base", 0.5, "C", 110.3);
    program35_sequence0_pattern0 = buildProgramSequencePattern(program35_sequence0, program35_voice0, ProgramSequencePattern.Type.Loop, 4, "Drop");
    program35_sequence0_pattern0_event0 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0, 1.0, "C2", 1.0);
    program35_sequence0_pattern0_event1 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0, 1.0, "G5", 0.8);
    program35_sequence0_pattern0_event2 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5, 1.0, "C2", 0.6);
    program35_sequence0_pattern0_event3 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0, 1.0, "G5", 0.9);
    //
    program35_sequence0_pattern1 = buildProgramSequencePattern(program35_sequence0, program35_voice0, ProgramSequencePattern.Type.Loop, 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0, 1.0, "B5", 0.9);
    program35_sequence0_pattern1_event1 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0, 1.0, "D2", 1.0);
    program35_sequence0_pattern1_event2 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5, 1.0, "E4", 0.7);
    program35_sequence0_pattern1_event3 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0, 1.0, "c3", 0.5);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database entities, so ordered from parent -> child
    return ImmutableList.of(
      account1,
      library2,
      user2,
      userRole2a,
      user3,
      userRole3a,
      accountUser1a,
      program35,
      program35_voice0,
      program35_voice0_track0,
      program35_voice0_track1,
      program35_voice0_track2,
      program35_voice0_track3,
      program35_meme0,
      program35_sequence0,
      program35_sequence0_pattern0,
      program35_sequence0_pattern0_event0,
      program35_sequence0_pattern0_event1,
      program35_sequence0_pattern0_event2,
      program35_sequence0_pattern0_event3,
      program35_sequence0_pattern1,
      program35_sequence0_pattern1_event0,
      program35_sequence0_pattern1_event1,
      program35_sequence0_pattern1_event2,
      program35_sequence0_pattern1_event3,
      program4,
      program4_meme0,
      program4_sequence0,
      program4_sequence0_binding0,
      program4_sequence0_binding0_meme0,
      program4_sequence1,
      program4_sequence1_binding0,
      program4_sequence1_binding0_meme0,
      program4_sequence1_binding0_meme1,
      program4_sequence2,
      program4_sequence2_binding0,
      program4_sequence2_binding0_meme0,
      program5,
      program5_meme0,
      program5_sequence0,
      program5_sequence0_binding0,
      program5_sequence0_binding0_meme0,
      program5_sequence0_chord0,
      program5_sequence0_chord0_voicing,
      program5_sequence0_chord1,
      program5_sequence0_chord1_voicing,
      program5_sequence0_chord2,
      program5_sequence0_chord2_voicing,
      program5_sequence1,
      program5_sequence1_binding0,
      program5_sequence1_binding0_meme0,
      program5_sequence1_chord0,
      program5_sequence1_chord0_voicing,
      program5_sequence1_chord1,
      program5_sequence1_chord1_voicing
    );
  }


  /**
   Library of Content B-2 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public Collection<Object> setupFixtureB2() {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = buildProgram(library2, Program.Type.Macro, Program.State.Published, "Tangy, Chunky to Smooth", "G minor", 120.0, 0.6);
    program3_meme0 = buildProgramMeme(program3, "Tangy");
    //
    program3_sequence0 = buildProgramSequence(program3, 0, "Start Chunky", 0.4, "G minor", 115.0);
    program3_sequence0_binding0 = buildProgramSequenceBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = buildProgramSequence(program3, 0, "Finish Smooth", 0.6, "C", 125.0);
    program3_sequence1_binding0 = buildProgramSequenceBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = buildProgram(library2, Program.Type.Main, Program.State.Published, "Next Jam", "Db minor", 140, 0.6);
    program15_meme0 = buildProgramMeme(program15, "Hindsight");
    //
    program15_sequence0 = buildProgramSequence(program15, 16, "Intro", 0.5, "G minor", 135.0);
    program15_sequence0_chord0 = buildProgramSequenceChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord0_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program15_sequence0_chord0, "G3, Bb3, D4");
    program15_sequence0_chord1 = buildProgramSequenceChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_chord1_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program15_sequence0_chord1, "Ab3, C3, Eb4");
    program15_sequence0_binding0 = buildProgramSequenceBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = buildProgramSequence(program15, 32, "Outro", 0.5, "A major", 135.0);
    program15_sequence1_chord0 = buildProgramSequenceChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord0_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program15_sequence0_chord0, "E3, G3, C4");
    program15_sequence1_chord1 = buildProgramSequenceChord(program15_sequence1, 8.0, "Bb major");
    program15_sequence1_chord1_voicing = buildProgramSequenceChordVoicing(Instrument.Type.Bass, program15_sequence0_chord1, "F3, Bb3, D4");
    program15_sequence1_binding0 = buildProgramSequenceBinding(program15_sequence1, 1);
    program15_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program15_sequence1_binding0, "Pride");
    program15_sequence1_binding0_meme1 = buildProgramSequenceBindingMeme(program15_sequence1_binding0, "Shame");

    // return them all
    return ImmutableList.of(
      program3,
      program3_meme0,
      program3_sequence0,
      program3_sequence0_binding0,
      program3_sequence0_binding0_meme0,
      program3_sequence1,
      program3_sequence1_binding0,
      program3_sequence1_binding0_meme0,
      program15,
      program15_meme0,
      program15_sequence0,
      program15_sequence0_chord0,
      program15_sequence0_chord0_voicing,
      program15_sequence0_chord1,
      program15_sequence0_chord1_voicing,
      program15_sequence0_binding0,
      program15_sequence0_binding0_meme0,
      program15_sequence1,
      program15_sequence1_chord0,
      program15_sequence1_chord0_voicing,
      program15_sequence1_chord1,
      program15_sequence1_chord1_voicing,
      program15_sequence1_binding0,
      program15_sequence1_binding0_meme0,
      program15_sequence1_binding0_meme1
    );
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
  public Collection<Object> setupFixtureB3() {
    // A basic beat
    program9 = buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "Basic Beat", "C", 121, 0.6);
    program9_meme0 = buildProgramMeme(program9, "Basic");
    //
    program9_voice0 = buildProgramVoice(program9, Instrument.Type.Percussive, "Drums");
    program9_voice0_track0 = buildProgramVoiceTrack(program9_voice0, "BLEEP");
    program9_voice0_track1 = buildProgramVoiceTrack(program9_voice0, "BLEIP");
    program9_voice0_track2 = buildProgramVoiceTrack(program9_voice0, "BLEAP");
    program9_voice0_track3 = buildProgramVoiceTrack(program9_voice0, "BLEEEP");
    program9_voice0_track4 = buildProgramVoiceTrack(program9_voice0, "CLOCK");
    program9_voice0_track5 = buildProgramVoiceTrack(program9_voice0, "SNORT");
    program9_voice0_track6 = buildProgramVoiceTrack(program9_voice0, "KICK");
    program9_voice0_track7 = buildProgramVoiceTrack(program9_voice0, "SNARL");
    program9_voice0_track8 = buildProgramVoiceTrack(program9_voice0, "KIICK");
    program9_voice0_track9 = buildProgramVoiceTrack(program9_voice0, "SNARR");
    program9_voice0_track10 = buildProgramVoiceTrack(program9_voice0, "KEICK");
    program9_voice0_track11 = buildProgramVoiceTrack(program9_voice0, "SNAER");
    program9_voice0_track12 = buildProgramVoiceTrack(program9_voice0, "TOOT");
    program9_voice0_track13 = buildProgramVoiceTrack(program9_voice0, "TOOOT");
    program9_voice0_track14 = buildProgramVoiceTrack(program9_voice0, "TOOTE");
    program9_voice0_track15 = buildProgramVoiceTrack(program9_voice0, "TOUT");
    //
    program9_sequence0 = buildProgramSequence(program9, 16, "Base", 0.5, "C", 110.3);
    //
    program9_sequence0_pattern0 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Intro, 4, "Intro");
    program9_sequence0_pattern0_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0);
    program9_sequence0_pattern0_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8);
    program9_sequence0_pattern0_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern0_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern1 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop A");
    program9_sequence0_pattern1_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0);
    program9_sequence0_pattern1_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8);
    program9_sequence0_pattern1_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern1_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern2 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop B");
    program9_sequence0_pattern2_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9);
    program9_sequence0_pattern2_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0);
    program9_sequence0_pattern2_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5, 1, "E4", 0.7);
    program9_sequence0_pattern2_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5);
    //
    program9_sequence0_pattern3 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Outro, 4, "Outro");
    program9_sequence0_pattern3_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0);
    program9_sequence0_pattern3_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8);
    program9_sequence0_pattern3_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern3_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9);

    // Instrument "808"
    instrument8 = buildInstrument(library2, Instrument.Type.Percussive, Instrument.State.Published, "808 Drums");
    instrument8_meme0 = buildInstrumentMeme(instrument8, "heavy");
    instrument8_audio8kick = buildInstrumentAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.62);
    instrument8_audio8kick_event0 = buildInstrumentAudioEvent(instrument8_audio8kick, 0, 1, "KICK", "Eb", 1.0);
    instrument8_audio8snare = buildInstrumentAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 1200, 0.62);
    instrument8_audio8snare_event0 = buildInstrumentAudioEvent(instrument8_audio8snare, 0, 1, "SNARE", "Ab", 0.8);
    instrument8_audio8bleep = buildInstrumentAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 1200, 0.62);
    instrument8_audio8bleep_event0 = buildInstrumentAudioEvent(instrument8_audio8bleep, 0, 1, "BLEEP", "Ab", 0.8);
    instrument8_audio8toot = buildInstrumentAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 1200, 0.62);
    instrument8_audio8toot_event0 = buildInstrumentAudioEvent(instrument8_audio8toot, 0, 1, "TOOT", "Ab", 0.8);

    // return them all
    return ImmutableList.of(
      program9,
      program9_meme0,
      program9_voice0,
      program9_voice0_track0,
      program9_voice0_track1,
      program9_voice0_track2,
      program9_voice0_track3,
      program9_voice0_track4,
      program9_voice0_track5,
      program9_voice0_track6,
      program9_voice0_track7,
      program9_voice0_track8,
      program9_voice0_track9,
      program9_voice0_track10,
      program9_voice0_track11,
      program9_voice0_track12,
      program9_voice0_track13,
      program9_voice0_track14,
      program9_voice0_track15,
      program9_sequence0,
      program9_sequence0_pattern0,
      program9_sequence0_pattern0_event0,
      program9_sequence0_pattern0_event1,
      program9_sequence0_pattern0_event2,
      program9_sequence0_pattern0_event3,
      program9_sequence0_pattern1,
      program9_sequence0_pattern1_event0,
      program9_sequence0_pattern1_event1,
      program9_sequence0_pattern1_event2,
      program9_sequence0_pattern1_event3,
      program9_sequence0_pattern2,
      program9_sequence0_pattern2_event0,
      program9_sequence0_pattern2_event1,
      program9_sequence0_pattern2_event2,
      program9_sequence0_pattern2_event3,
      program9_sequence0_pattern3,
      program9_sequence0_pattern3_event0,
      program9_sequence0_pattern3_event1,
      program9_sequence0_pattern3_event2,
      program9_sequence0_pattern3_event3,
      instrument8,
      instrument8_meme0,
      instrument8_audio8kick,
      instrument8_audio8kick_event0,
      instrument8_audio8snare,
      instrument8_audio8snare_event0,
      instrument8_audio8bleep,
      instrument8_audio8bleep_event0,
      instrument8_audio8toot,
      instrument8_audio8toot_event0
    );
  }

  /**
   Library of Content B-4 (shared test fixture)
   <p>
   [#154464276] Detail Craft v1
   */
  public Collection<Object> setupFixtureB4_DetailBass() {
    // A basic bass pattern
    program10 = buildProgram(library2, Program.Type.Detail, Program.State.Published, "Earth Bass Detail Pattern", "C", 121, 0.6);
    program10_meme0 = buildProgramMeme(program10, "EARTH");
    //
    program10_voice0 = buildProgramVoice(program10, Instrument.Type.Bass, "Dirty Bass");
    program10_voice0_track0 = buildProgramVoiceTrack(program10_voice0, "BUM");
    //
    program10_sequence0 = buildProgramSequence(program10, 16, "Simple Walk", 0.5, "C", 110.3);
    //
    program10_sequence0_pattern0 = buildProgramSequencePattern(program10_sequence0, program10_voice0, ProgramSequencePattern.Type.Intro, 4, "Intro");
    program10_sequence0_pattern0_event0 = buildProgramSequencePatternEvent(program10_sequence0_pattern0, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern0_event1 = buildProgramSequencePatternEvent(program10_sequence0_pattern0, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern0_event2 = buildProgramSequencePatternEvent(program10_sequence0_pattern0, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern0_event3 = buildProgramSequencePatternEvent(program10_sequence0_pattern0, program10_voice0_track0, 3, 1, "G5", 0.9);
    //
    program10_sequence0_pattern1 = buildProgramSequencePattern(program10_sequence0, program10_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop A");
    program10_sequence0_pattern1_event0 = buildProgramSequencePatternEvent(program10_sequence0_pattern1, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern1_event1 = buildProgramSequencePatternEvent(program10_sequence0_pattern1, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern1_event2 = buildProgramSequencePatternEvent(program10_sequence0_pattern1, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern1_event3 = buildProgramSequencePatternEvent(program10_sequence0_pattern1, program10_voice0_track0, 3, 1, "G5", 0.9);
    //
    program10_sequence0_pattern2 = buildProgramSequencePattern(program10_sequence0, program10_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop B");
    program10_sequence0_pattern2_event0 = buildProgramSequencePatternEvent(program10_sequence0_pattern2, program10_voice0_track0, 0, 1, "B5", 0.9);
    program10_sequence0_pattern2_event1 = buildProgramSequencePatternEvent(program10_sequence0_pattern2, program10_voice0_track0, 1, 1, "D2", 1.0);
    program10_sequence0_pattern2_event2 = buildProgramSequencePatternEvent(program10_sequence0_pattern2, program10_voice0_track0, 2, 1, "E4", 0.7);
    program10_sequence0_pattern2_event3 = buildProgramSequencePatternEvent(program10_sequence0_pattern2, program10_voice0_track0, 3, 1, "C3", 0.5);
    //
    program10_sequence0_pattern3 = buildProgramSequencePattern(program10_sequence0, program10_voice0, ProgramSequencePattern.Type.Outro, 4, "Outro");
    program10_sequence0_pattern3_event0 = buildProgramSequencePatternEvent(program10_sequence0_pattern3, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern3_event1 = buildProgramSequencePatternEvent(program10_sequence0_pattern3, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern3_event2 = buildProgramSequencePatternEvent(program10_sequence0_pattern3, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern3_event3 = buildProgramSequencePatternEvent(program10_sequence0_pattern3, program10_voice0_track0, 3, 1, "G5", 0.9);

    // Instrument "Bass"
    instrument9 = buildInstrument(library2, Instrument.Type.Bass, Instrument.State.Published, "Bass");
    instrument9_meme0 = buildInstrumentMeme(instrument9, "heavy");
    instrument9_audio8 = buildInstrumentAudio(instrument9, "bass", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.62);
    instrument9_audio8_event0 = buildInstrumentAudioEvent(instrument9_audio8, 0, 1, "BLOOP", "Eb", 1.0);

    // return them all
    return ImmutableList.of(
      program10,
      program10_meme0,
      program10_voice0,
      program10_voice0_track0,
      program10_sequence0,
      program10_sequence0_pattern0,
      program10_sequence0_pattern0_event0,
      program10_sequence0_pattern0_event1,
      program10_sequence0_pattern0_event2,
      program10_sequence0_pattern0_event3,
      program10_sequence0_pattern1,
      program10_sequence0_pattern1_event0,
      program10_sequence0_pattern1_event1,
      program10_sequence0_pattern1_event2,
      program10_sequence0_pattern1_event3,
      program10_sequence0_pattern2,
      program10_sequence0_pattern2_event0,
      program10_sequence0_pattern2_event1,
      program10_sequence0_pattern2_event2,
      program10_sequence0_pattern2_event3,
      program10_sequence0_pattern3,
      program10_sequence0_pattern3_event0,
      program10_sequence0_pattern3_event1,
      program10_sequence0_pattern3_event2,
      program10_sequence0_pattern3_event3,
      instrument9,
      instrument9_meme0,
      instrument9_audio8,
      instrument9_audio8_event0
    );
  }


  /**
   Generate a Library comprising many related entities

   @param N magnitude of library to generate
   @return entities
   */
  public Collection<Object> generatedFixture(int N) {
    Collection<Object> entities = Lists.newArrayList();

    account1 = add(entities, Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Generated")
      .build());
    user1 = add(entities, User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("generated")
      .setEmail("generated@email.com")
      .setAvatarUrl("http://pictures.com/generated.gif")
      .build());
    add(entities, UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(user1.getId())
      .setType(UserRole.Type.Admin)
      .build());
    library1 = add(entities, Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("generated")
      .build());

    // Create a N-magnitude set of unique major memes
    String[] majorMemeNames = listOfUniqueRandom(N, LoremIpsum.COLORS);
    String[] minorMemeNames = listOfUniqueRandom((long) StrictMath.ceil(N >> 1), LoremIpsum.VARIANTS);
    String[] percussiveNames = listOfUniqueRandom(N, LoremIpsum.PERCUSSIVE_NAMES);

    // Generate a Percussive Instrument for each meme
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      String minorMemeName = random(minorMemeNames);
      //
      Instrument instrument = add(entities, buildInstrument(library1, Instrument.Type.Percussive, Instrument.State.Published, String.format("%s Drums", majorMemeName)));
      add(entities, InstrumentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setInstrumentId(instrument.getId())
        .setName(majorMemeName)
        .build());
      add(entities, InstrumentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setInstrumentId(instrument.getId())
        .setName(minorMemeName)
        .build());
      // audios of instrument
      for (int k = 0; k < N; k++) {
        var audio = add(entities, buildInstrumentAudio(instrument, Text.toProper(percussiveNames[k]), String.format("%s.wav", Text.toLowerSlug(percussiveNames[k])), random(0, 0.05), random(0.25, 2), random(80, 120), random(100, 4000), 0.62));
        add(entities, buildInstrumentAudioEvent(audio, 0.0, 1.0, percussiveNames[k], "X", random(0.8, 1)));
      }
      //
      log.debug("Generated Percussive-type Instrument id={}, minorMeme={}, majorMeme={}", instrument.getId(), minorMemeName, majorMemeName);
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
      Program program = add(entities, buildProgram(library1, Program.Type.Macro, Program.State.Published, String.format("%s, create %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, 0.6));
      add(entities, ProgramMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramId(program.getId())
        .setName(minorMemeName)
        .build());
      // of offset 0
      var sequence0 = add(entities, buildProgramSequence(program, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom));
      var binding0 = add(entities, ProgramSequenceBinding.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramSequenceId(sequence0.getId())
        .setProgramId(sequence0.getProgramId())
        .setOffset(0)
        .build());
      add(entities, ProgramSequenceBindingMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramId(binding0.getProgramId())
        .setProgramSequenceBindingId(binding0.getId())
        .setName(majorMemeFromName)
        .build());
      // to offset 1
      double densityTo = random(0.3, 0.9);
      double tempoTo = random(803, 120);
      var sequence1 = add(entities, buildProgramSequence(program, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo));
      var binding1 = add(entities, ProgramSequenceBinding.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramSequenceId(sequence1.getId())
        .setProgramId(sequence1.getProgramId())
        .setOffset(1)
        .build());
      add(entities, ProgramSequenceBindingMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramSequenceBindingId(binding1.getId())
        .setProgramId(binding1.getProgramId())
        .setName(majorMemeToName)
        .build());
      //
      log.debug("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", program.getId(), minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    ProgramSequence[] sequences = new ProgramSequence[N];
    for (int i = 0; i < N << 2; i++) {
      String majorMemeName = random(majorMemeNames);
      String[] sequenceNames = listOfUniqueRandom(N, LoremIpsum.ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, LoremIpsum.MUSICAL_KEYS);
      Double[] subDensities = listOfRandomValues(N);
      double tempo = random(80, 120);
      //
      Program program = add(entities, buildProgram(library1, Program.Type.Main, Program.State.Published, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, 0.6));
      add(entities, ProgramMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramId(program.getId())
        .setName(majorMemeName)
        .build());
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = add(entities, buildProgramSequence(program, total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP], tempo));
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            add(entities, buildProgramSequenceChord(sequences[iP], StrictMath.floor((float) iPC * total * 4 / N), random(LoremIpsum.MUSICAL_CHORDS)));
          }
        }
      }
      // sequence sequence binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        var binding = add(entities, ProgramSequenceBinding.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setProgramSequenceId(sequences[num].getId())
          .setProgramId(sequences[num].getProgramId())
          .setOffset(offset)
          .build());
        add(entities, buildProgramSequenceBindingMeme(binding, random(minorMemeNames)));
      }
      log.debug("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.getId(), majorMemeName, N, N << 2);
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
      Program program = add(entities, buildProgram(library1, Program.Type.Rhythm, Program.State.Published, String.format("%s Beat", majorMemeName), key, tempo, 0.6));
      trackMap.clear();
      add(entities, ProgramMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramId(program.getId())
        .setName(majorMemeName)
        .build());
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = add(entities, buildProgramVoice(program, Instrument.Type.Percussive, String.format("%s %s", majorMemeName, percussiveNames[iV])));
      }
      var sequenceBase = add(entities, buildProgramSequence(program, random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key, tempo));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        ProgramSequencePattern.Type type = 0 == iP ? ProgramSequencePattern.Type.Loop : randomRhythmPatternType();
        var pattern = add(entities, buildProgramSequencePattern(sequenceBase, voices[num], type, total, String.format("%s %s %s", majorMemeName, type.toString(), random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            String name = percussiveNames[num];
            if (!trackMap.containsKey(name))
              trackMap.put(name, add(entities, buildProgramVoiceTrack(voices[num], name)));
            add(entities, buildProgramSequencePatternEvent(pattern, trackMap.get(name), StrictMath.floor((float) iPE * total * 4 / N), random(0.25, 1.0), "X", random(0.4, 0.9)));
          }
        }
      }
      log.debug("Generated Rhythm-type Program id={}, majorMeme={} with {} patterns", program.getId(), majorMemeName, N);
    }

    return entities;
  }

  /**
   Add an entity to a collection, then return that entity

   @param to     collection
   @param entity to add
   @param <N>    type of entity
   @return entity that's been added
   */
  private <N> N add(Collection<Object> to, N entity) {
    to.add(entity);
    return entity;
  }

  public static InstrumentAudioEvent buildInstrumentAudioEvent(InstrumentAudio instrumentAudio, double position, double duration, String name, String note, double velocity) {
    return InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setName(name)
      .setPosition(position)
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity)
      .build();
  }

  public static InstrumentAudioChord buildInstrumentAudioChord(InstrumentAudio instrumentAudio, double position, String name) {
    return InstrumentAudioChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setName(name)
      .setPosition(position)
      .build();
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, String waveformKey, double start, double length, double tempo, double pitch, double density) {
    return InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName(name)
      .setWaveformKey(waveformKey)
      .setStart(start)
      .setLength(length)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static User buildUser(String name, String email, String avatarUrl) {
    return User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setEmail(email)
      .setAvatarUrl(avatarUrl)
      .setName(name)
      .build();
  }

  public static Library buildLibrary(Account account, String name) {
    return Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account.getId())
      .setName(name)
      .build();
  }

  public static Account buildAccount() {
    return Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
  }

  public static Account buildAccount(String name) {
    return buildAccount().toBuilder()
      .setName(name)
      .build();
  }

  public static UserRole buildUserRole(User user, UserRole.Type type) {
    return UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(user.getId())
      .setType(type)
      .build();
  }

  public static AccountUser buildAccountUser(Account account, User user) {
    return AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account.getId())
      .setUserId(user.getId())
      .build();
  }

  public static Program buildProgram(Library library, Program.Type type, Program.State state, String name, String key, double tempo, double density) {
    return Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static ProgramMeme buildProgramMeme(Program program, String name) {
    return ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequence buildProgramSequence(Program program, int total, String name, double density, String key, double tempo) {
    return ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setTotal(total)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static ProgramSequenceBinding buildProgramSequenceBinding(ProgramSequence programSequence, int offset) {
    return ProgramSequenceBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setOffset(offset)
      .build();
  }

  public static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    return ProgramSequenceBindingMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequenceBinding.getProgramId())
      .setProgramSequenceBindingId(programSequenceBinding.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequenceChord buildProgramSequenceChord(ProgramSequence programSequence, double position, String name) {
    return ProgramSequenceChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramSequenceId(programSequence.getId())
      .setProgramId(programSequence.getProgramId())
      .setPosition(position)
      .setName(name)
      .build();
  }

  public static ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(Instrument.Type type, ProgramSequenceChord programSequenceChord, String notes) {
    return ProgramSequenceChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequenceChord.getProgramId())
      .setProgramSequenceChordId(programSequenceChord.getId())
      .setType(type)
      .setNotes(notes)
      .build();
  }

  public static ProgramVoice buildProgramVoice(Program program, Instrument.Type type, String name) {
    return ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setType(type)
      .setName(name)
      .build();
  }

  public static ProgramVoiceTrack buildProgramVoiceTrack(ProgramVoice programVoice, String name) {
    return ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programVoice.getProgramId())
      .setProgramVoiceId(programVoice.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequencePattern buildProgramSequencePattern(ProgramSequence programSequence, ProgramVoice programVoice, ProgramSequencePattern.Type type, int total, String name) {
    return ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setProgramVoiceId(programVoice.getId())
      .setType(type)
      .setTotal(total)
      .setName(name)
      .build();
  }

  public static ProgramSequencePatternEvent buildProgramSequencePatternEvent(ProgramSequencePattern programSequencePattern, ProgramVoiceTrack programVoiceTrack, double position, double duration, String note, double velocity) {
    return ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequencePattern.getProgramId())
      .setProgramSequencePatternId(programSequencePattern.getId())
      .setProgramVoiceTrackId(programVoiceTrack.getId())
      .setPosition(position)
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity)
      .build();
  }

  public static Instrument buildInstrument(Library library, Instrument.Type type, Instrument.State state, String name) {
    return Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .build();
  }

  public static InstrumentMeme buildInstrumentMeme(Instrument instrument, String name) {
    return InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName(name)
      .build();
  }

  public static Chain buildChain(Account account, String name, Chain.Type type, Chain.State state, Instant startAt, @Nullable Instant stopAt, @Nullable String embedKey) {
    Chain.Builder builder = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account.getId())
      .setName(name)
      .setType(type)
      .setState(state)
      .setStartAt(Value.formatIso8601UTC(startAt));
    if (Objects.nonNull(stopAt))
      builder.setStopAt(Value.formatIso8601UTC(stopAt));
    if (Objects.nonNull(embedKey))
      builder.setEmbedKey(embedKey);
    return builder.build();
  }

  public static ChainBinding buildChainBinding(Chain chain, Program program) {
    return ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(program.getId())
      .setType(ChainBinding.Type.Program)
      .build();
  }

  public static ChainBinding buildChainBinding(Chain chain, Instrument instrument) {
    return ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(instrument.getId())
      .setType(ChainBinding.Type.Instrument)
      .build();
  }

  public static ChainBinding buildChainBinding(Chain chain, Library library) {
    return ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(library.getId())
      .setType(ChainBinding.Type.Library)
      .build();
  }

  public static Segment buildSegment(Chain chain, int offset, Segment.State state, Instant beginAt, @Nullable Instant endAt, String key, int total, double density, double tempo, String storageKey, String outputEncoder) {
    Segment.Builder builder = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setOutputEncoder(outputEncoder)
      .setChainId(chain.getId())
      .setOffset(offset)
      .setState(state)
      .setBeginAt(Value.formatIso8601UTC(beginAt))
      .setKey(key)
      .setTotal(total)
      .setDensity(density)
      .setTempo(tempo)
      .setStorageKey(storageKey);

    if (Objects.nonNull(endAt))
      builder.setEndAt(Value.formatIso8601UTC(endAt));

    return builder.build();
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(String rolesCSV) {
    return new HubClientAccess().setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

}
