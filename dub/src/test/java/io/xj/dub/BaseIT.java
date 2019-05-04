//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.dub;

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
    // Account "elephants"
    IntegrationTestEntity.insertAccount(1, "elephants");

    // Jen has "user" and "admin" roles, belongs to account "elephants", has "google" auth
    IntegrationTestEntity.insertUser(2, "jen", "jen@email.com", "http://pictures.com/jen.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Fred has a "user" role and belongs to account "elephants"
    IntegrationTestEntity.insertUser(3, "fred", "fred@email.com", "http://pictures.com/fred.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Classic, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Classic, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(4, "Classic");
    IntegrationTestEntity.insertPattern(3, 4, PatternType.Macro, PatternState.Published, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePattern(340, 4, 3, 0);
    IntegrationTestEntity.insertSequencePatternMeme(340, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 0, "C");
    IntegrationTestEntity.insertPattern(4, 4, PatternType.Macro, PatternState.Published, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePattern(441, 4, 4, 1);
    IntegrationTestEntity.insertSequencePatternMeme(441, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertSequenceMeme(5, "Outlook");
    IntegrationTestEntity.insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(1550, 5, 15, 0);
    IntegrationTestEntity.insertSequencePatternMeme(1550, "Cloudy");
    IntegrationTestEntity.insertPatternChord(15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(15, 8, "G minor");
    IntegrationTestEntity.insertPattern(16, 5, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePattern(1651, 5, 16, 1);
    IntegrationTestEntity.insertSequencePatternMeme(1651, "Rosy");
    IntegrationTestEntity.insertPatternChord(16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");
    IntegrationTestEntity.insertSequenceMeme(35, "Basic");
    // basic beat first pattern
    IntegrationTestEntity.insertPattern(316, 35, PatternType.Loop, PatternState.Published, 16, "Continue", 0.5, "C", 125.0);
    // setup voice second pattern
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Loop, PatternState.Published, 4, "Drop", 0.5, "C", 125.0);

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPatternEvent(315, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(315, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(315, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(315, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // detail sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);
  }

  /**
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-pattern binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   */
  protected static void insertLibraryB() {
    // Account "crows"
    IntegrationTestEntity.insertAccount(1, "crows");

    // John has "user" and "admin" roles, belongs to account "crows", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "crows"
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

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(5, "Outlook");
    // # pattern offset 0
    IntegrationTestEntity.insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePattern(1550, 5, 15, 0);
    IntegrationTestEntity.insertSequencePatternMeme(1550, "Optimism");
    IntegrationTestEntity.insertPatternChord(15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(15, 8, "Ab minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPattern(16, 5, PatternType.Main, PatternState.Published, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(1651, 5, 16, 1);
    IntegrationTestEntity.insertSequencePatternMeme(1651, "Pessimism");
    IntegrationTestEntity.insertPatternChord(16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(16, 8, "Bb minor");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(15, "Hindsight");
    IntegrationTestEntity.insertPattern(415, 15, PatternType.Main, PatternState.Published, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePattern(415150, 15, 415, 0);
    IntegrationTestEntity.insertSequencePatternMeme(415150, "Regret");
    IntegrationTestEntity.insertPatternChord(415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(415, 8, "Ab minor");
    IntegrationTestEntity.insertPattern(416, 15, PatternType.Main, PatternState.Published, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertSequencePattern(416151, 15, 416, 1);
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Pride");
    IntegrationTestEntity.insertSequencePatternMeme(416151, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Loop, PatternState.Published, 4, "Drop", 0.5, "C", 125.0);
    // setup voice pattern events
    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPatternEvent(315, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(315, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(315, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(315, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);
    // basic beat second pattern
    IntegrationTestEntity.insertPattern(316, 35, PatternType.Loop, PatternState.Published, 4, "Continue", 0.5, "C", 125.0);
  }
}
