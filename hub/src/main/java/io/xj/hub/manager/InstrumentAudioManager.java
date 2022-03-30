// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.filestore.FileStoreException;

import java.util.Map;
import java.util.UUID;

public interface InstrumentAudioManager extends Manager<InstrumentAudio> {

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @return retrieved record
   @throws ManagerException on failure
   @param access control
   @param id        of audio
   @param extension of audio file
   */
  Map<String, String> authorizeUpload(HubAccess access, UUID id, String extension) throws ManagerException, FileStoreException;

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source instrumentAudio, of new record, and return it.
   https://www.pivotaltracker.com/story/show/170290553 Clone sub-entities of instruments

   @param access control
   @param cloneId   of instrumentAudio to clone
   @param entity    for the new InstrumentAudio
   @return newly readMany record
   */
  InstrumentAudio clone(HubAccess access, UUID cloneId, InstrumentAudio entity) throws ManagerException;

}
