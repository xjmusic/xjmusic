// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.voice_event.VoiceEvent;
import io.outright.xj.core.tables.records.VoiceEventRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface VoiceEventDAO {

  /**
   Create a new VoiceEvent

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  VoiceEventRecord create(Access access, VoiceEvent entity) throws Exception;

  /**
   Fetch one Voice Event if accessible

   @param access control
   @param id     of voice
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  VoiceEventRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Voice Event for one Voice by id

   @param access  control
   @param voiceId to fetch voices for.
   @return JSONArray of voices.
   @throws Exception on failure
   */
  Result<VoiceEventRecord> readAll(Access access, ULong voiceId) throws Exception;

  /**
   Update a specified Voice Event if accessible

   @param access control
   @param id     of specific Event to update.
   @param entity for the updated Event.
   */
  void update(Access access, ULong id, VoiceEvent entity) throws Exception;

  /**
   Delete a specified Voice Event if accessible

   @param access control
   @param id     of specific voice to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
