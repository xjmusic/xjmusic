// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.audio.AudioWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AudioDAO {

  /**
   Create a new Audio

   @param access control
   @param data   for the new Account User.
   @return newly created record as JSON
   */
  JSONObject create(AccessControl access, AudioWrapper data) throws Exception;

  /**
   Fetch one Audio if accessible

   @param access control
   @param id     of audio
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param access control
   @param id     of audio
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject uploadOne(AccessControl access, ULong id) throws Exception;

  /**
   Fetch all accessible Audio for one Account by id

   @param access       control
   @param instrumentId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong instrumentId) throws Exception;

  /**
   Update a specified Audio if accessible

   @param access control
   @param id     of specific Audio to update.
   @param data   for the updated Audio.
   */
  void update(AccessControl access, ULong id, AudioWrapper data) throws Exception;

  /**
   Delete a specified Audio if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
