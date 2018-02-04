// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.inject.assistedinject.Assisted;

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
public interface DigestFactory {

  /**
   Digest chords of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestChordProgression chordProgression(
    @Assisted("ingest") Ingest ingest
  ) throws Exception;

  /**
   Digest chords of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   @throws Exception on failure to of target entities
   */
  DigestChordMarkov chordMarkov(
    @Assisted("ingest") Ingest ingest
  ) throws Exception;

  /**
   Digest hash of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   @throws Exception on failure
   */
  DigestHash hashOf(
    @Assisted("ingest") Ingest ingest
  ) throws Exception;

  /**
   Digest memes of any ingest of entities.

   @param ingest to digest
   @return meme digest of ingest
   @throws Exception on failure
   */
  DigestMeme meme(
    @Assisted("ingest") Ingest ingest
  ) throws Exception;

  /**
   Digest pattern style of any ingest of entities.

   @param ingest to digest
   @return pattern style digest of ingest
   @throws Exception on failure
   */
  DigestPatternStyle patternStyle(
    @Assisted("ingest") Ingest ingest
  ) throws Exception;

}
