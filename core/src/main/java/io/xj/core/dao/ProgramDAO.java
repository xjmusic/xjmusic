// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;

import java.math.BigInteger;
import java.util.Collection;

public interface ProgramDAO extends DAO<Program> {

  /**
   Provide an entity containing some new properties, but otherwise clone everything from a source program, create new record, and return it.

   @param access  control
   @param cloneId of program to clone
   @param entity  for the new Program
   @return newly readMany record
   */
  Program clone(Access access, BigInteger cloneId, Program entity) throws CoreException;

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
  Collection<Program> readAllInAccount(Access access, BigInteger accountId) throws CoreException;

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
   Erase a specified Program if accessible.
   [#154887174] ProgramErase job erase a Program and all its Patterns in the background, in order to keep the UI functioning at a reasonable speed.

   @param access control
   @param id     of specific program to erase.
   */
  void erase(Access access, BigInteger id) throws CoreException;
}
