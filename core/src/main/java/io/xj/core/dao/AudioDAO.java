// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;

import org.json.JSONObject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface AudioDAO extends DAO<Audio> {

  /**
   Clone a Audio into a new Audio

   @param access  control
   @param cloneId of audio to clone
   @param entity  for the new Audio
   @return newly readMany record
   */
  Audio clone(Access access, BigInteger cloneId, Audio entity) throws CoreException;

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param access control
   @param id     of audio
   @return retrieved record
   @throws CoreException on failure
   */
  @Nullable
  JSONObject authorizeUpload(Access access, BigInteger id) throws CoreException;

  /**
   Fetch all Audio in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get audios in
   @return Result of audio records.
   @throws CoreException on failure
   */
  Collection<Audio> readAllInState(Access access, AudioState state) throws CoreException;

  /**
   Erase a specified Audio if accessible

   @param access control
   @param id     of specific audio to erase.
   */
  void erase(Access access, BigInteger id) throws CoreException;
}
