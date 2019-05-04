//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft;

import com.google.common.collect.Maps;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.util.Text;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseIT {

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  protected static void insertLibraryA() {
    IntegrationTestEntity.insertAccount(1, "testing");
    IntegrationTestEntity.insertUser(101, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(101, UserRoleType.Admin);
    Timestamp at = Timestamp.valueOf("2014-08-12 12:17:02.527142");
    //
    IntegrationTestEntity.insertLibrary(10000001, 1, "leaves", at);
    IntegrationTestEntity.insertInstrument(201, 10000001, 101, "808 Drums", InstrumentType.Percussive, 0.9, at);
    IntegrationTestEntity.insertInstrument(202, 10000001, 101, "909 Drums", InstrumentType.Percussive, 0.8, at);
    IntegrationTestEntity.insertInstrumentMeme(201, "Ants", at);
    IntegrationTestEntity.insertInstrumentMeme(201, "Mold", at);
    IntegrationTestEntity.insertInstrumentMeme(202, "Peel", at);
    IntegrationTestEntity.insertAudio(401, 201, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudio(402, 201, "Published", "Chords Cm to D", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudioChord(402, 0, "E minor", at);
    IntegrationTestEntity.insertAudioChord(402, 4, "A major", at);
    IntegrationTestEntity.insertAudioChord(402, 8, "B minor", at);
    IntegrationTestEntity.insertAudioChord(402, 12, "F# major", at);
    IntegrationTestEntity.insertAudioChord(402, 16, "Ab7", at);
    IntegrationTestEntity.insertAudioChord(402, 20, "Bb7", at);
    IntegrationTestEntity.insertAudioEvent(401, 2.5, 1, "KICK", "Eb", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(401, 3, 1, "SNARE", "Ab", 0.1, 0.8, at);
    IntegrationTestEntity.insertAudioEvent(401, 0, 1, "KICK", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(401, 1, 1, "SNARE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertSequence(701, 101, 10000001, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertSequence(702, 101, 10000001, SequenceType.Detail, SequenceState.Published, "coconuts", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertSequence(703, 101, 10000001, SequenceType.Main, SequenceState.Published, "bananas", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertSequenceMeme(701, "Ants", at);
    IntegrationTestEntity.insertSequenceMeme(702, "Ants", at);
    IntegrationTestEntity.insertSequenceMeme(703, "Peel", at);
    IntegrationTestEntity.insertPattern(901, 701, PatternType.Main, PatternState.Published, 16, "growth", 0.342, "C#", 120.4, at, 4, 4, 0);
    IntegrationTestEntity.insertPattern(902, 701, PatternType.Main, PatternState.Published, 16, "decay", 0.25, "F#", 110.3, at, 4, 4, 0);
    IntegrationTestEntity.insertPatternChord(902, 0, "G minor", at);
    IntegrationTestEntity.insertPatternChord(902, 4, "C major", at);
    IntegrationTestEntity.insertPatternChord(902, 8, "F7", at);
    IntegrationTestEntity.insertPatternChord(902, 12, "G7", at);
    IntegrationTestEntity.insertPatternChord(902, 16, "F minor", at);
    IntegrationTestEntity.insertPatternChord(902, 20, "Bb major", at);
    IntegrationTestEntity.insertVoice(1201, 701, InstrumentType.Percussive, "Drums", at);
    IntegrationTestEntity.insertVoice(1202, 702, InstrumentType.Harmonic, "Bass", at); // expect this to have no dependent entities, so it can be deleted for a test
    IntegrationTestEntity.insertPatternEvent(901, 1201, 0, 1, "BOOM", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPatternEvent(901, 1201, 1, 1, "SMACK", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPatternEvent(901, 1201, 2.5, 1, "BOOM", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPatternEvent(901, 1201, 3, 1, "SMACK", "G", 0.1, 0.9, at);
    IntegrationTestEntity.insertSequencePattern(7900, 701, 901, 0, at);
    IntegrationTestEntity.insertSequencePattern(7901, 701, 901, 1, at);
    IntegrationTestEntity.insertSequencePattern(7902, 701, 901, 2, at);
    IntegrationTestEntity.insertSequencePattern(7903, 701, 902, 3, at);
    IntegrationTestEntity.insertSequencePattern(7904, 701, 902, 4, at);
    IntegrationTestEntity.insertSequencePattern(7905, 701, 902, 5, at);
    IntegrationTestEntity.insertSequencePatternMeme(7900, "Gravel", at);
    IntegrationTestEntity.insertSequencePatternMeme(7901, "Gravel", at);
    IntegrationTestEntity.insertSequencePatternMeme(7902, "Gravel", at);
    IntegrationTestEntity.insertSequencePatternMeme(7903, "Rocks", at);
    IntegrationTestEntity.insertSequencePatternMeme(7901, "Fuzz", at);
    IntegrationTestEntity.insertSequencePatternMeme(7902, "Fuzz", at);
    IntegrationTestEntity.insertSequencePatternMeme(7903, "Fuzz", at);
    IntegrationTestEntity.insertSequencePatternMeme(7904, "Noise", at);
    //
    // stuff that should not get used because it's in a different library
    IntegrationTestEntity.insertLibrary(10000002, 1, "Garbage Library", at);
    IntegrationTestEntity.insertInstrument(251, 10000002, 101, "Garbage Instrument A", InstrumentType.Percussive, 0.9, at);
    IntegrationTestEntity.insertInstrument(252, 10000002, 101, "Garbage Instrument B", InstrumentType.Percussive, 0.8, at);
    IntegrationTestEntity.insertInstrumentMeme(251, "Garbage Instrument Meme A", at);
    IntegrationTestEntity.insertInstrumentMeme(251, "Garbage Instrument Meme B", at);
    IntegrationTestEntity.insertInstrumentMeme(252, "Garbage Instrument Meme C", at);
    IntegrationTestEntity.insertAudio(451, 251, "Published", "Garbage Audio A", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudio(452, 251, "Published", "Garbage audio B", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudioChord(452, 0, "E garbage", at);
    IntegrationTestEntity.insertAudioChord(452, 4, "A major garbage", at);
    IntegrationTestEntity.insertAudioChord(452, 8, "B minor garbage", at);
    IntegrationTestEntity.insertAudioChord(452, 12, "F# major garbage", at);
    IntegrationTestEntity.insertAudioChord(452, 16, "Ab7 garbage", at);
    IntegrationTestEntity.insertAudioChord(452, 20, "Bb7 garbage", at);
    IntegrationTestEntity.insertAudioEvent(451, 2.5, 1, "GARBAGE", "Eb", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(451, 3, 1, "GARBAGE", "Ab", 0.1, 0.8, at);
    IntegrationTestEntity.insertAudioEvent(451, 0, 1, "GARBAGE", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(451, 1, 1, "GARBAGE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertSequence(751, 101, 10000002, SequenceType.Main, SequenceState.Published, "Garbage Sequence A", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertSequence(752, 101, 10000002, SequenceType.Detail, SequenceState.Published, "Garbage Sequence B", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertSequence(753, 101, 10000002, SequenceType.Main, SequenceState.Published, "Garbage Sequence C", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertSequenceMeme(751, "Garbage Sequence Meme A", at);
    IntegrationTestEntity.insertSequenceMeme(751, "Garbage Sequence Meme B", at);
    IntegrationTestEntity.insertSequenceMeme(753, "Garbage Sequence Meme C", at);
    IntegrationTestEntity.insertPattern(951, 751, PatternType.Main, PatternState.Published, 16, "Garbage Pattern A", 0.342, "C#", 120.4, at, 4, 4, 0);
    IntegrationTestEntity.insertPattern(952, 751, PatternType.Main, PatternState.Published, 16, "Garbage Pattern A", 0.25, "F#", 110.3, at, 4, 4, 0);
    IntegrationTestEntity.insertSequencePattern(7500, 751, 951, 0, at);
    IntegrationTestEntity.insertSequencePattern(7501, 751, 952, 1, at);
    IntegrationTestEntity.insertPatternChord(952, 0, "G minor garbage", at);
    IntegrationTestEntity.insertPatternChord(952, 4, "C major garbage", at);
    IntegrationTestEntity.insertPatternChord(952, 8, "F7 garbage", at);
    IntegrationTestEntity.insertPatternChord(952, 12, "G7 garbage", at);
    IntegrationTestEntity.insertPatternChord(952, 16, "F minor garbage", at);
    IntegrationTestEntity.insertPatternChord(952, 20, "Bb major garbage", at);
    IntegrationTestEntity.insertSequencePatternMeme(7500, "Garbage Pattern Meme A", at);
    IntegrationTestEntity.insertSequencePatternMeme(7500, "Garbage Pattern Meme B", at);
    IntegrationTestEntity.insertSequencePatternMeme(7501, "Garbage Pattern Meme C", at);
    IntegrationTestEntity.insertVoice(1251, 751, InstrumentType.Percussive, "Garbage Voice A", at);
    IntegrationTestEntity.insertVoice(1252, 752, InstrumentType.Harmonic, "Garbage Voice B", at);
    IntegrationTestEntity.insertPatternEvent(951, 1251, 0, 1, "GARBAGE", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPatternEvent(951, 1251, 1, 1, "GARBAGE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPatternEvent(951, 1251, 2.5, 1, "GARBAGE", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPatternEvent(951, 1251, 3, 1, "GARBAGE", "G", 0.1, 0.9, at);
  }

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-pattern binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   */
  protected static void insertLibraryB1() {
    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(4, "Tropical");
    // " pattern offset 0
    IntegrationTestEntity.insertPattern(3, 4, PatternType.Macro, PatternState.Published, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePattern(340, 4, 3, 0);
    IntegrationTestEntity.insertSequencePatternMeme(340, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 0, "C");
    // " pattern offset 1
    IntegrationTestEntity.insertPattern(4, 4, PatternType.Macro, PatternState.Published, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePattern(441, 4, 4, 1);
    IntegrationTestEntity.insertSequencePatternMeme(441, "Cozy");
    IntegrationTestEntity.insertSequencePatternMeme(441, "Wild");
    IntegrationTestEntity.insertPatternChord(4, 0, "Bb minor");
    // " pattern offset 2
    IntegrationTestEntity.insertPattern(5, 4, PatternType.Macro, PatternState.Published, 0, "Finish Cozy", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertSequencePattern(542, 4, 5, 2);
    IntegrationTestEntity.insertSequencePatternMeme(542, "Cozy");
    IntegrationTestEntity.insertPatternChord(5, 0, "Ab minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(5, "Outlook");
    // # pattern offset 0
    IntegrationTestEntity.insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePattern(1550, 5, 15, 0);
    IntegrationTestEntity.insertSequencePatternMeme(1550, "Optimism");
    IntegrationTestEntity.insertPatternChord(15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(15, 8, "Ab minor");
    IntegrationTestEntity.insertPatternChord(15, 75, "G-9"); // [#154090557] this Chord should be ignored, because it's past the end of the main-pattern total
    // # pattern offset 1
    IntegrationTestEntity.insertPattern(16, 5, PatternType.Main, PatternState.Published, 32, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(1651, 5, 16, 1);
    IntegrationTestEntity.insertSequencePatternMeme(1651, "Pessimism");
    IntegrationTestEntity.insertPatternChord(16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(16, 8, "Bb minor");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    // Voice "Drums" are onomatopoeic to "KICK" and "SNARE" 2x each
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Intro, PatternState.Published, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(315, 1, 0, 1, "CLOCK", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(315, 1, 1, 1, "SNORT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(315, 1, 2.5, 1, "KICK", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(315, 1, 3, 1, "SNARL", "G5", 0.1, 0.9);

    // this is an alternate pattern at the same offset
    IntegrationTestEntity.insertPattern(317, 35, PatternType.Loop, PatternState.Published, 4, "Drop Alt", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(317, 1, 0, 1, "CLACK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPatternEvent(317, 1, 1, 1, "SNARL", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPatternEvent(317, 1, 2.5, 1, "CLICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPatternEvent(317, 1, 3, 1, "SNAP", "C3", 0.5, 0.5);

    // Detail Sequence
    IntegrationTestEntity.insertSequence(6, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);
  }

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  protected static void insertLibraryB2() {
    // "Tangy, Chunky to Smooth" macro-sequence in house library
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertSequenceMeme(3, "Tangy");
    // # pattern offset 0
    IntegrationTestEntity.insertPattern(1, 3, PatternType.Macro, PatternState.Published, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertSequencePattern(130, 3, 1, 0);
    IntegrationTestEntity.insertSequencePatternMeme(130, "Chunky");
    IntegrationTestEntity.insertPatternChord(1, 0, "G minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPattern(2, 3, PatternType.Macro, PatternState.Published, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePattern(231, 3, 2, 1);
    IntegrationTestEntity.insertSequencePatternMeme(231, "Smooth");
    IntegrationTestEntity.insertPatternChord(2, 0, "C");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(15, "Hindsight");
    IntegrationTestEntity.insertPattern(415, 15, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(415150, 15, 415, 0);
    IntegrationTestEntity.insertSequencePatternMeme(415150, "Regret");
    IntegrationTestEntity.insertPatternChord(415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(415, 8, "Ab minor");
    IntegrationTestEntity.insertPattern(416, 15, PatternType.Main, PatternState.Published, 32, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertSequencePattern(416151, 15, 416, 1);
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Pride");
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(416, 8, "Bb major");
    IntegrationTestEntity.insertPattern(440, 15, PatternType.Main, PatternState.Published, 32, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPattern(442, 15, PatternType.Main, PatternState.Published, 32, "Outro", 0.5, "A major", 135.0);
  }

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-pattern binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   <p>
   [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   [#150279647] Artist wants to create multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   */
  protected static void insertLibraryB3() {
    // A basic beat, first pattern has voice and events
    IntegrationTestEntity.insertSequence(99035, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(99035, "Basic");
    IntegrationTestEntity.insertVoice(2315, 99035, InstrumentType.Percussive, "drums");

    // Intro Pattern
    IntegrationTestEntity.insertPattern(2345, 99035, PatternType.Intro, PatternState.Published, 4, "Intro", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(2345, 2315, 0, 1, "BLEEP", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2345, 2315, 1, 1, "BLEIP", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(2345, 2315, 2.5, 1, "BLEAP", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(2345, 2315, 3, 1, "BLEEEP", "G5", 0.1, 0.9);
    // Loop Pattern A
    IntegrationTestEntity.insertPattern(2346, 99035, PatternType.Loop, PatternState.Published, 4, "Loop A", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(2346, 2315, 0, 1, "CLOCK", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2346, 2315, 1, 1, "SNORT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(2346, 2315, 2.5, 1, "KICK", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(2346, 2315, 3, 1, "SNARL", "G5", 0.1, 0.9);
    // Loop Pattern A
    IntegrationTestEntity.insertPattern(2347, 99035, PatternType.Loop, PatternState.Published, 4, "Loop B", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(2347, 2315, 0, 1, "KIICK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPatternEvent(2347, 2315, 1, 1, "SNARR", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPatternEvent(2347, 2315, 2.5, 1, "KEICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPatternEvent(2347, 2315, 3, 1, "SNAER", "C3", 0.5, 0.5);
    // Outro Pattern
    IntegrationTestEntity.insertPattern(2348, 99035, PatternType.Outro, PatternState.Published, 4, "Outro", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternEvent(2348, 2315, 0, 1, "TOOT", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2348, 2315, 1, 1, "TOOOT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(2348, 2315, 2.5, 1, "TOOTE", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(2348, 2315, 3, 1, "TOUT", "G5", 0.1, 0.9);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 0, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 0, 1, "SNARE", "Ab", 0.1, 0.8);

    // Audio "Bleep"
    IntegrationTestEntity.insertAudio(3, 1, "Published", "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(3, 0, 1, "BLEEP", "Ab", 0.8, 0.8);

    // Audio "Toot"
    IntegrationTestEntity.insertAudio(4, 1, "Published", "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(4, 0, 1, "TOOT", "Ab", 0.1, 0.8);
  }

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  protected static void insertLibraryB_Instruments() {
    IntegrationTestEntity.insertInstrument(201, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(201, "Ants");
    IntegrationTestEntity.insertInstrumentMeme(201, "Mold");
    IntegrationTestEntity.insertAudio(401, 201, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudio(402, 201, "Published", "Chords Cm to D", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioChord(402, 0, "E minor");
    IntegrationTestEntity.insertAudioChord(402, 4, "A major");
    IntegrationTestEntity.insertAudioChord(402, 8, "B minor");
    IntegrationTestEntity.insertAudioChord(402, 12, "F# major");
    IntegrationTestEntity.insertAudioChord(402, 16, "Ab7");
    IntegrationTestEntity.insertAudioChord(402, 20, "Bb7");
    IntegrationTestEntity.insertAudioEvent(401, 2.5, 1, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(401, 3, 1, "SNARE", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioEvent(401, 0, 1, "KICK", "C", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(401, 1, 1, "SNARE", "G", 0.1, 0.8);
  }

}
