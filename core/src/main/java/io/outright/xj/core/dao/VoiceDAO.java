// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.voice.VoiceWrapper;

import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface VoiceDAO {

  /**
   * Create a new Voice
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, VoiceWrapper data) throws Exception;

  /**
   * Fetch one Voice if accessible
   *
   * @param access control
   * @param id     of voice
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch all accessible Voice for one Account by id
   *
   * @param access    control
   * @param ideaId to fetch voices for.
   * @return JSONArray of voices.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong ideaId) throws Exception;

  /**
   * Update a specified Voice if accessible
   *
   * @param access control
   * @param id of specific Voice to update.
   * @param data   for the updated Voice.
   */
  void update(AccessControl access, ULong id, VoiceWrapper data) throws Exception;

  /**
   * Delete a specified Voice if accessible
   *
   * @param access control
   * @param id of specific voice to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
