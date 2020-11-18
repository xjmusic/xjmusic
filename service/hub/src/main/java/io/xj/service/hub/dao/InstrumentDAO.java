// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.protobuf.MessageLite ;
import io.xj.Instrument;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface InstrumentDAO extends DAO<Instrument> {

  /**
   Clone a Instrument into a new Instrument
   [#170290553] Clone sub-entities of instruments

   @param hubAccess control
   @param cloneId   of instrument to clone
   @param entity    for the new Instrument
   @return newly readMany record
   */
  Instrument clone(HubAccess hubAccess, String cloneId, Instrument entity) throws DAOException;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param hubAccess control
   @param accountId to fetch instruments for.
   @return Collection of instruments.
   @throws DAOException on failure
   */
  Collection<Instrument> readManyInAccount(HubAccess hubAccess, String accountId) throws DAOException;

  /**
   Fetch all instrument visible to given hubAccess

   @param hubAccess control
   @return Collection of instruments.
   @throws DAOException on failure
   */
  Collection<Instrument> readMany(HubAccess hubAccess) throws DAOException;

  /**
   Read all ids of Instruments in the specified Library ids

   @param hubAccess  control
   @param libraryIds of which to get all instrument ids
   @return instrument ids in the specified library ids
   */
  Collection<String> readIdsInLibraries(HubAccess hubAccess, Collection<String> libraryIds) throws DAOException;

  /**
   Read many instruments including all child entities

   @param hubAccess     control
   @param instrumentIds to read
   @return collection of entities
   */
  <N extends MessageLite> Collection<N> readManyWithChildEntities(HubAccess hubAccess, Collection<String> instrumentIds) throws DAOException;
}
