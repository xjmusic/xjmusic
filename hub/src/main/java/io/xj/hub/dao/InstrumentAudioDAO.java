// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.access.HubAccess;
import io.xj.lib.filestore.FileStoreException;

import java.util.Map;
import java.util.UUID;

public interface InstrumentAudioDAO extends DAO<InstrumentAudio> {

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @return retrieved record
   @throws DAOException on failure
   @param hubAccess control
   @param id        of audio
   */
  Map<String, String> authorizeUpload(HubAccess hubAccess, UUID id) throws DAOException, FileStoreException;

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source instrumentAudio, of new record, and return it.
   [#170290553] Clone sub-entities of instruments

   @return newly readMany record
   @param hubAccess control
   @param cloneId   of instrumentAudio to clone
   @param entity    for the new InstrumentAudio
   */
  InstrumentAudio clone(HubAccess hubAccess, UUID cloneId, InstrumentAudio entity) throws DAOException;

}
