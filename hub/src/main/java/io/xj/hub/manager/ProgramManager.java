// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.UUID;

public interface ProgramManager extends Manager<Program> {

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source program, of new record, and return it.
   https://www.pivotaltracker.com/story/show/170290553 Clone sub-entities of program

   @param access control
   @param cloneId   of program to clone
   @param entity    for the new Program
   @return newly readMany record
   */
  ManagerCloner<Program> clone(HubAccess access, UUID cloneId, Program entity) throws ManagerException;

  /**
   INSIDE ANOTHER DATABASE TRANSACTION
   When a library is cloned, also clone all programs within it https://www.pivotaltracker.com/story/show/181196881

   @return newly readMany record
   @param access control
   @param cloneId   of program to clone
   @param entity    for the new Program
   */
  ManagerCloner<Program> clone(DSLContext db, HubAccess access, UUID cloneId, Program entity) throws ManagerException;

  /**
   Read child entities of many programs

   @param access  control
   @param programIds to read
   @param types      of entities to include
   @return collection of entities
   */
  Collection<Object> readChildEntities(HubAccess access, Collection<UUID> programIds, Collection<String> types) throws ManagerException;

  /**
   Fetch all program visible to given access

   @param access control
   @return JSONArray of programs.
   @throws ManagerException on failure
   */
  Collection<Program> readMany(HubAccess access) throws ManagerException;

  /**
   Read all program in a given account

   @param access control
   @param accountId to read programs in
   @return programs
   @throws ManagerException on failure
   */
  Collection<Program> readManyInAccount(HubAccess access, String accountId) throws ManagerException;

  /**
   Fetch all Program in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state     to get programs in
   @return Result of program records.
   @throws ManagerException on failure
   */
  Collection<Program> readManyInState(HubAccess access, ProgramState state) throws ManagerException;

  /**
   Read all ids of Programs in the specified Library ids

   @param access  control
   @param libraryIds of which to get all program ids
   @return program ids in the specified library ids
   */
  Collection<UUID> readIdsInLibraries(HubAccess access, Collection<UUID> libraryIds) throws ManagerException;

  /**
   Read many programs including all child entities

   @param access  control
   @param programIds to read
   @return collection of entities
   */
  <N> Collection<N> readManyWithChildEntities(HubAccess access, Collection<UUID> programIds) throws ManagerException;
}
