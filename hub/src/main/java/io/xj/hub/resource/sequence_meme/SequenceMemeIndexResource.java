// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence_meme;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.sequence_meme.SequenceMemeWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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

/**
 Sequence record
 */
@Path("sequence-memes")
public class SequenceMemeIndexResource extends HubResource {
  private final SequenceMemeDAO sequenceMemeDAO = injector.getInstance(SequenceMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("sequenceId")
  String sequenceId;

  /**
   Get Memes in one sequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (null == sequenceId || sequenceId.isEmpty()) {
      return response.notAcceptable("Sequence id is required");
    }

    try {
      return response.readMany(
        SequenceMeme.KEY_MANY,
        sequenceMemeDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(sequenceId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new sequence meme

   @param data with which to update Sequence record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(SequenceMemeWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        SequenceMeme.KEY_MANY,
        SequenceMeme.KEY_ONE,
        sequenceMemeDAO.create(
          Access.fromContext(crc),
          data.getSequenceMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
