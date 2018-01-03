// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.voice.Voice;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface VoiceDAO {

  /**
   Create a new Voice

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  Voice create(Access access, Voice entity) throws Exception;

  /**
   Fetch one Voice if accessible

   @param access control
   @param id     of voice
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Voice readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Voice for one phase by id

   @return JSONArray of voices.
   @throws Exception on failure
    @param access  control
   @param patternId to fetch voices for.
   */
  Collection<Voice> readAll(Access access, BigInteger patternId) throws Exception;

  /**
   Update a specified Voice if accessible

   @param access control
   @param id     of specific Voice to update.
   @param entity for the updated Voice.
   */
  void update(Access access, BigInteger id, Voice entity) throws Exception;

  /**
   Delete a specified Voice if accessible

   @param access control
   @param id     of specific voice to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
