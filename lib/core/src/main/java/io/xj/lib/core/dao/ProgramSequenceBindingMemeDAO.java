// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ProgramSequenceBindingMeme;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ProgramSequenceBindingMemeDAO extends DAO<ProgramSequenceBindingMeme> {

  Collection<ProgramSequenceBindingMeme> readAllForPrograms(Access access, Set<UUID> programIds) throws CoreException;
}
