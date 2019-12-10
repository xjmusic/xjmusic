// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainConfig;
import io.xj.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.Tables.CHAIN_CONFIG;

public class ChainConfigDAOImpl extends DAOImpl<ChainConfig> implements ChainConfigDAO {

  @Inject
  public ChainConfigDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainConfig create(Access access, ChainConfig entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    return DAO.modelFrom(ChainConfig.class,
      executeCreate(CHAIN_CONFIG, entity));

  }

  @Override
  @Nullable
  public ChainConfig readOne(Access access, UUID id) throws CoreException {
    requireUser(access);
    return DAO.modelFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainConfig> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireUser(access);
    return DAO.modelsFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainConfig entity) throws CoreException {
    entity.validate();
    requireTopLevel(access);
    executeUpdate(CHAIN_CONFIG, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireLibrary(access);
    dbProvider.getDSL().deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.ID.eq(id))
      .execute();
  }

  @Override
  public ChainConfig newInstance() {
    return new ChainConfig();
  }

}
