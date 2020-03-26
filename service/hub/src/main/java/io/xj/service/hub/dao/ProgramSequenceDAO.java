// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramSequence;

import java.util.UUID;

public interface ProgramSequenceDAO extends DAO<ProgramSequence> {

  /**
   Clone a ProgramSequence and its children

   @param access  control
   @param cloneId of entity to clone attributes and children of
   @param entity  with attributes to set on new entity
   @return newly cloned entity
   */
  DAOCloner<ProgramSequence> clone(Access access, UUID cloneId, ProgramSequence entity) throws HubException;
}
