// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app.impl;

import com.google.inject.Inject;
import io.xj.core.app.Health;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

import static redis.clients.jedis.Protocol.Keyword.PONG;

/**
 Implementation of application health check
 */
public class HealthImpl implements Health {
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final SQLDatabaseProvider sqlDatabaseProvider;

  @Inject
  public HealthImpl(RedisDatabaseProvider redisDatabaseProvider, SQLDatabaseProvider sqlDatabaseProvider) {
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.sqlDatabaseProvider = sqlDatabaseProvider;
  }

  /**
   [#169242891] Core healthcheck should test disk usage on machine, report failure when available space below threshold

   @throws CoreException if local machine disk space is below threshold
   */
  private static void throwExceptionIfLocalMachineDiskSpaceBelowThreshold() throws CoreException {
    File file = new File("/");
    Double usableRatio = file.getUsableSpace() / (double) file.getTotalSpace();
    if (usableRatio > Config.getAlarmDiskFreeRatioLower()) return;

    throw new CoreException(String.format("Local machine has only %s free disk space!",
      new DecimalFormat("##.##%").format(usableRatio)));
  }

  @Override
  public void check() throws Exception {
    throwExceptionIfRedisDatabaseCannotBePinged();
    throwExceptionIfLocalMachineDiskSpaceBelowThreshold();
    throwExceptionIfSQLDatabaseCannotCompleteTransaction();
  }

  /**
   @throws CoreException if SQL database cannot complete a transaction
   */
  private void throwExceptionIfSQLDatabaseCannotCompleteTransaction() throws CoreException {
    sqlDatabaseProvider.getConnection();
  }

  /**
   @throws CoreException if Redis database cannot be pinged
   */
  private void throwExceptionIfRedisDatabaseCannotBePinged() throws CoreException {
    Jedis client = redisDatabaseProvider.getClient();
    String pingResult = client.ping();
    if (!Objects.equals(PONG.toString(), pingResult)) {
      client.close();
      throw new CoreException("Redis server ping result: " + pingResult);
    }
    client.close();
  }
}
