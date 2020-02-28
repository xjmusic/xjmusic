// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.digest;

import com.google.inject.assistedinject.Assisted;
import io.xj.lib.core.ingest.Ingest;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 DigestChordProgression entities = digestFactory.chordProgression(ingest);
 DigestMeme memes = digestFactory.meme(ingest);
 */
public interface DigestFactory {

  /**
   Digest entities of any ingest of entities.

   @param ingest to digest
   @return chord digest of ingest
   */
  DigestChordProgression chordProgression(
    @Assisted("ingest") Ingest ingest
  );

  /**
   Digest entities of any ingest of entities.

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
  DigestProgramStyle programStyle(
    @Assisted("ingest") Ingest ingest
  );

}
