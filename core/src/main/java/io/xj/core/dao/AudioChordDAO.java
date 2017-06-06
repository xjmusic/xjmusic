// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.app.access.impl.Access;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.tables.records.AudioChordRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface AudioChordDAO {

  /**
   Create a new AudioChord

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  AudioChordRecord create(Access access, AudioChord entity) throws Exception;

  /**
   Fetch one Audio Chord if accessible

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AudioChordRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Audio Chord for one Audio by id

   @param access  control
   @param audioId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  Result<AudioChordRecord> readAll(Access access, ULong audioId) throws Exception;

  /**
   Update a specified Audio Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param entity for the updated Chord.
   */
  void update(Access access, ULong id, AudioChord entity) throws Exception;

  /**
   Delete a specified Audio Chord if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
