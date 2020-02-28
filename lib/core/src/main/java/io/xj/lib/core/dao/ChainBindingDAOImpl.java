// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ChainBinding;
import io.xj.lib.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.CHAIN_BINDING;

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
    requireEngineer(access);
    return DAO.modelFrom(ChainBinding.class,
      executeCreate(dbProvider.getDSL(), CHAIN_BINDING, entity));

  }

  @Override
  @Nullable
  public ChainBinding readOne(Access access, UUID id) throws CoreException {
    requireEngineer(access);
    return DAO.modelFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainBinding> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireEngineer(access);
    return DAO.modelsFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainBinding entity) throws CoreException {
    entity.validate();
    requireEngineer(access);
    executeUpdate(dbProvider.getDSL(), CHAIN_BINDING, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireEngineer(access);
    dbProvider.getDSL().deleteFrom(CHAIN_BINDING)
      .where(CHAIN_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public ChainBinding newInstance() {
    return new ChainBinding();
  }

}
