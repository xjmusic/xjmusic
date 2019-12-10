// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainBinding;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.CHAIN_BINDING;

public class ChainBindingDAOImpl extends DAOImpl<ChainBinding> implements ChainBindingDAO {

  @Inject
  public ChainBindingDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainBinding create(Access access, ChainBinding entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ChainBinding.class,
      executeCreate(CHAIN_BINDING, entity));

  }

  @Override
  @Nullable
  public ChainBinding readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainBinding> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainBinding entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(CHAIN_BINDING, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(CHAIN_BINDING)
      .where(CHAIN_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public ChainBinding newInstance() {
    return new ChainBinding();
  }

}
