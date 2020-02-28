// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.app;

import com.google.inject.Injector;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.persistence.RedisDatabaseProvider;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import redis.clients.jedis.Jedis;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Objects;

import static redis.clients.jedis.Protocol.Keyword.PONG;

/**
 This resource (along with everything in this `core/resource` package)
 is imported by all JAX-RS resources (e.g. in the Hub or Worker app),
 and results in an /o2 healthcheck route made available in all apps.
 <p>
 Due to Jersey JAX-RS implementing an HK2 injection vector,
 we cannot use Guice injection at the class level.
 That's why it appears inside this class as a manual inject.getInstance(...for each class...)
 <p>
 FUTURE: determine a more testable injection vector
 */
@Path("o2")
public class HealthcheckResource extends AppResource {
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final SQLDatabaseProvider sqlDatabaseProvider;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public HealthcheckResource(
    Injector injector
  ) {
    super(injector);
    redisDatabaseProvider = injector.getInstance(RedisDatabaseProvider.class);
    sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);
  }

  /**
   [#169242891] Core healthcheck should test disk usage on machine, report failure when available space below threshold

   @param threshold below which to throw alarm
   @throws CoreException if local machine disk space is below threshold
   */
  private static void throwExceptionIfLocalMachineDiskSpaceBelowThreshold(Double threshold) throws CoreException {
    File file = new File("/");
    Double usableRatio = file.getUsableSpace() / (double) file.getTotalSpace();
    if (usableRatio > threshold) return;

    throw new CoreException(String.format("Local machine has only %s free disk space!",
      new DecimalFormat("##.##%").format(usableRatio)));
  }

  /**
   Method handling HTTP GET requests. The returned object will be sent
   to the client as "text/plain" media type.

   @return String that will be returned as a text/plain response.
   */
  @GET
  @Context
  @PermitAll
  public Response healthcheck() {
    try {
      throwExceptionIfRedisDatabaseCannotBePinged();
      throwExceptionIfLocalMachineDiskSpaceBelowThreshold(config.getDouble("alarm.diskFreeRatioLower"));
      throwExceptionIfSQLDatabaseCannotCompleteTransaction();
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   @throws CoreException if SQL database cannot complete a transaction
   */
  private void throwExceptionIfSQLDatabaseCannotCompleteTransaction() throws CoreException {
    try (Connection connection = sqlDatabaseProvider.getDataSource().getConnection()) {
      connection.getClientInfo();
    } catch (SQLException e) {
      throw new CoreException("Failed to connect to SQL database", e);
    }
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
