//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.IntegrationTestingFixtures.*;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
@SuppressWarnings("SameParameterValue")
public class HubContentFixtures {
  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public AccountUser accountUser1a;
  public Instrument instrument8;
  public InstrumentAudio instrument8_audio8bleep;
  public InstrumentAudio instrument8_audio8kick;
  public InstrumentAudio instrument8_audio8snare;
  public InstrumentAudio instrument8_audio8toot;
  public InstrumentMeme instrument8_meme0;
  public Library library1;
  public Library library2;
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
  public ProgramMeme program15_meme0;
  public ProgramMeme program35_meme0;
  public ProgramMeme program3_meme0;
  public ProgramMeme program4_meme0;
  public ProgramMeme program5_meme0;
  public ProgramMeme program9_meme0;
  public ProgramSequence program15_sequence0;
  public ProgramSequence program15_sequence1;
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
  public ProgramSequenceBinding program4_sequence1_binding0;
  public ProgramSequenceBinding program4_sequence2_binding0;
  public ProgramSequenceBinding program5_sequence0_binding0;
  public ProgramSequenceBinding program5_sequence1_binding1;
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
  public ProgramSequenceBindingMeme program5_sequence1_binding1_meme0;
  public ProgramSequenceChord program15_sequence0_chord0;
  public ProgramSequenceChord program15_sequence0_chord1;
  public ProgramSequenceChord program15_sequence1_chord0;
  public ProgramSequenceChord program15_sequence1_chord1;
  public ProgramSequenceChord program5_sequence0_chord0;
  public ProgramSequenceChord program5_sequence0_chord1;
  public ProgramSequenceChord program5_sequence0_chord2;
  public ProgramSequenceChord program5_sequence1_chord0;
  public ProgramSequenceChord program5_sequence1_chord1;
  public ProgramSequenceChordVoicing program5_sequence0_chord0_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord1_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord2_voicing;
  public ProgramSequencePattern program35_sequence0_pattern0;
  public ProgramSequencePattern program35_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern0;
  public ProgramSequencePattern program9_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern2;
  public ProgramSequencePattern program9_sequence0_pattern3;
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
  public ProgramVoice program35_voice0;
  public ProgramVoice program9_voice0;
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
  public User user2;
  public User user3;

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
   A whole library of mock content

