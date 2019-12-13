// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.InstrumentAudio;

import java.util.Map;
import java.util.UUID;

public interface InstrumentAudioDAO extends DAO<InstrumentAudio> {

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param access control
   @param id     of audio
   @return retrieved record
   @throws CoreException on failure
   */
  Map<String, String> authorizeUpload(Access access, UUID id) throws CoreException;

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source instrumentAudio, of new record, and return it.

   @param access  control
   @param cloneId of instrumentAudio to clone
   @param entity  for the new InstrumentAudio
   @return newly readMany record
   */
  InstrumentAudio clone(Access access, UUID cloneId, InstrumentAudio entity) throws CoreException;

}
