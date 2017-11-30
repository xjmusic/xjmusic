// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.chain;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChainDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainWrapper;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

/**
 Chains
 */
@Path("chains")
public class ChainIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainDAO DAO = injector.getInstance(ChainDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get all chains.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (accountId == null || accountId.length() == 0) {
      return response.notAcceptable("Account id is required");
    }

    try {
      return response.readMany(
        Chain.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(accountId)));

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
  @RolesAllowed({Role.ARTIST})
  public Response create(ChainWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Chain.KEY_MANY,
        Chain.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getChain()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
