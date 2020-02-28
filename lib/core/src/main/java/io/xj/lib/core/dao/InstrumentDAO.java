// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Instrument;

import java.util.Collection;
import java.util.UUID;

public interface InstrumentDAO extends DAO<Instrument> {

  /**
   Clone a Instrument into a new Instrument
   [#170290553] Clone sub-entities of instruments

   @param access  control
   @param cloneId of instrument to clone
   @param entity  for the new Instrument
   @return newly readMany record
   */
  Instrument clone(Access access, UUID cloneId, Instrument entity) throws CoreException;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access    control
   @param accountId to fetch instruments for.
   @return Collection of instruments.
   @throws CoreException on failure
   */
  Collection<Instrument> readAllInAccount(Access access, UUID accountId) throws CoreException;

  /**
   Fetch all instrument visible to given access

   @param access control
   @return Collection of instruments.
   @throws CoreException on failure
   */
  Collection<Instrument> readAll(Access access) throws CoreException;

  /**
   Read all ids of Instruments in the specified Library ids

   @param access     control
   @param libraryIds of which to get all instrument ids
   @return instrument ids in the specified library ids
   */
  Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> libraryIds) throws CoreException;

  /**
   Read many instruments including all child entities

   @param access     control
   @param instrumentIds to read
   @return collection of entities
   */
  Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> instrumentIds) throws CoreException;
}
