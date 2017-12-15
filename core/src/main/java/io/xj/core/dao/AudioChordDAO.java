// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio_chord.AudioChord;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface AudioChordDAO {

  /**
   Create a new AudioChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  AudioChord create(Access access, AudioChord entity) throws Exception;

  /**
   Fetch one Audio Chord if accessible

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AudioChord readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Audio Chord for one Audio by id

   @param access  control
   @param audioId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  Collection<AudioChord> readAll(Access access, BigInteger audioId) throws Exception;

  /**
   Update a specified Audio Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, BigInteger id, AudioChord entity) throws Exception;

  /**
   Delete a specified Audio Chord if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
