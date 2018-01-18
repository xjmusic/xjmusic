// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation.digest;

import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.chords.DigestChords;
import io.xj.core.evaluation.digest.hash.DigestHash;
import io.xj.core.evaluation.digest.memes.DigestMemes;

import com.google.inject.assistedinject.Assisted;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 DigestChords chords = digestFactory.chordsOf(evaluation);
 DigestMemes memes = digestFactory.memesOf(evaluation);
 */
public interface DigestFactory {

  /**
   Digest chords of any evaluation of entities.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChords chordsOf(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

  /**
   Digest hash of any evaluation of entities.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestHash hashOf(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

  /**
   Digest memes of any evaluation of entities.

   @param evaluation to digest
   @return meme digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestMemes memesOf(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

}
