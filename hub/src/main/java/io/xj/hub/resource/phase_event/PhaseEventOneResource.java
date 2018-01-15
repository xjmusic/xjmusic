// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase_event;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.dao.PhaseEventDAO;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_event.PhaseEventWrapper;



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
 PhaseEvent record
 */
@Path("phase-events/{id}")
public class PhaseEventOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseEventDAO phaseEventDAO = injector.getInstance(PhaseEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one phaseEvent.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        PhaseEvent.KEY_ONE,
        phaseEventDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one phaseEvent

   @param data with which to update PhaseEvent record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(PhaseEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      phaseEventDAO.update(
        Access.fromContext(crc),
        new BigInteger(id),
        data.getPhaseEvent());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one phaseEvent

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      phaseEventDAO.destroy(
        Access.fromContext(crc),
        new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
