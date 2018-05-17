// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import io.xj.craft.digest.hash.DigestHash;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.sequence_style.DigestSequenceStyle;
import io.xj.craft.ingest.Ingest;

import com.google.inject.assistedinject.Assisted;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Sequence, and Instrument for any purpose.
 DigestChordProgression chords = digestFactory.chordProgression(ingest);
 DigestMeme memes = digestFactory.meme(ingest);
 */
public interface DigestFactory {

  /**
   Digest chords of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   */
  DigestChordProgression chordProgression(
    @Assisted("ingest") Ingest ingest
  );

  /**
   Digest chords of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   */
  DigestChordMarkov chordMarkov(
    @Assisted("ingest") Ingest ingest
  );

  /**
   Digest hash of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   */
  DigestHash hashOf(
    @Assisted("ingest") Ingest ingest
  );

  /**
   Digest memes of any ingest of entities.

   @param ingest to digest
   @return meme digest of ingest
   */
  DigestMeme meme(
    @Assisted("ingest") Ingest ingest
  );

  /**
   Digest sequence style of any ingest of entities.

   @param ingest to digest
   @return sequence style digest of ingest
   */
  DigestSequenceStyle sequenceStyle(
    @Assisted("ingest") Ingest ingest
  );

}
