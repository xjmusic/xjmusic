// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.ProgramSequence;

import java.util.UUID;

public interface ProgramSequenceDAO extends DAO<ProgramSequence> {

  /**
   Clone a ProgramSequence and its children

   @param hubAccess control
   @param cloneId   of entity to clone attributes and children of
   @param entity    with attributes to set on new entity
   @return cloner comprising newly cloned entity and its newly cloned child entities
   */
  DAOCloner<ProgramSequence> clone(HubAccess hubAccess, UUID cloneId, ProgramSequence entity) throws DAOException;
}
