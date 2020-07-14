// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.ProgramSequenceBindingMeme;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ProgramSequenceBindingMemeDAO extends DAO<ProgramSequenceBindingMeme> {

  Collection<ProgramSequenceBindingMeme> readAllForPrograms(HubAccess hubAccess, Set<UUID> programIds) throws DAOException;
}
