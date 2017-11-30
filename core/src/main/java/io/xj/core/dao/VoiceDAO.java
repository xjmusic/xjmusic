// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.voice.Voice;
import io.xj.core.tables.records.VoiceRecord;

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
   Fetch all accessible Voice for one phase by id

   @param access  control
   @param phaseId to fetch voices for.
   @return JSONArray of voices.
   @throws Exception on failure
   */
  Result<VoiceRecord> readAll(Access access, ULong phaseId) throws Exception;

  /**
   Fetch all accessible Voice for an idea phase by offset

   @return voices in phase
    @param access      control
   @param ideaId      to fetch phase voices for
   @param phaseOffset offset of phase in idea
   */
  Result<VoiceRecord> readAllForIdeaPhaseOffset(Access access, ULong ideaId, ULong phaseOffset) throws Exception;

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
