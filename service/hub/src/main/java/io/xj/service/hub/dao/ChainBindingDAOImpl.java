// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.persistence.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.CHAIN_BINDING;

public class ChainBindingDAOImpl extends DAOImpl<ChainBinding> implements ChainBindingDAO {

  @Inject
  public ChainBindingDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ChainBinding create(Access access, ChainBinding entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireEngineer(access);
    return modelFrom(ChainBinding.class,
      executeCreate(dbProvider.getDSL(), CHAIN_BINDING, entity));

  }

  @Override
  @Nullable
  public ChainBinding readOne(Access access, UUID id) throws HubException {
    requireEngineer(access);
    return modelFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.ID.eq(id))
        .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ChainBinding> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    requireEngineer(access);
    return modelsFrom(ChainBinding.class,
      dbProvider.getDSL().selectFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.CHAIN_ID.in(parentIds))
        .fetch());
  }

  @Override
  public void update(Access access, UUID id, ChainBinding entity) throws HubException, RestApiException, ValueException {
    entity.validate();
    requireEngineer(access);
    executeUpdate(dbProvider.getDSL(), CHAIN_BINDING, id, entity);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
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
