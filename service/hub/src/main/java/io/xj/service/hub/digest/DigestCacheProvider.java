// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import io.xj.service.hub.ingest.HubIngest;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
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
  DigestChordProgression chordProgression(HubIngest ingest);

  /**
   Digest chord Markov entities of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return chord Markov digest of ingest
   */
  DigestChordMarkov chordMarkov(HubIngest ingest);

  /**
   Digest hash of any ingest of entities.
   NOT CACHED.

   @param ingest to digest
   @return hash digest of ingest
   */
  DigestHash hash(HubIngest ingest);

  /**
   Digest memes of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return meme digest of ingest
   */
  DigestMeme meme(HubIngest ingest);

  /**
   Digest sequence style of any ingest of entities.
   CACHED result based on hash digest of ingest.

   @param ingest to digest
   @return sequence style digest of ingest
   */
  DigestProgramStyle sequenceStyle(HubIngest ingest);

}
