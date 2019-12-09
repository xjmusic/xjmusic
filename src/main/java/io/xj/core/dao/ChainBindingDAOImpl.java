// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainBinding;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
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
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      return DAORecord.modelFrom(ChainBinding.class,
        executeCreate(connection, CHAIN_BINDING, entity));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public ChainBinding readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelFrom(ChainBinding.class,
        DAORecord.DSL(connection).selectFrom(CHAIN_BINDING)
          .where(CHAIN_BINDING.ID.eq(id))
          .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<ChainBinding> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireUser(access);
      return DAORecord.modelsFrom(ChainBinding.class,
        DAORecord.DSL(connection).selectFrom(CHAIN_BINDING)
          .where(CHAIN_BINDING.CHAIN_ID.in(parentIds))
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, ChainBinding entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      entity.validate();
      requireTopLevel(access);
      executeUpdate(connection, CHAIN_BINDING, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireLibrary(access);
      DAORecord.DSL(connection).deleteFrom(CHAIN_BINDING)
        .where(CHAIN_BINDING.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public ChainBinding newInstance() {
    return new ChainBinding();
  }

}
