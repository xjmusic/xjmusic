// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain_pattern;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChainPatternDAO;
import io.xj.core.model.chain_pattern.ChainPattern;
import io.xj.core.model.chain_pattern.ChainPatternWrapper;
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
 Chain Pattern record
 */
@Path("chain-patterns")
public class ChainPatternIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainPatternDAO DAO = injector.getInstance(ChainPatternDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("chainId")
  String chainId;

  /**
   Get Patterns in one chain.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (chainId == null || chainId.length() == 0) {
      return response.notAcceptable("Chain id is required");
    }

    try {
      return response.readMany(
        ChainPattern.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(chainId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new chain pattern

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST,Role.ENGINEER,Role.ADMIN})
  public Response create(ChainPatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        ChainPattern.KEY_MANY,
        ChainPattern.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getChainPattern()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
