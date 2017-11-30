// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.tables.records.AudioRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AudioDAO {

  /**
   Create a new Audio

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  AudioRecord create(Access access, Audio entity) throws Exception;

  /**
   Fetch one Audio if accessible

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AudioRecord readOne(Access access, ULong id) throws Exception;

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  JSONObject uploadOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Audio for one Instrument by id
   [#326] Instruments Audios returned in order of name

   @param access       control
   @param instrumentId to fetch audios for.
   @return Result of audio records.
   @throws Exception on failure
   */
  Result<AudioRecord> readAll(Access access, ULong instrumentId) throws Exception;

  /**
   Fetch all Audio in a certain state
   [INTERNAL USE ONLY]

   @return Result of audio records.
   @throws Exception on failure
    @param access       control
   @param state to get audios in
   */
  Result<AudioRecord> readAllInState(Access access, AudioState state) throws Exception;

  /**
   Fetch all accessible Audio picked for a link

   @param access       control
   @param linkId to fetch audios picked for.
   @return Result of audio records.
   @throws Exception on failure
   */
  Result<AudioRecord> readAllPickedForLink(Access access, ULong linkId) throws Exception;

  /**
   Update a specified Audio if accessible

   @param access control
   @param id     of specific Audio to update.
   @param entity for the updated Audio.
   */
  void update(Access access, ULong id, Audio entity) throws Exception;

  /**
   Delete a specified Audio

   @param access control
   @param id     of specific audio to delete.
   */
  void destroy(Access access, ULong id) throws Exception;

  /**
   Erase a specified Audio if accessible

   @param access control
   @param id     of specific audio to erase.
   */
  void erase(Access access, ULong id) throws Exception;
}
