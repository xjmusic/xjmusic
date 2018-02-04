// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.cache;

import io.xj.craft.digest.Digest;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import io.xj.craft.digest.hash.DigestHash;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.pattern_style.DigestPatternStyle;
import io.xj.craft.ingest.Ingest;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 DigestChordProgression chords = digestFactory.chordProgression(ingest);
 DigestMeme memes = digestFactory.meme(ingest);
 */
public interface DigestCacheProvider {

  /**
   Digest chord progressions of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return chord progression digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestChordProgression chordProgression(Ingest ingest) throws Exception;

  /**
   Digest chord Markov chords of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return chord Markov digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestChordMarkov chordMarkov(Ingest ingest) throws Exception;

  /**
   Digest hash of any ingest of entities.
   NOT CACHED.

   @param ingest to digest
   @return hash digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestHash hash(Ingest ingest) throws Exception;

  /**
   Digest memes of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return meme digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestMeme meme(Ingest ingest) throws Exception;

  /**
   Digest pattern style of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return pattern style digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestPatternStyle patternStyle(Ingest ingest) throws Exception;

}
