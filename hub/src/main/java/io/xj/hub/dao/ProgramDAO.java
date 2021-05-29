// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.protobuf.MessageLite ;
import io.xj.Program;
import io.xj.hub.access.HubAccess;

import java.util.Collection;

public interface ProgramDAO extends DAO<Program> {

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source program, of new record, and return it.
   [#170290553] Clone sub-entities of program

   @param hubAccess control
   @param cloneId   of program to clone
   @param entity    for the new Program
   @return newly readMany record
   */
  DAOCloner<Program> clone(HubAccess hubAccess, String cloneId, Program entity) throws DAOException;

  /**
   Read child entities of many programs

   @param hubAccess  control
   @param programIds to read
   @param types      of entities to include
   @return collection of entities
   */
  Collection<Object> readChildEntities(HubAccess hubAccess, Collection<String> programIds, Collection<String> types) throws DAOException;

  /**
   Fetch all program visible to given hubAccess

   @param hubAccess control
   @return JSONArray of programs.
   @throws DAOException on failure
   */
  Collection<Program> readMany(HubAccess hubAccess) throws DAOException;

  /**
   Read all program in a given account

   @param hubAccess control
   @param accountId to read programs in
   @return programs
   @throws DAOException on failure
   */
  Collection<Program> readManyInAccount(HubAccess hubAccess, String accountId) throws DAOException;

  /**
   Fetch all Program in a certain state
   [INTERNAL USE ONLY]

   @param hubAccess control
   @param state     to get programs in
   @return Result of program records.
   @throws DAOException on failure
   */
  Collection<Program> readManyInState(HubAccess hubAccess, Program.State state) throws DAOException;

  /**
   Read all ids of Programs in the specified Library ids

   @param libraryIds of which to get all program ids
   @param hubAccess  control
   @return program ids in the specified library ids
   */
  Collection<String> readIdsInLibraries(HubAccess hubAccess, Collection<String> libraryIds) throws DAOException;

  /**
   Read many programs including all child entities

   @param hubAccess  control
   @param programIds to read
   @return collection of entities
   */
  <N extends MessageLite> Collection<N> readManyWithChildEntities(HubAccess hubAccess, Collection<String> programIds) throws DAOException;
}
