// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql.impl;

import com.google.inject.Singleton;
import com.mysql.cj.jdbc.MysqlDataSource;
import io.xj.core.Xj;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static final Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);

  /**
   Get DSL context

   @param conn SQL connection
   @return DSL context
   */
  private static DSLContext getContext(Connection conn) {
    return DSL.using(conn, SQLDialect.MYSQL, getSettings());
  }

  /**
   Get SQL Database jOOQ settings

   @return jOOQ Settings
   */
  private static Settings getSettings() {
    return new Settings()
      .withRenderMapping(new RenderMapping()
        .withSchemata(
          new MappedSchema().withInput(Xj.XJ.getName())
            .withOutput(Config.getDbMysqlDatabase())
        )
      );
  }

  @Override
  public SQLConnection getConnection() throws CoreException {
    return createConnection(false);
  }

  @Override
  public SQLConnection getConnection(Boolean isTransaction) throws CoreException {
    return createConnection(isTransaction);
  }

  /**
   Get a SQL connection to a specified URL

   @param isTransaction true if transaction
   @return SQL connect
   */
  private SQLConnection createConnection(Boolean isTransaction) throws CoreException {
    try {
      MysqlDataSource mysqlDataSource = new MysqlDataSource();
      mysqlDataSource.setURL(getUrl());
      mysqlDataSource.setUser(getUser());
      mysqlDataSource.setPassword(getPassword());
      Connection connection = mysqlDataSource.getConnection();
      DSLContext context = getContext(connection);
      if (isTransaction) {
        connection.setAutoCommit(false);
      }
      return new SQLConnection(connection, context, isTransaction);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new CoreException("SQL exception!", e);
    }
  }

  @Override
  public String getUrl() {
    return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&maxAllowedPacket=67108864",
      Config.getDbMysqlHost(), Config.getDbMysqlPort(), Config.getDbMysqlDatabase());
  }

  @Override
  public String getUser() {
    return Config.getDbMysqlUser();
  }

  @Override
  public String getPassword() {
    return Config.getDbMysqlPass();
  }

}
