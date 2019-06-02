// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain_sequence;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainSequenceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain_sequence.ChainSequence;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
 Chain Sequence record
 */
@Path("chain-sequences/{id}")
public class ChainSequenceRecordResource extends HubResource {
  private final ChainSequenceDAO chainSequenceDAO = injector.getInstance(ChainSequenceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one ChainSequence by id

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        ChainSequence.KEY_ONE,
        chainSequenceDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Chain Sequence");

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one ChainSequence

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER, UserRoleType.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      chainSequenceDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
