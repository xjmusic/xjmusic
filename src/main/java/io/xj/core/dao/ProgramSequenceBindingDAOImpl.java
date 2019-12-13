// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING;

public class ProgramSequenceBindingDAOImpl extends DAOImpl<ProgramSequenceBinding> implements ProgramSequenceBindingDAO {

  @Inject
  public ProgramSequenceBindingDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBinding create(Access access, ProgramSequenceBinding entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    return DAO.modelFrom(ProgramSequenceBinding.class,
      executeCreate(dbProvider.getDSL(), PROGRAM_SEQUENCE_BINDING, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceBinding readOne(Access access, UUID id) throws CoreException {
    requireArtist(access);
    return DAO.modelFrom(ProgramSequenceBinding.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBinding> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    return DAO.modelsFrom(ProgramSequenceBinding.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
        .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ProgramSequenceBinding entity) throws CoreException {
    entity.validate();
    requireArtist(access);
    executeUpdate(dbProvider.getDSL(), PROGRAM_SEQUENCE_BINDING, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireArtist(access);
    dbProvider.getDSL().deleteFrom(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBinding newInstance() {
    return new ProgramSequenceBinding();
  }

}
