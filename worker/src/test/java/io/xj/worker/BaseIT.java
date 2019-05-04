//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.worker;

import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;

public class BaseIT {

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-pattern binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   */
  protected static void insertLibraryA() {
    // Account "pilots"
    IntegrationTestEntity.insertAccount(1, "pilots");

    // Ted has "user" and "admin" roles, belongs to account "pilots", has "google" auth
    IntegrationTestEntity.insertUser(2, "ted", "ted@email.com", "http://pictures.com/ted.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Sally has a "user" role and belongs to account "pilots"
    IntegrationTestEntity.insertUser(3, "sally", "sally@email.com", "http://pictures.com/sally.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // Percussive Instrument
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

    // "Heavy, Deep to Metal" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Heavy, Deep to Metal", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(4, "Heavy");
    // " pattern offset 0
    IntegrationTestEntity.insertPattern(3, 4, PatternType.Macro, PatternState.Published, 0, "Start Deep", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePattern(340, 4, 3, 0);
    IntegrationTestEntity.insertSequencePatternMeme(340, "Deep");
    IntegrationTestEntity.insertPatternChord(3, 0, "C");
    // " pattern offset 1
    IntegrationTestEntity.insertPattern(4, 4, PatternType.Macro, PatternState.Published, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePattern(441, 4, 4, 1);
    IntegrationTestEntity.insertSequencePatternMeme(441, "Metal");
    IntegrationTestEntity.insertSequencePatternMeme(441, "Deep");
    IntegrationTestEntity.insertPatternChord(4, 0, "Bb minor");
    // " pattern offset 2
    IntegrationTestEntity.insertPattern(5, 4, PatternType.Macro, PatternState.Published, 0, "Finish Metal", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertSequencePattern(542, 4, 5, 2);
    IntegrationTestEntity.insertSequencePatternMeme(542, "Metal");
    IntegrationTestEntity.insertPatternChord(5, 0, "Ab minor");

    // "Tech, Steampunk to Modern" macro-sequence in house library
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "Tech, Steampunk to Modern", 0.5, "G minor", 120);
    IntegrationTestEntity.insertSequenceMeme(3, "Tech");
    // # pattern offset 0
    IntegrationTestEntity.insertPattern(1, 3, PatternType.Macro, PatternState.Published, 0, "Start Steampunk", 0.4, "G minor", 115);
    IntegrationTestEntity.insertSequencePattern(130, 3, 1, 0);
    IntegrationTestEntity.insertSequencePatternMeme(130, "Steampunk");
    IntegrationTestEntity.insertPatternChord(1, 0, "G minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPattern(2, 3, PatternType.Macro, PatternState.Published, 0, "Finish Modern", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePattern(231, 3, 2, 1);
    IntegrationTestEntity.insertSequencePatternMeme(231, "Modern");
    IntegrationTestEntity.insertPatternChord(2, 0, "C");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(5, "Attitude");
    // # pattern offset 0
    IntegrationTestEntity.insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePattern(1550, 5, 15, 0);
    IntegrationTestEntity.insertSequencePatternMeme(1550, "Gritty");
    IntegrationTestEntity.insertPatternChord(15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(15, 8, "Ab minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPattern(16, 5, PatternType.Main, PatternState.Published, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(1651, 5, 16, 1);
    IntegrationTestEntity.insertSequencePatternMeme(1651, "Gentle");
    IntegrationTestEntity.insertPatternChord(16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(16, 8, "Bb minor");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(15, "Temptation");
    IntegrationTestEntity.insertPattern(415, 15, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(415150, 15, 415, 0);
    IntegrationTestEntity.insertSequencePatternMeme(415150, "Food");
    IntegrationTestEntity.insertPatternChord(415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(415, 8, "Ab minor");
    IntegrationTestEntity.insertPattern(416, 15, PatternType.Main, PatternState.Published, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertSequencePattern(416151, 15, 416, 1);
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Drink");
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(35, "Basic");
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Loop, PatternState.Published, 16, "Drop", 0.5, "C", 125.0);

    // For cloning-related test: Sequence "808" and "2020"
    IntegrationTestEntity.insertSequence(1001, 2, 2, SequenceType.Rhythm, SequenceState.Published, "808 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertSequenceMeme(1001, "heavy");
    IntegrationTestEntity.insertVoice(1001, 1001, InstrumentType.Percussive, "Kick Drum");
    IntegrationTestEntity.insertVoice(1002, 1001, InstrumentType.Percussive, "Snare Drum");
    IntegrationTestEntity.insertSequence(10012, 2, 2, SequenceType.Rhythm, SequenceState.Published, "2020 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertVoice(1003, 10012, InstrumentType.Percussive, "Kack Dram");
    IntegrationTestEntity.insertVoice(1004, 10012, InstrumentType.Percussive, "Snarr Dram");

    // For cloning-related test: Pattern "Verse"
    IntegrationTestEntity.insertPattern(1001, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 1", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternChord(1001, 0, "Db7");
    IntegrationTestEntity.insertPatternEvent(1001, 1001, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(1001, 1002, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // For cloning-related test: Pattern "Verse"
    IntegrationTestEntity.insertPattern(1002, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 2", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternChord(1002, 0, "Gm9");
    IntegrationTestEntity.insertPatternEvent(1002, 1001, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(1002, 1002, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Newly cloned patterns -- awaiting PatternClone job to run, and create their child entities
    IntegrationTestEntity.insertPattern(1003, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 34", 0.5, "G", 120);
    IntegrationTestEntity.insertPattern(1004, 10012, PatternType.Loop, PatternState.Published, 16, "Verse 79", 0.5, "G", 120);
  }

}
