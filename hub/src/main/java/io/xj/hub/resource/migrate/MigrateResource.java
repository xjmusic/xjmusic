// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.migrate;

import io.xj.core.access.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.DAORecord;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.util.Text;
import io.xj.hub.HubResource;
import org.jooq.DSLContext;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;

/**
 Accounts
 */
@SuppressWarnings({"Duplicates", "RedundantCast"})
@Path("migrate")
public class MigrateResource extends HubResource {

  /**
   Get all accounts.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ADMIN)
  public Response readAll(@Context ContainerRequestContext crc) {
    try (Connection connection = injector.getInstance(SQLDatabaseProvider.class).getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      Access access = Access.fromContext(crc);

      new ExperimentalMigration(db, access).go();

      return Response.ok()
        .type(MediaType.TEXT_PLAIN)
        .entity(String.join(Config.getLineSeparator(), "Migrated OK."))
        .build();

    } catch (Exception e) {

      return Response.serverError()
        .type(MediaType.TEXT_PLAIN)
        .entity(Text.formatStackTrace(e))
        .build();
    }
  }

}
