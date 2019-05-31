// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
 Sequence record
 */
@Path("sequences/{id}")
public class SequenceOneResource extends HubResource {
  private final SequenceDAO sequenceDAO = injector.getInstance(SequenceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one sequence.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Sequence.KEY_ONE,
        sequenceDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Sequence");

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one sequence

   @param data with which to update Sequence record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(SequenceWrapper data, @Context ContainerRequestContext crc) {
    try {
      sequenceDAO.update(Access.fromContext(crc), new BigInteger(id), data.getSequence());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one sequence

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ADMIN)
  public Response erase(@Context ContainerRequestContext crc) {
    try {
      sequenceDAO.erase(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
