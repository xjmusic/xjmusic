// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.voice_event.VoiceEventWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface VoiceEventDAO {

  /**
   * Create a new VoiceEvent
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, VoiceEventWrapper data) throws Exception;

  /**
   * Fetch one Voice Event if accessible
   *
   * @param access control
   * @param id     of voice
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch all accessible Voice Event for one Voice by id
   *
   * @param access    control
   * @param voiceId to fetch voices for.
   * @return JSONArray of voices.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong voiceId) throws Exception;

  /**
   * Update a specified Voice Event if accessible
   *
   * @param access control
   * @param id of specific Event to update.
   * @param data   for the updated Event.
   */
  void update(AccessControl access, ULong id, VoiceEventWrapper data) throws Exception;

  /**
   * Delete a specified Voice Event if accessible
   *
   * @param access control
   * @param id of specific voice to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
