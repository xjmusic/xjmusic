// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.VoiceRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface VoiceDAO {

  /**
   Create a new Voice

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  VoiceRecord create(Access access, Voice entity) throws Exception;

  /**
   Fetch one Voice if accessible

   @param access control
   @param id     of voice
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  VoiceRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Voice for one Account by id

   @param access control
   @param ideaId to fetch voices for.
   @return JSONArray of voices.
   @throws Exception on failure
   */
  Result<VoiceRecord> readAll(Access access, ULong ideaId) throws Exception;

  /**
   Update a specified Voice if accessible

   @param access control
   @param id     of specific Voice to update.
   @param entity for the updated Voice.
   */
  void update(Access access, ULong id, Voice entity) throws Exception;

  /**
   Delete a specified Voice if accessible

   @param access control
   @param id     of specific voice to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
