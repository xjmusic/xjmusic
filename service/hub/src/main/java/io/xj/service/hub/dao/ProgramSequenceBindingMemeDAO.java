// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ProgramSequenceBindingMemeDAO extends DAO<ProgramSequenceBindingMeme> {

  Collection<ProgramSequenceBindingMeme> readAllForPrograms(Access access, Set<UUID> programIds) throws HubException;
}
