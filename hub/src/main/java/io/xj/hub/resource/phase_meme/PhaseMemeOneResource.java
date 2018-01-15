// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase_meme;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.transport.JSON;



import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Phase record
 */
@Path("phase-memes/{id}")
public class PhaseMemeOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseMemeDAO phaseMemeDAO = injector.getInstance(PhaseMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one PhaseMeme by phaseId and memeId

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        PhaseMeme.KEY_ONE,
        phaseMemeDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return Response.serverError().header("error", e.getMessage()).build();
    }
  }

  /**
   Delete one PhaseMeme by id

   @return application/json response.
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      phaseMemeDAO.destroy(Access.fromContext(crc), new BigInteger(id));
    } catch (BusinessException e) {
      return Response
        .status(HttpStatus.SC_BAD_REQUEST)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      return Response.serverError().header("error", e.getMessage()).build();
    }

    return Response.accepted("{}").build();
  }

}
