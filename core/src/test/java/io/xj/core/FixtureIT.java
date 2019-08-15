//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core;

import io.xj.core.model.account.Account;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.library.Library;
import io.xj.core.model.program.PatternType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.user.User;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.time.Instant;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class FixtureIT extends CoreIT {
  protected Account account1;
  protected Audio audio8kick;
  protected Audio audio8snare;
  protected Audio audio8bleep;
  protected Audio audio8toot;
  protected Audio audioKick;
  protected Audio audioSnare;
  protected Audio audioHihat;
  protected Chain chain3;
  protected Instrument instrument201;
  protected Instrument instrument202;
  protected Instrument instrument251;
  protected Instrument instrument8;
  protected Instrument instrument9;
  protected Library library10000001;
  protected Library library10000002;
  protected Library library1;
  protected Library library2;
  protected Program program15;
  protected Program program1;
  protected Program program35;
  protected Program program3;
  protected Program program4;
  protected Program program5;
  protected Program program6;
  protected Program program701;
  protected Program program702;
  protected Program program703;
  protected Program program751;
  protected Program program7;
  protected Program program9;
  protected Segment segment17;
  protected Segment segment1;
  protected Segment segment2;
  protected Segment segment3;
  protected Segment segment4;
  protected Segment segment5;
  protected Segment segment6;
  protected SequenceBinding program15_binding0;
  protected SequenceBinding program15_binding1;
  protected SequenceBinding program3_binding0;
  protected SequenceBinding program3_binding1;
  protected SequenceBinding program4_binding0;
  protected SequenceBinding program4_binding1;
  protected SequenceBinding program4_binding2;
  protected SequenceBinding program5_binding0;
  protected SequenceBinding program5_binding1;
  protected User user1;
  protected User user101;
  protected User user2;
  protected User user3;
  protected Voice voiceDrums;

  /**
   Library of Content A (shared test fixture)
   */
  protected void insertFixtureA() {
    // account
    account1 = insert(newAccount(1, "testing"));
    user101 = insert(newUser(101, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(101, UserRoleType.Admin));

    // Library content all created at this known time
    Instant at = Instant.parse("2014-08-12T12:17:02.527142Z");
    library10000001 = insert(newLibrary(10000001, 1, "leaves", at));

    // Instrument 201
    instrument201 = newInstrument(201, 101, 10000001, InstrumentType.Percussive, InstrumentState.Published, "808 Drums", at);
    instrument201.add(newInstrumentMeme("Ants"));
    instrument201.add(newInstrumentMeme("Mold"));
    //
    Audio audio402 = instrument201.add(newAudio("Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument201.add(newAudioChord(audio402, 0.0, "E minor"));
    instrument201.add(newAudioChord(audio402, 4.0, "A major"));
    instrument201.add(newAudioChord(audio402, 8.0, "B minor"));
    instrument201.add(newAudioChord(audio402, 12.0, "F# major"));
    instrument201.add(newAudioChord(audio402, 16.0, "Ab7"));
    instrument201.add(newAudioChord(audio402, 20.0, "Bb7"));
    //
    Audio audio401 = instrument201.add(newAudio("Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument201.add(newAudioEvent(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    instrument201.add(newAudioEvent(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    instrument201.add(newAudioEvent(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    instrument201.add(newAudioEvent(audio401, 3.0, 1.0, "SNARE", "B", 0.8));
    //
    insert(instrument201);

    // Instrument 202
    instrument202 = newInstrument(202, 101, 10000001, InstrumentType.Percussive, InstrumentState.Published, "909 Drums", at);
    instrument202.add(newInstrumentMeme("Peel"));
    insert(instrument202);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program701 = newProgram(701, 101, 10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, at);
    program701.add(newProgramMeme("Ants"));
    Sequence sequence902 = program701.add(newSequence(16, "decay", 0.25, "F#", 110.3));
    program701.add(newSequenceChord(sequence902, 0.0, "G minor"));
    program701.add(newSequenceChord(sequence902, 4.0, "C major"));
    program701.add(newSequenceChord(sequence902, 8.0, "F7"));
    program701.add(newSequenceChord(sequence902, 12.0, "G7"));
    program701.add(newSequenceChord(sequence902, 16.0, "F minor"));
    program701.add(newSequenceChord(sequence902, 20.0, "Bb major"));
    SequenceBinding binding902_0 = program701.add(newSequenceBinding(sequence902, 0));
    SequenceBinding binding902_1 = program701.add(newSequenceBinding(sequence902, 1));
    SequenceBinding binding902_2 = program701.add(newSequenceBinding(sequence902, 2));
    SequenceBinding binding902_3 = program701.add(newSequenceBinding(sequence902, 3));
    SequenceBinding binding902_4 = program701.add(newSequenceBinding(sequence902, 4));
    program701.add(newSequenceBinding(sequence902, 5));
    program701.add(newSequenceBindingMeme(binding902_0, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_1, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_2, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_3, "Rocks"));
    program701.add(newSequenceBindingMeme(binding902_1, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_2, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_3, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_4, "Noise"));
    insert(program701);

    // Program 702, rhythm-type, has unbound sequence with pattern with events
    program702 = newProgram(702, 101, 10000001, ProgramType.Rhythm, ProgramState.Published, "coconuts", "F#", 110.3, at);
    program702.add(newProgramMeme("Ants"));
    voiceDrums = program702.add(newVoice(InstrumentType.Percussive, "Drums"));
    Sequence sequence702a = program702.add(newSequence(16, "Base", 0.5, "C", 110.3));
    Pattern pattern901 = program702.add(newPattern(sequence702a, voiceDrums, PatternType.Loop, 16, "growth"));
    program702.add(newPatternEvent(pattern901, 0.0, 1.0, "BOOM", "C", 1.0));
    program702.add(newPatternEvent(pattern901, 1.0, 1.0, "SMACK", "G", 0.8));
    program702.add(newPatternEvent(pattern901, 2.5, 1.0, "BOOM", "C", 0.6));
    program702.add(newPatternEvent(pattern901, 3.0, 1.0, "SMACK", "G", 0.9));
    insert(program702);

    // Program 703
    program703 = newProgram(703, 101, 10000001, ProgramType.Main, ProgramState.Published, "bananas", "Gb", 100.6, at);
    program703.add(newProgramMeme("Peel"));
    insert(program703);

    // DELIBERATELY UNUSED stuff that should not get used because it's in a different library
    library10000002 = insert(newLibrary(10000002, 1, "Garbage Library", at));
    //
    instrument251 = newInstrument(251, 101, 10000002, InstrumentType.Percussive, InstrumentState.Published, "Garbage Instrument", at);
    instrument251.add(newInstrumentMeme("Garbage Meme"));
    insert(instrument251);
    //
    program751 = newProgram(751, 101, 10000002, ProgramType.Rhythm, ProgramState.Published, "coconuts", "F#", 110.3, at);
    program751.add(newProgramMeme("Ants"));
    Voice voiceGarbage = program751.add(newVoice(InstrumentType.Percussive, "Garbage"));
    Sequence sequence751a = program751.add(newSequence(16, "Base", 0.5, "C", 110.3));
    Pattern pattern951 = program751.add(newPattern(sequence751a, voiceGarbage, PatternType.Loop, 16, "Garbage"));
    program751.add(newPatternEvent(pattern951, 0.0, 1.0, "GR", "C", 1.0));
    program751.add(newPatternEvent(pattern951, 1.0, 1.0, "BAG", "G", 0.8));
    program751.add(newPatternEvent(pattern951, 2.5, 1.0, "GR", "C", 0.6));
    program751.add(newPatternEvent(pattern951, 3.0, 1.0, "BAG", "G", 0.9));
    insert(program751);
  }

  /**
   Library of Content B-1 (shared test fixture)
   */
  protected void insertFixtureB1() {
    // Account "bananas"
    account1 = insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newUserRole(3, UserRoleType.User));
    insert(newAccountUser(1, 3));

    // Library "house"
    library2 = insert(newLibrary(2, 1, "house", now()));

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = newProgram(4, 3, 2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", "C", 120.0, now());
    program4.add(newProgramMeme("Tropical"));
    //
    Sequence sequence4a = program4.add(newSequence(0, "Start Wild", 0.6, "C", 125.0));
    program4_binding0 = program4.add(newSequenceBinding(sequence4a, 0));
    program4.add(newSequenceBindingMeme(program4_binding0, "Wild"));
    //
    Sequence sequence4b = program4.add(newSequence(0, "Intermediate", 0.4, "Bb minor", 115.0));
    program4_binding1 = program4.add(newSequenceBinding(sequence4b, 1));
    program4.add(newSequenceBindingMeme(program4_binding1, "Cozy"));
    program4.add(newSequenceBindingMeme(program4_binding1, "Wild"));
    //
    Sequence sequence4c = program4.add(newSequence(0, "Finish Cozy", 0.4, "Ab minor", 125.0));
    program4_binding2 = program4.add(newSequenceBinding(sequence4c, 2));
    program4.add(newSequenceBindingMeme(program4_binding2, "Cozy"));
    //
    insert(program4);

    // Main program
    program5 = newProgram(5, 3, 2, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140, now());
    program5.add(newProgramMeme("Outlook"));
    //
    Sequence sequence5a = program5.add(newSequence(16, "Intro", 0.5, "G major", 135.0));
    program5.add(newSequenceChord(sequence5a, 0.0, "G major"));
    program5.add(newSequenceChord(sequence5a, 8.0, "Ab minor"));
    program5.add(newSequenceChord(sequence5a, 75.0, "G-9")); // [#154090557] this Chord should be ignored, because it's past the end of the main-pattern total
    program5_binding0 = program5.add(newSequenceBinding(sequence5a, 0));
    program5.add(newSequenceBindingMeme(program5_binding0, "Optimism"));
    //
    Sequence sequence5b = program5.add(newSequence(32, "Drop", 0.5, "G minor", 135.0));
    program5.add(newSequenceChord(sequence5b, 0.0, "C major"));
    program5.add(newSequenceChord(sequence5b, 8.0, "Bb minor"));
    program5_binding1 = program5.add(newSequenceBinding(sequence5b, 1));
    program5.add(newSequenceBindingMeme(program5_binding1, "Pessimism"));
    //
    insert(program5);

    // A basic beat
    program35 = newProgram(35, 3, 2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, now());
    program35.add(newProgramMeme("Basic"));
    voiceDrums = program35.add(newVoice(InstrumentType.Percussive, "Drums"));
    Sequence sequence35a = program35.add(newSequence(16, "Base", 0.5, "C", 110.3));
    //
    Pattern pattern35a1 = program35.add(newPattern(sequence35a, voiceDrums, PatternType.Loop, 4, "Drop"));
    program35.add(newPatternEvent(pattern35a1, 0.0, 1.0, "CLOCK", "C2", 1.0));
    program35.add(newPatternEvent(pattern35a1, 1.0, 1.0, "SNORT", "G5", 0.8));
    program35.add(newPatternEvent(pattern35a1, 2.5, 1.0, "KICK", "C2", 0.6));
    program35.add(newPatternEvent(pattern35a1, 3.0, 1.0, "SNARL", "G5", 0.9));
    //
    Pattern pattern35a2 = program35.add(newPattern(sequence35a, voiceDrums, PatternType.Loop, 4, "Drop Alt"));
    program35.add(newPatternEvent(pattern35a2, 0.0, 1.0, "CLACK", "B5", 0.9));
    program35.add(newPatternEvent(pattern35a2, 1.0, 1.0, "SNARL", "D2", 1.0));
    program35.add(newPatternEvent(pattern35a2, 2.5, 1.0, "CLICK", "E4", 0.7));
    program35.add(newPatternEvent(pattern35a2, 3.0, 1.0, "SNAP", "c3", 0.5));
    //
    insert(program35);

    // Detail Sequence
    program6 = insert(newProgram(6, 3, 2, ProgramType.Detail, ProgramState.Published, "Beat Jam", "D#", 150, now()));
    program7 = insert(newProgram(7, 3, 2, ProgramType.Detail, ProgramState.Published, "Detail Jam", "Cb minor", 170, now()));
  }

  /**
   Library of Content B-2 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  protected void insertFixtureB2() {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = newProgram(3, 3, 2, ProgramType.Macro, ProgramState.Published, "Tangy, Chunky to Smooth", "G minor", 120.0, now());
    program3.add(newProgramMeme("Tangy"));
    //
    Sequence sequence3a = program3.add(newSequence(0, "Start Chunky", 0.4, "G minor", 115.0));
    program3_binding0 = program3.add(newSequenceBinding(sequence3a, 0));
    program3.add(newSequenceBindingMeme(program3_binding0, "Chunky"));
    //
    Sequence sequence3b = program3.add(newSequence(0, "Finish Smooth", 0.6, "C", 125.0));
    program3_binding1 = program3.add(newSequenceBinding(sequence3b, 1));
    program3.add(newSequenceBindingMeme(program3_binding1, "Smooth"));
    //
    insert(program3);

    // Main program
    program15 = newProgram(15, 3, 2, ProgramType.Main, ProgramState.Published, "Next Jam", "Db minor", 140, now());
    program15.add(newProgramMeme("Hindsight"));
    //
    Sequence sequence15a = program15.add(newSequence(16, "Intro", 0.5, "G minor", 135.0));
    program15.add(newSequenceChord(sequence15a, 0.0, "G minor"));
    program15.add(newSequenceChord(sequence15a, 8.0, "Ab minor"));
    program15_binding0 = program15.add(newSequenceBinding(sequence15a, 0));
    program15.add(newSequenceBindingMeme(program15_binding0, "Regret"));
    //
    Sequence sequence15b = program15.add(newSequence(32, "Outro", 0.5, "A major", 135.0));
    program15.add(newSequenceChord(sequence15b, 0.0, "C major"));
    program15.add(newSequenceChord(sequence15b, 8.0, "Bb major"));
    program15_binding1 = program15.add(newSequenceBinding(sequence15b, 1));
    program15.add(newSequenceBindingMeme(program15_binding1, "Pride"));
    program15.add(newSequenceBindingMeme(program15_binding1, "Shame"));
    //
    insert(program15);
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
   [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   [#150279647] Artist wants to create multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   */
  protected void insertFixtureB3() {
    // A basic beat
    program9 = newProgram(99035, 3, 2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, now());
    program9.add(newProgramMeme("Basic"));
    voiceDrums = program9.add(newVoice(InstrumentType.Percussive, "Drums"));
    Sequence sequence9a = program9.add(newSequence(16, "Base", 0.5, "C", 110.3));
    //
    Pattern pattern9a1 = program9.add(newPattern(sequence9a, voiceDrums, PatternType.Intro, 4, "Intro"));
    program9.add(newPatternEvent(pattern9a1, 0, 1, "BLEEP", "C2", 1.0));
    program9.add(newPatternEvent(pattern9a1, 1, 1, "BLEIP", "G5", 0.8));
    program9.add(newPatternEvent(pattern9a1, 2.5, 1, "BLEAP", "C2", 0.6));
    program9.add(newPatternEvent(pattern9a1, 3, 1, "BLEEEP", "G5", 0.9));
    //
    Pattern pattern9a2 = program9.add(newPattern(sequence9a, voiceDrums, PatternType.Loop, 4, "Loop A"));
    program9.add(newPatternEvent(pattern9a2, 0, 1, "CLOCK", "C2", 1.0));
    program9.add(newPatternEvent(pattern9a2, 1, 1, "SNORT", "G5", 0.8));
    program9.add(newPatternEvent(pattern9a2, 2.5, 1, "KICK", "C2", 0.6));
    program9.add(newPatternEvent(pattern9a2, 3, 1, "SNARL", "G5", 0.9));
    //
    Pattern pattern9a3 = program9.add(newPattern(sequence9a, voiceDrums, PatternType.Loop, 4, "Loop B"));
    program9.add(newPatternEvent(pattern9a3, 0, 1, "KIICK", "B5", 0.9));
    program9.add(newPatternEvent(pattern9a3, 1, 1, "SNARR", "D2", 1.0));
    program9.add(newPatternEvent(pattern9a3, 2.5, 1, "KEICK", "E4", 0.7));
    program9.add(newPatternEvent(pattern9a3, 3, 1, "SNAER", "C3", 0.5));
    //
    Pattern pattern9a4 = program9.add(newPattern(sequence9a, voiceDrums, PatternType.Outro, 4, "Outro"));
    program9.add(newPatternEvent(pattern9a4, 0, 1, "TOOT", "C2", 1.0));
    program9.add(newPatternEvent(pattern9a4, 1, 1, "TOOOT", "G5", 0.8));
    program9.add(newPatternEvent(pattern9a4, 2.5, 1, "TOOTE", "C2", 0.6));
    program9.add(newPatternEvent(pattern9a4, 3, 1, "TOUT", "G5", 0.9));
    //
    insert(program9);

    // Instrument "808"
    instrument8 = newInstrument(1, 3, 2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums", now());
    instrument8.add(newInstrumentMeme("heavy"));
    //
    audio8kick = instrument8.add(newAudio("Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.62));
    instrument8.add(newAudioEvent(audio8kick, 0, 1, "KICK", "Eb", 1.0));
    //
    audio8snare = instrument8.add(newAudio("Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    instrument8.add(newAudioEvent(audio8snare, 0, 1, "SNARE", "Ab", 0.8));
    //
    audio8bleep = instrument8.add(newAudio("Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    instrument8.add(newAudioEvent(audio8bleep, 0, 1, "BLEEP", "Ab", 0.8));
    //
    audio8toot = instrument8.add(newAudio("Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 1200, 0.62));
    instrument8.add(newAudioEvent(audio8toot, 0, 1, "TOOT", "Ab", 0.8));
    //
    insert(instrument8);
  }

  /**
   Library of Content B: Instruments (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  protected void insertFixtureB_Instruments() {
    instrument201 = newInstrument(201, 3, 2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums", now());
    instrument201.add(newInstrumentMeme("Ants"));
    instrument201.add(newInstrumentMeme("Mold"));
    //
    Audio audio401 = instrument201.add(newAudio("Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument201.add(newAudioEvent(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    instrument201.add(newAudioEvent(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    instrument201.add(newAudioEvent(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    instrument201.add(newAudioEvent(audio401, 3.0, 1.0, "SNARE", "B", 0.8));
    //
    Audio audio402 = instrument201.add(newAudio("Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument201.add(newAudioChord(audio402, 0.0, "E minor"));
    instrument201.add(newAudioChord(audio402, 4.0, "A major"));
    instrument201.add(newAudioChord(audio402, 8.0, "B minor"));
    instrument201.add(newAudioChord(audio402, 12.0, "F# major"));
    instrument201.add(newAudioChord(audio402, 16.0, "Ab7"));
    instrument201.add(newAudioChord(audio402, 20.0, "Bb7"));
    //
    insert(instrument201);
  }

  /**
   Shared fixtures for tests that require a library and some entities
   */
  protected void insertFixtureC() {
    // User "bill"
    user2 = insert(newUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif"));

    // Library "test sounds"
    library1 = insert(newLibrary(1, 1, "test sounds", now()));

    // Program: Epic Beat
    program1 = newProgram(1, 2, 1, ProgramType.Rhythm, ProgramState.Published, "epic beat", "C#", 0.286, now());
    Sequence sequence1 = program1.add(newSequence(16, "epic beat part 1", 0.342, "C#", 0.286));
    SequenceBinding binding1_0 = program1.add(newSequenceBinding(sequence1, 0));
    Voice voice1x = program1.add(newVoice(InstrumentType.Percussive, "This is a percussive newVoice"));
    Pattern pattern1a = program1.add(newPattern(sequence1, voice1x, PatternType.Loop, 16, "Ants"));
    program1.add(newPatternEvent(pattern1a, 0.0, 1.0, "KICK", "C", 1.0));
    insert(program1);

    // Library has Instrument with Audio
    instrument9 = newInstrument(9, 2, 1, InstrumentType.Percussive, InstrumentState.Published, "jams", now());
    instrument9.add(newAudio("Kick", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    insert(instrument9);

    // Chain "Test Print #1" has one segment
    chain3 = insert(newChain(3, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null, now()));
    // segment-17 at offset-0 of chain-3
    segment17 = segmentFactory.newSegment(BigInteger.valueOf(17))
      .setChainId(BigInteger.valueOf(3))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D Major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment17.add(new Choice()
      .setProgramId(BigInteger.valueOf(1))
      .setSequenceBinding(binding1_0)
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));
    insert(segment17);
  }

  /**
   Insert a generated library

   @param N magnitude of library to generate
   */
  protected void insertGeneratedFixture(int N) {
    account1 = insert(newAccount(1L, "Generated"));
    user1 = insert(newUser(1L, "generated", "generated@email.com", "http://pictures.com/generated.gif"));
    insert(newUserRole(1L, UserRoleType.Admin));
    library1 = insert(newLibrary(1, 1, "generated", now()));

    // Create a N-magnitude set of unique major memes
    String[] majorMemeNames = listOfUniqueRandom(N, LoremIpsum.COLORS);
    String[] minorMemeNames = listOfUniqueRandom((long) StrictMath.ceil(N / 2), LoremIpsum.VARIANTS);
    String[] percussiveInflections = listOfUniqueRandom(N, LoremIpsum.PERCUSSIVE_INFLECTIONS);

    // Generate a Percussive Instrument for each meme
    for (int i = 0; i < N; i++) {
      long instrumentId = getNextUniqueId();
      String majorMemeName = majorMemeNames[i];
      String minorMemeName = random(minorMemeNames);
      //
      Instrument instrument = newInstrument(instrumentId, 1, 1, InstrumentType.Percussive, InstrumentState.Published, String.format("%s Drums", majorMemeName), now());
      instrument.add(newInstrumentMeme(majorMemeName));
      instrument.add(newInstrumentMeme(minorMemeName));
      // audios of instrument
      for (int k = 0; k < N; k++) {
        Audio audio = instrument.add(newAudio(Text.toProper(percussiveInflections[k]), String.format("%s.wav", Text.toLowerSlug(percussiveInflections[k])), random(0, 0.05), random(0.25, 2), random(80, 120), random(100, 4000), 0.62));
        instrument.add(newAudioEvent(audio, 0.0, 1.0, percussiveInflections[k], "X", random(0.8, 1)));
      }
      insert(instrument);
      //
      log.info("Generated Percussive-type Instrument id={}, minorMeme={}, majorMeme={}", instrumentId, minorMemeName, majorMemeName);
    }

    // Generate N*2 total Macro-type Sequences, each transitioning from one Meme to another
    for (int i = 0; i < N << 1; i++) {
      long programId = getNextUniqueId();
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
      Program program = newProgram(programId, 1, 1, ProgramType.Macro, ProgramState.Published, String.format("%s, from %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, now());
      program.add(newProgramMeme(minorMemeName));
      // from offset 0
      Sequence sequence0 = program.add(newSequence(0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom));
      SequenceBinding binding0 = program.add(newSequenceBinding(sequence0, 0));
      program.add(newSequenceBindingMeme(binding0, majorMemeFromName));
      // to offset 1
      double densityTo = random(0.3, 0.9);
      double tempoTo = random(803, 120);
      Sequence sequence1 = program.add(newSequence(0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo));
      SequenceBinding binding1 = program.add(newSequenceBinding(sequence1, 1));
      program.add(newSequenceBindingMeme(binding1, majorMemeToName));
      insert(program);
      //
      log.info("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", programId, minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    Sequence[] sequences = new Sequence[N];
    for (int i = 0; i < N << 2; i++) {
      long programId = getNextUniqueId();
      String majorMemeName = random(majorMemeNames);
      String[] sequenceNames = listOfUniqueRandom(N, LoremIpsum.ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, LoremIpsum.MUSICAL_KEYS);
      Double[] subDensities = listOfRandomValues(N, 0.3, 0.8);
      double tempo = random(80, 120);
      //
      Program program = newProgram(programId, 1, 1, ProgramType.Main, ProgramState.Published, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, now());
      program.add(newProgramMeme(majorMemeName));
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = program.add(newSequence(total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP], tempo));
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            program.add(newSequenceChord(sequences[iP], StrictMath.floor(total * iPC / N << 2), random(LoremIpsum.MUSICAL_CHORDS)));
          }
        }
      }
      // sequence sequence binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        SequenceBinding binding = program.add(newSequenceBinding(sequences[num], offset));
        program.add(newSequenceBindingMeme(binding, random(minorMemeNames)));
      }
      insert(program);
      log.info("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", programId, majorMemeName, N, N << 2);
    }

    // Generate N total Rhythm-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    Voice[] voices = new Voice[N];
    for (int i = 0; i < N; i++) {
      long programId = getNextUniqueId();
      String majorMemeName = majorMemeNames[i];
      double tempo = random(80, 120);
      String key = random(LoremIpsum.MUSICAL_KEYS);
      double density = random(0.4, 0.9);
      //
      Program program = newProgram(programId, 1, 1, ProgramType.Rhythm, ProgramState.Published, String.format("%s Beat", majorMemeName), key, tempo, now());
      program.add(newProgramMeme(majorMemeName));
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = program.add(newVoice(InstrumentType.Percussive, String.format("%s %s", majorMemeName, percussiveInflections[iV])));
      }
      Sequence sequenceBase = program.add(newSequence(random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key, tempo));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        PatternType type = 0 == iP ? PatternType.Loop : randomRhythmPatternType();
        Pattern pattern = program.add(newPattern(sequenceBase, voices[num], type, total, String.format("%s %s %s", majorMemeName, type.toString(), random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            program.add(newPatternEvent(pattern, StrictMath.floor(total * iPE / N << 2), random(0.25, 1.0), percussiveInflections[num], "X", random(0.4, 0.9)));
          }
        }
      }
      insert(program);
      log.info("Generated Rhythm-type Program id={}, majorMeme={} with {} patterns", programId, majorMemeName, N);
    }
  }

}
