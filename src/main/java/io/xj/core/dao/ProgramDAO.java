// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;

import java.util.Collection;
import java.util.UUID;

public interface ProgramDAO extends DAO<Program> {

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source program, of new record, and return it.
   [#170290553] Clone sub-entities of program

   @param access  control
   @param cloneId of program to clone
   @param entity  for the new Program
   @return newly readMany record
   */
  Program clone(Access access, UUID cloneId, Program entity) throws CoreException;

  /**
   Fetch all program visible to given access

   @param access control
   @return JSONArray of programs.
   @throws CoreException on failure
   */
  Collection<Program> readAll(Access access) throws CoreException;

  /**
   Read all program in a given account

   @param access    control
   @param accountId to read programs in
   @return programs
   @throws CoreException on failure
   */
  Collection<Program> readAllInAccount(Access access, UUID accountId) throws CoreException;

  /**
   Fetch all Program in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get programs in
   @return Result of program records.
   @throws CoreException on failure
   */
  Collection<Program> readAllInState(Access access, ProgramState state) throws CoreException;

  /**
   Read all ids of Programs in the specified Library ids

   @param libraryIds of which to get all program ids
   @param access     control
   @return program ids in the specified library ids
   */
  Collection<UUID> readIdsInLibraries(Access access, Collection<UUID> libraryIds) throws CoreException;

  /**
   Read many programs including all child entities

   @param access     control
   @param programIds to read
   @return collection of entities
   */
  Collection<Entity> readManyWithChildEntities(Access access, Collection<UUID> programIds) throws CoreException;
}
