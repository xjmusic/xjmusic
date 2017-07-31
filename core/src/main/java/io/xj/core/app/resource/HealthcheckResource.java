// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.resource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.app.exception.DatabaseException;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.db.RedisDatabaseProvider;
import io.xj.core.db.sql.SQLDatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Root resource (exposed at "o2" path)
 */
@Path("o2")
public class HealthcheckResource {

  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(HealthcheckResource.class);
  private final RedisDatabaseProvider redisDatabaseProvider = injector.getInstance(RedisDatabaseProvider.class);
  private final SQLDatabaseProvider dbProvider = injector.getInstance(SQLDatabaseProvider.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  private static final String PONG = "PONG";

  /**
   Method handling HTTP getContent requests. The returned object will be sent
   to the client as "text/plain" media type.

   @return String that will be returned as a text/plain response.
   */
  @GET
  @Context
  @PermitAll
  public Response healthcheck() {
    try {
      String pingResult = redisDatabaseProvider.getClient().ping();
      if (!PONG.equals(pingResult)) {
        throw new DatabaseException("Redis server ping result: " + pingResult);
      }
    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
    try {
      dbProvider.getConnection().success();
    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
    return Response
      .accepted()
      .status(Response.Status.OK)
      .build();
  }
}
