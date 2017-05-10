// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.phase_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.model.phase_meme.PhaseMeme;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

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

/**
 Phase record
 */
@Path("phase-memes/{id}")
public class PhaseMemeRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseMemeDAO DAO = injector.getInstance(PhaseMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one PhaseMeme by phaseId and memeId

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        PhaseMeme.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  /**
   Delete one PhaseMeme by id

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      DAO.delete(Access.fromContext(crc), ULong.valueOf(id));
    } catch (BusinessException e) {
      return Response
        .status(HttpStatus.SC_BAD_REQUEST)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      return Response.serverError().build();
    }

    return Response.accepted("{}").build();
  }

}
