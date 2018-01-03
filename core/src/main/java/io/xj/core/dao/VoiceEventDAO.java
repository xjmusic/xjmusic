// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.voice_event.VoiceEvent;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface VoiceEventDAO {

  /**
   Create a new VoiceEvent

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  VoiceEvent create(Access access, VoiceEvent entity) throws Exception;

  /**
   Fetch one Voice Event if accessible

   @param access control
   @param id     of voice
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  VoiceEvent readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Voice Event for one Voice by id

   @param access  control
   @param phaseId to fetch voices for.
   @return JSONArray of voices.
   @throws Exception on failure
   */
  Collection<VoiceEvent> readAll(Access access, BigInteger phaseId) throws Exception;

  /**
   Update a specified Voice Event if accessible

   @param access control
   @param id     of specific Event to update.
   @param entity for the updated Event.
   */
  void update(Access access, BigInteger id, VoiceEvent entity) throws Exception;

  /**
   Delete a specified Voice Event if accessible

   @param access control
   @param id     of specific voice to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
