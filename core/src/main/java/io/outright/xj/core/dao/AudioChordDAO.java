// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.audio_chord.AudioChordWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AudioChordDAO {

  /**
   Create a new AudioChord

   @param access control
   @param data   for the new Account User.
   @return newly created record as JSON
   */
  JSONObject create(AccessControl access, AudioChordWrapper data) throws Exception;

  /**
   Fetch one Audio Chord if accessible

   @param access control
   @param id     of audio
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Fetch all accessible Audio Chord for one Audio by id

   @param access  control
   @param audioId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong audioId) throws Exception;

  /**
   Update a specified Audio Chord if accessible

   @param access control
   @param id     of specific Chord to update.
   @param data   for the updated Chord.
   */
  void update(AccessControl access, ULong id, AudioChordWrapper data) throws Exception;

  /**
   Delete a specified Audio Chord if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
