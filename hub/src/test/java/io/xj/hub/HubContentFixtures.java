// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
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
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import io.xj.lib.entity.Entities;
import io.xj.hub.client.HubClientAccess;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  public InstrumentAudioEvent instrument8_audio8bleep_event0;
  public InstrumentAudioEvent instrument8_audio8kick_event0;
  public InstrumentAudioEvent instrument8_audio8snare_event0;
  public InstrumentAudioEvent instrument8_audio8toot_event0;
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
  public UserRole userRole2a;
  public UserRole userRole3a;

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
      .setRoleTypes(io.xj.lib.entity.common.User.userRoleTypesFromCsv(rolesCSV));
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
      .setRoleTypes(io.xj.lib.entity.common.User.userRoleTypesFromCsv(rolesCSV));
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
      .setRoleTypes(io.xj.lib.entity.common.User.userRoleTypesFromCsv(rolesCSV));
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
      .setRoleTypes(io.xj.lib.entity.common.User.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(String rolesCSV) {
    return new HubClientAccess().setRoleTypes(io.xj.lib.entity.common.User.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   A whole library of mock content

   @param returnParentEntities true if we only want to return the parent entities and library, in addition to the content
   @return collection of entities
   */
  public Collection<Object> setupFixtureB1(Boolean returnParentEntities) {

    // Account "bananas"
    account1 = IntegrationTestingFixtures.buildAccount("bananas");

    // Library "house"
    library2 = IntegrationTestingFixtures.buildLibrary(account1, "house");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = IntegrationTestingFixtures.buildUser("john", "john@email.com", "http://pictures.com/john.gif");
    userRole2a = IntegrationTestingFixtures.buildUserRole(user2, UserRole.Type.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = IntegrationTestingFixtures.buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    userRole3a = IntegrationTestingFixtures.buildUserRole(user3, UserRole.Type.User);
    accountUser1a = IntegrationTestingFixtures.buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Macro, Program.State.Published, "Tropical, Wild to Cozy", "C", 120.0, 0.6);
    program4_meme0 = IntegrationTestingFixtures.buildProgramMeme(program4, "Tropical");
    //
    program4_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program4, 0, "Start Wild", 0.6, "C", 125.0);
    program3_sequence0_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program3_sequence0_binding0, "Wild");
    //
    program4_sequence1 = IntegrationTestingFixtures.buildProgramSequence(program4, 0, "Intermediate", 0.4, "Bb minor", 115.0);
    program4_sequence1_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = IntegrationTestingFixtures.buildProgramSequence(program4, 0, "Finish Cozy", 0.4, "Ab minor", 125.0);
    program4_sequence2_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Main, Program.State.Published, "Main Jam", "C minor", 140, 0.6);
    program5_meme0 = IntegrationTestingFixtures.buildProgramMeme(program5, "Outlook");
    //
    program5_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program5, 16, "Intro", 0.5, "G major", 135.0);
    program5_sequence0_chord0 = IntegrationTestingFixtures.buildProgramSequenceChord(program5_sequence0, 0.0, "G major");
    program5_sequence0_chord0_voicing = IntegrationTestingFixtures.buildProgramSequenceChordVoicing(Instrument.Type.Pad, program5_sequence0_chord0, "G3, B3, D4");
    program5_sequence0_chord1 = IntegrationTestingFixtures.buildProgramSequenceChord(program5_sequence0, 8.0, "Ab minor");
    program5_sequence0_chord1_voicing = IntegrationTestingFixtures.buildProgramSequenceChordVoicing(Instrument.Type.Pad, program5_sequence0_chord1, "Ab3, Db3, F4");
    program5_sequence0_chord2 = IntegrationTestingFixtures.buildProgramSequenceChord(program5_sequence0, 75.0, "G-9"); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total
    program5_sequence0_chord2_voicing = IntegrationTestingFixtures.buildProgramSequenceChordVoicing(Instrument.Type.Pad, program5_sequence0_chord2, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = IntegrationTestingFixtures.buildProgramSequence(program5, 32, "Drop", 0.5, "G minor", 135.0);
    program5_sequence1_chord0 = IntegrationTestingFixtures.buildProgramSequenceChord(program5_sequence1, 0.0, "C major");
    program5_sequence1_chord1 = IntegrationTestingFixtures.buildProgramSequenceChord(program5_sequence1, 8.0, "Bb minor");
    program5_sequence1_binding1 = IntegrationTestingFixtures.buildProgramSequenceBinding(program5_sequence1, 1);
    program5_sequence1_binding1_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program5_sequence1_binding1, "Pessimism");

    // A basic beat
    program35 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "Basic Beat", "C", 121, 0.6);
    program35_meme0 = IntegrationTestingFixtures.buildProgramMeme(program35, "Basic");
    program35_voice0 = IntegrationTestingFixtures.buildProgramVoice(program35, Instrument.Type.Percussive, "Drums");
    program35_voice0_track0 = IntegrationTestingFixtures.buildProgramVoiceTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = IntegrationTestingFixtures.buildProgramVoiceTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = IntegrationTestingFixtures.buildProgramVoiceTrack(program35_voice0, "KICK");
    program35_voice0_track3 = IntegrationTestingFixtures.buildProgramVoiceTrack(program35_voice0, "SNARL");
    //
    program3_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program35, 16, "Base", 0.5, "C", 110.3);
    program35_sequence0_pattern0 = IntegrationTestingFixtures.buildProgramSequencePattern(program3_sequence0, program35_voice0, ProgramSequencePattern.Type.Loop, 4, "Drop");
    program35_sequence0_pattern0_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0, 1.0, "C2", 1.0);
    program35_sequence0_pattern0_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0, 1.0, "G5", 0.8);
    program35_sequence0_pattern0_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5, 1.0, "C2", 0.6);
    program35_sequence0_pattern0_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0, 1.0, "G5", 0.9);
    //
    program35_sequence0_pattern1 = IntegrationTestingFixtures.buildProgramSequencePattern(program3_sequence0, program35_voice0, ProgramSequencePattern.Type.Loop, 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0, 1.0, "B5", 0.9);
    program35_sequence0_pattern1_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0, 1.0, "D2", 1.0);
    program35_sequence0_pattern1_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5, 1.0, "E4", 0.7);
    program35_sequence0_pattern1_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0, 1.0, "c3", 0.5);

    // Detail Sequence
    program6 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Detail, Program.State.Published, "Beat Jam", "D#", 150, 0.6);
    program7 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Detail, Program.State.Published, "Detail Jam", "Cb minor", 170, 0.6);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database inserts, so ordered from parent -> child
    List<Object> parentEntities = ImmutableList.of(
      account1,
      library2,
      user2,
      userRole2a,
      user3,
      userRole3a,
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
    program3 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Macro, Program.State.Published, "Tangy, Chunky to Smooth", "G minor", 120.0, 0.6);
    program3_meme0 = IntegrationTestingFixtures.buildProgramMeme(program3, "Tangy");
    //
    program3_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program3, 0, "Start Chunky", 0.4, "G minor", 115.0);
    program3_sequence0_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = IntegrationTestingFixtures.buildProgramSequence(program3, 0, "Finish Smooth", 0.6, "C", 125.0);
    program3_sequence1_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Main, Program.State.Published, "Next Jam", "Db minor", 140, 0.6);
    program15_meme0 = IntegrationTestingFixtures.buildProgramMeme(program15, "Hindsight");
    //
    program15_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program15, 16, "Intro", 0.5, "G minor", 135.0);
    program15_sequence0_chord0 = IntegrationTestingFixtures.buildProgramSequenceChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord1 = IntegrationTestingFixtures.buildProgramSequenceChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = IntegrationTestingFixtures.buildProgramSequence(program15, 32, "Outro", 0.5, "A major", 135.0);
    program15_sequence1_chord0 = IntegrationTestingFixtures.buildProgramSequenceChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord1 = IntegrationTestingFixtures.buildProgramSequenceChord(program15_sequence1, 8.0, "Bb major");
    program15_sequence1_binding0 = IntegrationTestingFixtures.buildProgramSequenceBinding(program15_sequence1, 1);
    program15_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program15_sequence1_binding0, "Pride");
    program15_sequence1_binding0_meme1 = IntegrationTestingFixtures.buildProgramSequenceBindingMeme(program15_sequence1_binding0, "Shame");

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
    program9 = IntegrationTestingFixtures.buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "Basic Beat", "C", 121, 0.6);
    program9_meme0 = IntegrationTestingFixtures.buildProgramMeme(program9, "Basic");
    //
    program9_voice0 = IntegrationTestingFixtures.buildProgramVoice(program9, Instrument.Type.Percussive, "Drums");
    program9_voice0_track0 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "BLEEP");
    program9_voice0_track1 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "BLEIP");
    program9_voice0_track2 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "BLEAP");
    program9_voice0_track3 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "BLEEEP");
    program9_voice0_track4 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "CLOCK");
    program9_voice0_track5 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "SNORT");
    program9_voice0_track6 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "KICK");
    program9_voice0_track7 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "SNARL");
    program9_voice0_track8 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "KIICK");
    program9_voice0_track9 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "SNARR");
    program9_voice0_track10 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "KEICK");
    program9_voice0_track11 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "SNAER");
    program9_voice0_track12 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "TOOT");
    program9_voice0_track13 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "TOOOT");
    program9_voice0_track14 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "TOOTE");
    program9_voice0_track15 = IntegrationTestingFixtures.buildProgramVoiceTrack(program9_voice0, "TOUT");
    //
    program9_sequence0 = IntegrationTestingFixtures.buildProgramSequence(program9, 16, "Base", 0.5, "C", 110.3);
    //
    program9_sequence0_pattern0 = IntegrationTestingFixtures.buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Intro, 4, "Intro");
    program9_sequence0_pattern0_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0);
    program9_sequence0_pattern0_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8);
    program9_sequence0_pattern0_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern0_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern1 = IntegrationTestingFixtures.buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop A");
    program9_sequence0_pattern1_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0);
    program9_sequence0_pattern1_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8);
    program9_sequence0_pattern1_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern1_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern2 = IntegrationTestingFixtures.buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Loop, 4, "Loop B");
    program9_sequence0_pattern2_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9);
    program9_sequence0_pattern2_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0);
    program9_sequence0_pattern2_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5, 1, "E4", 0.7);
    program9_sequence0_pattern2_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5);
    //
    program9_sequence0_pattern3 = IntegrationTestingFixtures.buildProgramSequencePattern(program9_sequence0, program9_voice0, ProgramSequencePattern.Type.Outro, 4, "Outro");
    program9_sequence0_pattern3_event0 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0);
    program9_sequence0_pattern3_event1 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8);
    program9_sequence0_pattern3_event2 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern3_event3 = IntegrationTestingFixtures.buildProgramSequencePatternEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9);

    // Instrument "808"
    instrument8 = IntegrationTestingFixtures.buildInstrument(library2, Instrument.Type.Percussive, Instrument.State.Published, "808 Drums");
    instrument8_meme0 = IntegrationTestingFixtures.buildInstrumentMeme(instrument8, "heavy");
    instrument8_audio8kick = IntegrationTestingFixtures.buildInstrumentAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62);
    instrument8_audio8kick_event0 = IntegrationTestingFixtures.buildInstrumentAudioEvent(instrument8_audio8kick, 0, 1, "KICK", "Eb1", 1.0);
    instrument8_audio8snare = IntegrationTestingFixtures.buildInstrumentAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 0.62);
    instrument8_audio8snare_event0 = IntegrationTestingFixtures.buildInstrumentAudioEvent(instrument8_audio8snare, 0, 1, "SNARE", "Ab1", 0.8);
    instrument8_audio8bleep = IntegrationTestingFixtures.buildInstrumentAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 0.62);
    instrument8_audio8bleep_event0 = IntegrationTestingFixtures.buildInstrumentAudioEvent(instrument8_audio8bleep, 0, 1, "BLEEP", "Ab2", 0.8);
    instrument8_audio8toot = IntegrationTestingFixtures.buildInstrumentAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 0.62);
    instrument8_audio8toot_event0 = IntegrationTestingFixtures.buildInstrumentAudioEvent(instrument8_audio8toot, 0, 1, "TOOT", "Ab3", 0.8);

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

}
