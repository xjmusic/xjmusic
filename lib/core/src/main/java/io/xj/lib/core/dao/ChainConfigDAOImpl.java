// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.ChainConfig;
import io.xj.lib.core.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.CHAIN_CONFIG;

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
    requireEngineer(access);
    return DAO.modelFrom(ChainConfig.class,
      executeCreate(dbProvider.getDSL(), CHAIN_CONFIG, entity));

  }

  @Override
  @Nullable
  public ChainConfig readOne(Access access, UUID id) throws CoreException {
    requireEngineer(access);
    return DAO.modelFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainConfig> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireEngineer(access);
    return DAO.modelsFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainConfig entity) throws CoreException {
    entity.validate();
    requireEngineer(access);
    executeUpdate(dbProvider.getDSL(), CHAIN_CONFIG, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    requireEngineer(access);
    dbProvider.getDSL().deleteFrom(CHAIN_CONFIG)
      .where(CHAIN_CONFIG.ID.eq(id))
      .execute();
  }

  @Override
  public ChainConfig newInstance() {
    return new ChainConfig();
  }

}
