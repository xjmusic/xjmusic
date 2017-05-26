// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.tables.records.AudioRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

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

   @param access       control
   @param instrumentId to fetch audios for.
   @return Result of audio records.
   @throws Exception on failure
   */
  Result<AudioRecord> readAll(Access access, ULong instrumentId) throws Exception;

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
   Delete a specified Audio if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
