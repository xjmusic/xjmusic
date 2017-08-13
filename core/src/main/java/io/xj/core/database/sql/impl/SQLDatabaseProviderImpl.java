// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.database.sql.impl;

import io.xj.core.Xj;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.exception.DatabaseException;
import io.xj.core.database.sql.SQLDatabaseProvider;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);

  private final String dbSchemaName = Config.dbMysqlDatabase();
  private final String url = "jdbc:mysql://" + Config.dbMysqlHost()
    + ":" + Config.dbMysqlPort()
    + "/" + dbSchemaName
    + "?useSSL=false&serverTimezone=UTC";
  private final String user = Config.dbMysqlUser();
  private final String pass = Config.dbMysqlPass();

  @Override
  public SQLConnection getConnection() throws DatabaseException {
    return getConnection(false);
  }

  @Override
  public SQLConnection getConnection(Boolean isTransaction) throws DatabaseException {
    try {
      Connection connection = DriverManager.getConnection(url, user, pass);
      DSLContext context = getContext(connection);
      if (isTransaction) {
        connection.setAutoCommit(false);
      }
      return new SQLConnection(connection, context, isTransaction);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  @Override
  public String getUrl() throws ConfigException {
    return url;
  }

  @Override
  public String getUser() throws ConfigException {
    return user;
  }

  @Override
  public String getPass() throws ConfigException {
    return pass;
  }

  private DSLContext getContext(Connection conn) {
    return DSL.using(conn, SQLDialect.MYSQL, getSettings());
  }

  /**
   Get SQL Database jOOQ settings

   @return jOOQ Settings
   */
  private Settings getSettings() {
    return new Settings()
      .withRenderMapping(new RenderMapping()
        .withSchemata(
          new MappedSchema().withInput(Xj.XJ.getName())
            .withOutput(dbSchemaName)
        )
      );
  }

}
