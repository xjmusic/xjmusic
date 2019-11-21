// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@Singleton
public class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static final Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);
  private PGConnectionPoolDataSource dataSource;

  /**
   Constructor
   */
  @Inject
  public SQLDatabaseProviderImpl() {
  }

  /**
   New CoreException wrapping SQL exception

   @param e SQLException to wrap
   @return CoreException
   */
  private static CoreException newException(SQLException e) {
    log.error(e.getMessage(), e);
    return new CoreException("SQL newException!", e);
  }

  @Override
  public Connection getConnection() throws CoreException {
    try {
      Connection connection = getPooledConnection().getConnection();
      if (Objects.isNull(connection)) throw new CoreException("Failed to make connection.");
      return connection;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new CoreException("SQL newException!", e);
    }
  }

  /**
   Instantiates a pooled connection only once; after that, it returns that connection

   @return pooled connection
   @throws CoreException on failure
   */
  public PooledConnection getPooledConnection() throws CoreException {
    try {
      return getDataSource().getPooledConnection(getUser(), getPassword());
    } catch (SQLException e) {
      throw newException(e);
    }
  }

  /**
   Instantiates a pooled data source only once; after that, it returns that data source

   @return connection pool data source
   */
  public ConnectionPoolDataSource getDataSource() {
    if (Objects.isNull(dataSource)) {
      dataSource = new PGConnectionPoolDataSource();
      dataSource.setUrl(getUrl());
      dataSource.setDatabaseName(Config.getDbPostgresDatabase());
    }

    return dataSource;
  }

  @Override
  public String getUrl() {
    return String.format("jdbc:postgresql://%s:%s/%s",
      Config.getDbPostgresHost(), Config.getDbPostgresPort(), Config.getDbPostgresDatabase());
  }

  @Override
  public String getUser() {
    return Config.getDbPostgresUser();
  }

  @Override
  public String getPassword() {
    return Config.getDbPostgresPass();
  }

}
