// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.zaxxer.hikari.HikariDataSource;
import io.xj.hub.manager.Manager;
import io.xj.lib.app.AppEnvironment;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HubSqlStoreProviderImpl implements HubSqlStoreProvider {
  private static final Logger log = LoggerFactory.getLogger(HubSqlStoreProviderImpl.class);
  private final HikariDataSource dataSource;
  private final String schemas;

  /**
   * Constructor
   */
  public HubSqlStoreProviderImpl(
    AppEnvironment env
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

    log.info("HikariDataSource created for {}", url);
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
