// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence_meme;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
 Sequence record
 */
@Path("sequence-memes/{id}")
public class SequenceMemeOneResource extends HubResource {
  private final SequenceMemeDAO sequenceMemeDAO = injector.getInstance(SequenceMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one SequenceMeme by sequenceId and memeId

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        SequenceMeme.KEY_ONE,
        sequenceMemeDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Sequence Meme");

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one SequenceMeme by sequenceId and memeId

   @return application/json response.
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      sequenceMemeDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
