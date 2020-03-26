// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariDataSource;
import io.xj.service.hub.dao.DAO;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static final Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);
  private final HikariDataSource dataSource;
  private final String schemas;

  /**
   Constructor
   */
  @Inject
  public SQLDatabaseProviderImpl(
    Config config
  ) {
    String database = config.getString("postgres.database");
    String host = config.getString("postgres.host");
    int port = config.getInt("postgres.port");
    String user = config.getString("postgres.user");
    String pass = config.getString("postgres.pass");
    String url = String.format("jdbc:postgresql://%s:%s/%s",
      host, port, database);
    schemas = config.getString("postgres.schemas");

    dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(user);
    dataSource.setPassword(pass);
    dataSource.setMaximumPoolSize(config.getInt("postgres.poolSizeMax"));

    log.info("HikariDataSource created for {}", url);
  }

  @Override
  public DSLContext getDSL() {
    return DAO.DSL(getDataSource());
  }

  @Override
  public HikariDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public void shutdown() {
    if (Objects.nonNull(dataSource) && dataSource.isRunning())
      dataSource.close();
  }

  @Override
  public String getSchemas() {
    return schemas;
  }

}
