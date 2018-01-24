// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.digest;

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
public interface DigestCacheProvider {

  /**
   Digest chord progressions of any evaluation of entities.
   CACHED result based on hash digest of evaluation.

   @param evaluation to digest
   @return chord progression digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChordProgression chordProgression(Evaluation evaluation) throws Exception;

  /**
   Digest chord Markov chords of any evaluation of entities.
   CACHED result based on hash digest of evaluation.

   @param evaluation to digest
   @return chord Markov digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChordMarkov chordMarkov(Evaluation evaluation) throws Exception;

  /**
   Digest hash of any evaluation of entities.
   NOT CACHED.

   @param evaluation to digest
   @return hash digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestHash hash(Evaluation evaluation) throws Exception;

  /**
   Digest memes of any evaluation of entities.
   CACHED result based on hash digest of evaluation.

   @param evaluation to digest
   @return meme digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestMeme meme(Evaluation evaluation) throws Exception;
}
