// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.ProgramSequence;

import java.util.UUID;

public interface ProgramSequenceDAO extends DAO<ProgramSequence> {

  /**
   Clone a ProgramSequence and its children

   @return cloner comprising newly cloned entity and its newly cloned child entities
   @param hubAccess control
   @param cloneId   of entity to clone attributes and children of
   @param entity    with attributes to set on new entity
   */
  DAOCloner<ProgramSequence> clone(HubAccess hubAccess, UUID cloneId, ProgramSequence entity) throws DAOException;
}
