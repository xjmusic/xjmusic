// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence_pattern_meme;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.GsonProvider;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;
import org.apache.http.HttpStatus;

import javax.annotation.security.RolesAllowed;
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
 Pattern record
 */
@Path("sequence-pattern-memes/{id}")
public class SequencePatternMemeOneResource extends HubResource {
  private final SequencePatternMemeDAO sequencePatternMemeDAO = injector.getInstance(SequencePatternMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one SequencePatternMeme by sequencePatternId and memeId

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        SequencePatternMeme.KEY_ONE,
        sequencePatternMemeDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Sequence-Pattern Bound Meme");

    } catch (Exception e) {
      return Response.serverError().header("error", e.getMessage()).build();
    }
  }

  /**
   Delete one SequencePatternMeme by id

   @return application/json response.
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      sequencePatternMemeDAO.destroy(Access.fromContext(crc), new BigInteger(id));
    } catch (CoreException e) {
      return Response
        .status(HttpStatus.SC_BAD_REQUEST)
        .entity(gsonProvider.wrapError(e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError().header("error", e.getMessage()).build();
    }

    return Response.accepted("{}").build();
  }

}
