// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.digest;

import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.chords.DigestChords;
import io.xj.core.evaluation.digest.hash.DigestHash;
import io.xj.core.evaluation.digest.memes.DigestMemes;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 DigestChords chords = digestFactory.chordsOf(evaluation);
 DigestMemes memes = digestFactory.memesOf(evaluation);
 */
public interface DigestCacheProvider {

  /**
   Digest chords of any evaluation of entities.
   CACHED result based on hash digest of evaluation.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestChords chordsOf(Evaluation evaluation) throws Exception;

  /**
   Digest hash of any evaluation of entities.
   NOT CACHED.

   @param evaluation to digest
   @return chord digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestHash hashOf(Evaluation evaluation) throws Exception;

  /**
   Digest memes of any evaluation of entities.
   CACHED result based on hash digest of evaluation.

   @param evaluation to digest
   @return meme digest of evaluation
   @throws Exception on failure to of target entities
   */
  DigestMemes memesOf(Evaluation evaluation) throws Exception;

}
