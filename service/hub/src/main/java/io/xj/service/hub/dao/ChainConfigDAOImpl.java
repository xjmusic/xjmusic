// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ChainConfig;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.CHAIN_CONFIG;

public class ChainConfigDAOImpl extends DAOImpl<ChainConfig> implements ChainConfigDAO {

  @Inject
  public ChainConfigDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainConfig create(Access access, ChainConfig entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireEngineer(access);
    return modelFrom(ChainConfig.class,
      executeCreate(dbProvider.getDSL(), CHAIN_CONFIG, entity));

  }

  @Override
  @Nullable
  public ChainConfig readOne(Access access, UUID id) throws HubException {
    requireEngineer(access);
    return modelFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainConfig> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireEngineer(access);
    return modelsFrom(ChainConfig.class,
      dbProvider.getDSL().selectFrom(CHAIN_CONFIG)
        .where(CHAIN_CONFIG.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainConfig entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireEngineer(access);
    executeUpdate(dbProvider.getDSL(), CHAIN_CONFIG, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
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
