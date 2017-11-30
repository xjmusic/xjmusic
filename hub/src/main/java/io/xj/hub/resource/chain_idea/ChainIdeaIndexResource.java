// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.chain_idea;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChainIdeaDAO;
import io.xj.core.model.chain_idea.ChainIdea;
import io.xj.core.model.chain_idea.ChainIdeaWrapper;
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
 Chain Idea record
 */
@Path("chain-ideas")
public class ChainIdeaIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainIdeaDAO DAO = injector.getInstance(ChainIdeaDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("chainId")
  String chainId;

  /**
   Get Ideas in one chain.

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
        ChainIdea.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(chainId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new chain idea

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST,Role.ENGINEER,Role.ADMIN})
  public Response create(ChainIdeaWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        ChainIdea.KEY_MANY,
        ChainIdea.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getChainIdea()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
