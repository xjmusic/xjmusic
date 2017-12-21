// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;

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
import java.math.BigInteger;
import java.util.Objects;

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

  @QueryParam("cloneId")
  String cloneId;

  /**
   Get all patterns.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.nonNull(libraryId) && !libraryId.isEmpty()) {
      return readAllInLibrary(Access.fromContext(crc));

    } else if (Objects.nonNull(accountId) && !accountId.isEmpty()) {
      return readAllInAccount(Access.fromContext(crc));

    } else {
      return readAll(Access.fromContext(crc));
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Pattern.KEY_MANY,
        patternDAO.readAllInAccount(
          access,
          new BigInteger(accountId)));

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
          new BigInteger(libraryId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAll(Access access) {
    try {
      return response.readMany(
        Pattern.KEY_MANY,
        patternDAO.readAll(
          access));

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
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      Pattern created;
      if (Objects.nonNull(cloneId)) {
        created = patternDAO.clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          data.getPattern());
      } else {
        created = patternDAO.create(
          Access.fromContext(crc),
          data.getPattern());
      }
      return response.create(
        Pattern.KEY_MANY,
        Pattern.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
