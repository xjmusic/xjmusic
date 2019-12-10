// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import io.xj.core.ingest.Ingest;
import io.xj.craft.digest.DigestChordMarkov;
import io.xj.craft.digest.DigestChordProgression;
import io.xj.craft.digest.DigestHash;
import io.xj.craft.digest.DigestMeme;
import io.xj.craft.digest.DigestProgramStyle;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 DigestChordProgression entities = digestFactory.chordProgression(ingest);
 DigestMeme memes = digestFactory.meme(ingest);
 */
public interface DigestCacheProvider {

  /**
   Digest chord progressions of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return chord progression digest of ingest
   */
  DigestChordProgression chordProgression(Ingest ingest);

  /**
   Digest chord Markov entities of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return chord Markov digest of ingest
   */
  DigestChordMarkov chordMarkov(Ingest ingest);

  /**
   Digest hash of any ingest of entities.
   NOT CACHED.

   @param ingest to digest
   @return hash digest of ingest
   */
  DigestHash hash(Ingest ingest);

  /**
   Digest memes of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return meme digest of ingest
   */
  DigestMeme meme(Ingest ingest);

  /**
   Digest sequence style of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return sequence style digest of ingest
   */
  DigestProgramStyle sequenceStyle(Ingest ingest);

}
