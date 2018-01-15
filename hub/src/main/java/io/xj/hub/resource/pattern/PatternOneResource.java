// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;



import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Pattern record
 */
@Path("patterns/{id}")
public class PatternOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PatternDAO patternDAO = injector.getInstance(PatternDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one pattern.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Pattern.KEY_ONE,
        patternDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one pattern

   @param data with which to update Pattern record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(PatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      patternDAO.update(Access.fromContext(crc), new BigInteger(id), data.getPattern());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one pattern

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ADMIN)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      patternDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
