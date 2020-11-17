// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.InstrumentAudio;
import io.xj.lib.filestore.FileStoreException;
import io.xj.service.hub.access.HubAccess;

import java.util.Map;

public interface InstrumentAudioDAO extends DAO<InstrumentAudio> {

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @param hubAccess control
   @param id        of audio
   @return retrieved record
   @throws DAOException on failure
   */
  Map<String, String> authorizeUpload(HubAccess hubAccess, String id) throws DAOException, FileStoreException;

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source instrumentAudio, of new record, and return it.
   [#170290553] Clone sub-entities of instruments

   @param hubAccess control
   @param cloneId   of instrumentAudio to clone
   @param entity    for the new InstrumentAudio
   @return newly readMany record
   */
  InstrumentAudio clone(HubAccess hubAccess, String cloneId, InstrumentAudio entity) throws DAOException;

}
