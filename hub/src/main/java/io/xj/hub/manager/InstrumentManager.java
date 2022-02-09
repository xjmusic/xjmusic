// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.Instrument;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

public interface InstrumentManager extends Manager<Instrument> {

  /**
   Clone an Instrument into a new Instrument
   [#170290553] Clone sub-entities of instruments

   @param access control
   @param cloneId   of instrument to clone
   @param entity    for the new Instrument
   @return newly readMany record
   */
  ManagerCloner<Instrument> clone(HubAccess access, UUID cloneId, Instrument entity) throws ManagerException;

  /**
   INSIDE ANOTHER DATABASE TRANSACTION
   When a library is cloned, also clone all instruments within it #181196881

   @return newly readMany record
   @param access control
   @param cloneId   of instrument to clone
   @param entity    for the new Instrument
   */
  ManagerCloner<Instrument> clone(DSLContext db, HubAccess access, UUID cloneId, Instrument entity) throws ManagerException;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access control
   @param accountId to fetch instruments for.
   @return Collection of instruments.
   @throws ManagerException on failure
   */
  Collection<Instrument> readManyInAccount(HubAccess access, String accountId) throws ManagerException;

  /**
   Fetch all instrument visible to given hubAccess

   @param access control
   @return Collection of instruments.
   @throws ManagerException on failure
   */
  Collection<Instrument> readMany(HubAccess access) throws ManagerException;

  /**
   Read all ids of Instruments in the specified Library ids

   @param access  control
   @param libraryIds of which to get all instrument ids
   @return instrument ids in the specified library ids
   */
  Collection<UUID> readIdsInLibraries(HubAccess access, Collection<UUID> libraryIds) throws ManagerException;

  /**
   Read many instruments including all child entities

   @param access     control
   @param instrumentIds to read
   @return collection of entities
   */
  <N> Collection<N> readManyWithChildEntities(HubAccess access, Collection<UUID> instrumentIds) throws ManagerException;

  /**
   Read child entities of many instruments

   @param access  control
   @param instrumentIds to read
   @param types      of entities to include
   @return collection of entities
   */
  Collection<Object> readChildEntities(HubAccess access, Collection<UUID> instrumentIds, Collection<String> types) throws ManagerException;
}
