// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.payload.MediaType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.util.Value;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Objects;

/**
 Chains
 */
@Path("chains")
public class ChainIndexResource extends HubResource {

  @QueryParam("accountId")
  public String accountId;

  @QueryParam("reviveId")
  public String reviveId;

  /**
   Get all chains.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) {
    return readMany(crc, dao(), accountId);
  }


  /**
   Create new chain
   -or-
   [#160299309] Engineer wants a *revived* action for a live production chain

   @param payload with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    try {
      Chain chain;

      // test if we will revive a prior chain
      if (!Objects.isNull(reviveId) && !reviveId.isEmpty() && Value.isInteger(reviveId))
        chain = dao().revive(Access.fromContext(crc), new BigInteger(reviveId));
      else
        chain = dao().create(Access.fromContext(crc), dao().newInstance().consume(payload));

      Payload result = new Payload();
      result.setDataOne(chain.toPayloadObject());
      return response.create(result);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private ChainDAO dao() {
    return injector.getInstance(ChainDAO.class);
  }
}
