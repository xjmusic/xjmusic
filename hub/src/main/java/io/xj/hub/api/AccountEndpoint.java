// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.HubEndpoint;
import io.xj.hub.dao.AccountDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Accounts
 */
@Path("api/1/accounts")
public class AccountEndpoint extends HubEndpoint {
  private final AccountDAO dao;

  /**
   Constructor
   */
  @Inject
  public AccountEndpoint(
    AccountDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all accounts.

   @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc) {
    return readMany(crc, dao(), ImmutableList.of());
  }

  /**
   Create new account

   @param jsonapiPayload comprising new entities
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ADMIN)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one account.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one account

   @param jsonapiPayload comprising updated entities
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ADMIN)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one account

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ADMIN)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private AccountDAO dao() {
    return dao;
  }
}
