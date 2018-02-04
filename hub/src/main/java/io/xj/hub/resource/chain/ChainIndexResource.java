// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
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
import java.util.Objects;

/**
 Chains
 */
@Path("chains")
public class ChainIndexResource extends HubResource {
  private final ChainDAO chainDAO = injector.getInstance(ChainDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get all chains.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(accountId) || accountId.isEmpty()) {
      return response.notAcceptable("Account id is required");
    }

    try {
      return response.readMany(
        Chain.KEY_MANY,
        chainDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(accountId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new chain

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(ChainWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Chain.KEY_MANY,
        Chain.KEY_ONE,
        chainDAO.create(
          Access.fromContext(crc),
          data.getChain()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
