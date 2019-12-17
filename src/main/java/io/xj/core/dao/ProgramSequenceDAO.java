// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequence;

import java.util.UUID;

public interface ProgramSequenceDAO extends DAO<ProgramSequence> {

  /**
   Clone a ProgramSequence and its children

   @param access  control
   @param cloneId of entity to clone attributes and children of
   @param entity  with attributes to set on new entity
   @return newly cloned entity
   */
  DAOCloner<ProgramSequence> clone(Access access, UUID cloneId, ProgramSequence entity) throws CoreException;
}
