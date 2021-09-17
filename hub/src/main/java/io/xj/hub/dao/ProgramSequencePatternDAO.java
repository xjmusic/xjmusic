// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.access.HubAccess;

import java.util.UUID;

public interface ProgramSequencePatternDAO extends DAO<ProgramSequencePattern> {
  /**
   [#171617769] Artist editing Program clones a pattern
   [#173912361] Hub API create pattern cloning existing pattern
   <p>
   FIX [#176352798] Clone API for Artist editing a Program can clone a pattern including its events
   due to constraints of serializing and deserializing the empty JSON payload for cloning an object
   without setting values (we will do this better in the future)--
   when cloning a pattern, `type` and `total` will always be set from the source pattern, and cannot be overridden.

   @return cloner comprising newly cloned entity and its newly cloned child entities
   @param hubAccess control
   @param cloneId   of entity to clone attributes and children of
   @param entity    with attributes to set on new entity
   */
  DAOCloner<ProgramSequencePattern> clone(HubAccess hubAccess, UUID cloneId, ProgramSequencePattern entity) throws DAOException;
}