   @param returnParentEntities true if we only want to return the parent entities and library, in addition to the content
   @return collection of entities
   */
  public Collection<Object> setupFixtureB1(Boolean returnParentEntities) {

    // Account "bananas"
    account1 = buildAccount("bananas");

    // Library "house"
    library2 = buildLibrary(account1, "house");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = buildUser("john", "john@email.com", "http://pictures.com/john.gif", "Admin");

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User");
    accountUser1a = buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", "C", 120.0f, 0.6f);
    program4_meme0 = buildProgramMeme(program4, "Tropical");
    //
    program4_sequence0 = buildProgramSequence(program4, (short) 0, "Start Wild", 0.6f, "C", 125.0f);
    program3_sequence0_binding0 = buildProgramSequenceBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program3_sequence0_binding0, "Wild");
    //
    program4_sequence1 = buildProgramSequence(program4, (short) 0, "Intermediate", 0.4f, "Bb minor", 115.0f);
    program4_sequence1_binding0 = buildProgramSequenceBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = buildProgramSequence(program4, (short) 0, "Finish Cozy", 0.4f, "Ab minor", 125.0f);
    program4_sequence2_binding0 = buildProgramSequenceBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = buildProgramSequenceBindingMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = buildProgram(library2, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140f, 0.6f);
    program5_meme0 = buildProgramMeme(program5, "Outlook");
    //
    program5_sequence0 = buildProgramSequence(program5, (short) 16, "Intro", 0.5f, "G major", 135.0f);
    program5_sequence0_chord0 = buildProgramSequenceChord(program5_sequence0, 0.0, "G major");
    program5_sequence0_chord0_voicing = buildProgramSequenceChordVoicing(program5_sequence0_chord0, InstrumentType.Pad, "G3, B3, D4");
    program5_sequence0_chord1 = buildProgramSequenceChord(program5_sequence0, 8.0, "Ab minor");
    program5_sequence0_chord1_voicing = buildProgramSequenceChordVoicing(program5_sequence0_chord1, InstrumentType.Pad, "Ab3, Db3, F4");
    program5_sequence0_chord2 = buildProgramSequenceChord(program5_sequence0, 75.0, "G-9"); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total
    program5_sequence0_chord2_voicing = buildProgramSequenceChordVoicing(program5_sequence0_chord2, InstrumentType.Pad, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = buildProgramSequenceBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = buildProgramSequence(program5, (short) 32, "Drop", 0.5f, "G minor", 135.0f);
    program5_sequence1_chord0 = buildProgramSequenceChord(program5_sequence1, 0.0, "C major");
    program5_sequence1_chord1 = buildProgramSequenceChord(program5_sequence1, 8.0, "Bb minor");
    program5_sequence1_binding1 = buildProgramSequenceBinding(program5_sequence1, 1);
    program5_sequence1_binding1_meme0 = buildProgramSequenceBindingMeme(program5_sequence1_binding1, "Pessimism");

    // A basic beat
    program35 = buildProgram(library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121f, 0.6f);
    program35_meme0 = buildProgramMeme(program35, "Basic");
    program35_voice0 = buildProgramVoice(program35, InstrumentType.Drum, "Drums");
    program35_voice0_track0 = buildProgramVoiceTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = buildProgramVoiceTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = buildProgramVoiceTrack(program35_voice0, "KICK");
    program35_voice0_track3 = buildProgramVoiceTrack(program35_voice0, "SNARL");
    //
    program3_sequence0 = buildProgramSequence(program35, (short) 16, "Base", 0.5f, "C", 110.3f);
    program35_sequence0_pattern0 = buildProgramSequencePattern(program3_sequence0, program35_voice0, ProgramSequencePatternType.Loop, (short) 4, "Drop");
    program35_sequence0_pattern0_event0 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0f, 1.0f, "C2", 1.0f);
    program35_sequence0_pattern0_event1 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0f, 1.0f, "G5", 0.8f);
    program35_sequence0_pattern0_event2 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5f, 1.0f, "C2", 0.6f);
    program35_sequence0_pattern0_event3 = buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0f, 1.0f, "G5", 0.9f);
    //
    program35_sequence0_pattern1 = buildProgramSequencePattern(program3_sequence0, program35_voice0, ProgramSequencePatternType.Loop, (short) 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0f, 1.0f, "B5", 0.9f);
    program35_sequence0_pattern1_event1 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0f, 1.0f, "D2", 1.0f);
    program35_sequence0_pattern1_event2 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5f, 1.0f, "E4", 0.7f);
    program35_sequence0_pattern1_event3 = buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0f, 1.0f, "c3", 0.5f);

    // Detail Sequence
    program6 = buildProgram(library2, ProgramType.Detail, ProgramState.Published, "Beat Jam", "D#", 150f, 0.6f);
    program7 = buildProgram(library2, ProgramType.Detail, ProgramState.Published, "Detail Jam", "Cb minor", 170f, 0.6f);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database inserts, so ordered from parent -> child
    List<Object> parentEntities = ImmutableList.of(
      account1,
      library2,
      user2,
      user3,
      accountUser1a
    );

    // List of all entities in the library
    // ORDER IS IMPORTANT because this list will be used for real database inserts, so ordered from parent -> child
    List<Object> libraryContent = ImmutableList.of(
      program35,
      program35_voice0,
      program35_voice0_track0,
      program35_voice0_track1,
      program35_voice0_track2,
      program35_voice0_track3,
      program35_meme0,
      program3_sequence0,
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
      program3_sequence0_binding0,
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
      program5_sequence1_binding1,
      program5_sequence1_binding1_meme0,
      program5_sequence1_chord0,
      program5_sequence1_chord1,
      program6,
      program7
    );

    return returnParentEntities ?
      Stream.concat(parentEntities.stream(), libraryContent.stream()).collect(Collectors.toList()) :
      libraryContent;
  }

  /**
   Library of Content B-2 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public Collection<Object> setupFixtureB2() {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tangy, Chunky to Smooth", "G minor", 120.0f, 0.6f);
    program3_meme0 = buildProgramMeme(program3, "Tangy");
    //
    program3_sequence0 = buildProgramSequence(program3, (short) 0, "Start Chunky", 0.4f, "G minor", 115.0f);
    program3_sequence0_binding0 = buildProgramSequenceBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = buildProgramSequence(program3, (short) 0, "Finish Smooth", 0.6f, "C", 125.0f);
    program3_sequence1_binding0 = buildProgramSequenceBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = buildProgramSequenceBindingMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = buildProgram(library2, ProgramType.Main, ProgramState.Published, "Next Jam", "Db minor", 140f, 0.6f);
    program15_meme0 = buildProgramMeme(program15, "Hindsight");
    //
    program15_sequence0 = buildProgramSequence(program15, (short) 16, "Intro", 0.5f, "G minor", 135.0f);
    program15_sequence0_chord0 = buildProgramSequenceChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord1 = buildProgramSequenceChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_binding0 = buildProgramSequenceBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = buildProgramSequenceBindingMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = buildProgramSequence(program15, (short) 32, "Outro", 0.5f, "A major", 135.0f);
    program15_sequence1_chord0 = buildProgramSequenceChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord1 = buildProgramSequenceChord(program15_sequence1, 8.0, "Bb major");
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
      program15_sequence0_chord1,
      program15_sequence0_binding0,
      program15_sequence0_binding0_meme0,
      program15_sequence1,
      program15_sequence1_chord0,
      program15_sequence1_chord1,
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
    program9 = buildProgram(library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121f, 0.6f);
    program9_meme0 = buildProgramMeme(program9, "Basic");
    //
    program9_voice0 = buildProgramVoice(program9, InstrumentType.Drum, "Drums");
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
    program9_sequence0 = buildProgramSequence(program9, (short) 16, "Base", 0.5f, "C", 110.3f);
    //
    program9_sequence0_pattern0 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Intro, (short) 4, "Intro");
    program9_sequence0_pattern0_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern0_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern0_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern0_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern1 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Loop, (short) 4, "Loop A");
    program9_sequence0_pattern1_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern1_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern1_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern1_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern2 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Loop, (short) 4, "Loop B");
    program9_sequence0_pattern2_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9f);
    program9_sequence0_pattern2_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0f);
    program9_sequence0_pattern2_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5f, 1, "E4", 0.7f);
    program9_sequence0_pattern2_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5f);
    //
    program9_sequence0_pattern3 = buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Outro, (short) 4, "Outro");
    program9_sequence0_pattern3_event0 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern3_event1 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern3_event2 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern3_event3 = buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9f);

    // Instrument "808"
    instrument8 = buildInstrument(library2, InstrumentType.Drum, InstrumentState.Published, "808 Drums");
    instrument8_meme0 = buildInstrumentMeme(instrument8, "heavy");
    instrument8_audio8kick = buildInstrumentAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);
    instrument8_audio8snare = buildInstrumentAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01f, 1.5f, 120.0f, 0.62f, "SNARE", "Eb", 1.0f);
    instrument8_audio8bleep = buildInstrumentAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01f, 1.5f, 120.0f, 0.62f, "BLEEP", "Eb", 1.0f);
    instrument8_audio8toot = buildInstrumentAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01f, 1.5f, 120.0f, 0.62f, "TOOT", "Eb", 1.0f);

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
      instrument8_audio8snare,
      instrument8_audio8bleep,
      instrument8_audio8toot
    );
  }

}
