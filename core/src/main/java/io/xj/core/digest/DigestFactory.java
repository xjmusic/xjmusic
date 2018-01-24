// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.digest;

import com.google.inject.assistedinject.Assisted;

import io.xj.core.digest.chord_markov.DigestChordMarkov;
import io.xj.core.digest.chord_sequence.DigestChordProgression;
import io.xj.core.digest.hash.DigestHash;
import io.xj.core.digest.meme.DigestMeme;
import io.xj.core.evaluation.Evaluation;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 DigestChordProgression chords = digestFactory.chordProgression(evaluation);
 DigestMeme memes = digestFactory.meme(evaluation);
 */
public interface DigestFactory {

  /**
   Digest chords of any evaluation of entities.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChordProgression chordProgression(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

  /**
   Digest chords of any evaluation of entities.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChordMarkov chordMarkov(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

  /**
   Digest hash of any evaluation of entities.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure
   */
  DigestHash hashOf(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

  /**
   Digest memes of any evaluation of entities.

   @param evaluation to digest
   @return meme digest of evaluation
   @throws Exception on failure
   */
  DigestMeme meme(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

}
