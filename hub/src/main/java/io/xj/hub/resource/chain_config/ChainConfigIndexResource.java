// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain_config;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigWrapper;



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
import java.math.BigInteger;
import java.util.Objects;

/**
 Chain record
 */
@Path("chain-configs")
public class ChainConfigIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainConfigDAO chainConfigDAO = injector.getInstance(ChainConfigDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("chainId")
  String chainId;

  /**
   Get Configs in one chain.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(chainId) || chainId.isEmpty()) {
      return response.notAcceptable("Chain id is required");
    }

    try {
      return response.readMany(
        ChainConfig.KEY_MANY,
        chainConfigDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(chainId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new chain config

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({UserRoleType.ENGINEER, UserRoleType.ADMIN})
  public Response create(ChainConfigWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        ChainConfig.KEY_MANY,
        ChainConfig.KEY_ONE,
        chainConfigDAO.create(
          Access.fromContext(crc),
          data.getChainConfig()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
