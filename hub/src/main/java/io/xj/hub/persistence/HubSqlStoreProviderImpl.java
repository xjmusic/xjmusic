// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.api.client.util.Strings;
import com.zaxxer.hikari.HikariDataSource;
import io.xj.hub.manager.Manager;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HubSqlStoreProviderImpl implements HubSqlStoreProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubSqlStoreProviderImpl.class);
  private final HikariDataSource dataSource;
  private final String schemas;

  /**
   * Constructor
   */
  @Autowired
  public HubSqlStoreProviderImpl(
    @Value("${postgres.schemas}") String schemas,
    @Value("${cloud.sql.socket.factory}") String gcpCloudSqlSocketFactory,
    @Value("${cloud.sql.instance}") String gcpCloudSqlInstance,
    @Value("${postgres.database}") String postgresDatabase,
    @Value("${postgres.host}") String postgresHost,
    @Value("${postgres.port}") String postgresPort,
    @Value("${postgres.user}") String postgresUser,
    @Value("${postgres.pass}") String postgresPass,
    @Value("${postgres.pool.size.max}") Integer postgresPoolSizeMax
  ) {
    this.schemas = schemas;
    String url;
    LOG.info("Spring @Value 'cloud.sql.instance' {}", gcpCloudSqlInstance);
    LOG.info("System.getenv('cloud.sql.instance') {}", System.getenv("cloud.sql.instance"));
    if (Strings.isNullOrEmpty(gcpCloudSqlInstance)) {
      url = String.format("jdbc:postgresql://%s:%s/%s", postgresHost, postgresPort, postgresDatabase);
      LOG.info("Configured without GCP Cloud SQL instance, using host and port: {}", url);
    } else {
      url = String.format(
        "jdbc:postgresql:///%s?cloudSqlInstance=%s&socketFactory=%s&user=%s&password=%s",
        postgresDatabase, gcpCloudSqlInstance, gcpCloudSqlSocketFactory, postgresUser, postgresPass);
      LOG.info("Configured with GCP Cloud SQL instance, using socket factory: {}", url);
    }

    dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(postgresUser);
    dataSource.setPassword(postgresPass);
    dataSource.setMaximumPoolSize(postgresPoolSizeMax);

    LOG.info("HikariDataSource created for {}", url);
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
