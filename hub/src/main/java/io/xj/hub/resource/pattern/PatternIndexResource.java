// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternWrapper;
import io.xj.core.model.role.Role;
import io.xj.core.server.HttpResponseProvider;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Patterns
 */
@Path("patterns")
public class PatternIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PatternDAO patternDAO = injector.getInstance(PatternDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  /**
   Get all patterns.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(Role.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (null != libraryId && !libraryId.isEmpty()) {
      return readAllInLibrary(Access.fromContext(crc));
    } else if (null != accountId && !accountId.isEmpty()) {
      return readAllInAccount(Access.fromContext(crc));
    } else {
      return response.notAcceptable("Either Account or Library id is required");
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Pattern.KEY_MANY,
        patternDAO.readAllInAccount(
          access,
          ULong.valueOf(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Pattern.KEY_MANY,
        patternDAO.readAllInLibrary(
          access,
          ULong.valueOf(libraryId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new pattern

   @param data with which to update Pattern record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(Role.ARTIST)
  public Response create(PatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Pattern.KEY_MANY,
        Pattern.KEY_ONE,
        patternDAO.create(
          Access.fromContext(crc),
          data.getPattern()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
