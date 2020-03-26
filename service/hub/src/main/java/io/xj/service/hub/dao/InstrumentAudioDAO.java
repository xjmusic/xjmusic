// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.InstrumentAudio;

import java.util.Map;
import java.util.UUID;

public interface InstrumentAudioDAO extends DAO<InstrumentAudio> {

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param access control
   @param id     of audio
   @return retrieved record
   @throws HubException on failure
   */
  Map<String, String> authorizeUpload(Access access, UUID id) throws HubException;

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source instrumentAudio, of new record, and return it.
   [#170290553] Clone sub-entities of instruments

   @param access  control
   @param cloneId of instrumentAudio to clone
   @param entity  for the new InstrumentAudio
   @return newly readMany record
   */
  InstrumentAudio clone(Access access, UUID cloneId, InstrumentAudio entity) throws HubException;

}
