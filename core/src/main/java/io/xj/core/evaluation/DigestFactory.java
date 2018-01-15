// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation;

import io.xj.core.evaluation.digest_chords.DigestChords;
import io.xj.core.evaluation.digest_memes.DigestMemes;

import com.google.inject.assistedinject.Assisted;

/**
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
   Digest memes of any evaluation of entities.

   @param evaluation to digest
   @return meme digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestMemes memesOf(
    @Assisted("evaluation") Evaluation evaluation
  ) throws Exception;

}
