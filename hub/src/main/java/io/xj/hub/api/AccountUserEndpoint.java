// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.dao.AccountUserDAO;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Account record
 */
@Path("api/1/account-users")
public class AccountUserEndpoint extends HubJsonapiEndpoint<AccountUser> {
  private final AccountUserDAO dao;

  /**
   Constructor
   */
  @Inject
  public AccountUserEndpoint(
    AccountUserDAO dao,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.dao = dao;
  }

  /**
   Get Users in one account.

   @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    return readMany(crc, dao(), accountId);
  }

  /**
   Create new account user

   @param jsonapiPayload with which to of Account User
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ADMIN)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one AccountUser by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Delete one AccountUser by accountId and userId

   @return application/json response.
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
  private AccountUserDAO dao() {
    return dao;
  }

}
