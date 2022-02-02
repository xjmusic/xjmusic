// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;
import io.xj.hub.manager.Manager;
import io.xj.lib.app.Environment;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
class HubDatabaseProviderImpl implements HubDatabaseProvider {
  private static final Logger log = LoggerFactory.getLogger(HubDatabaseProviderImpl.class);
  private final HikariDataSource dataSource;
  private final String schemas;

  /**
   Constructor
   */
  @Inject
  public HubDatabaseProviderImpl(
    Environment env
  ) {
    String database = env.getPostgresDatabase();
    String host = env.getPostgresHost();
    int port = env.getPostgresPort();
    String user = env.getPostgresUser();
    String pass = env.getPostgresPass();
    String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
    schemas = env.getPostgresSchemas();
    dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(user);
    dataSource.setPassword(pass);
    dataSource.setMaximumPoolSize(env.getPostgresPoolSizeMax());

    log.debug("HikariDataSource created for {}", url);
  }

  @Override
  public DSLContext getDSL() {
    return Manager.DSL(getDataSource());
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
