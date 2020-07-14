// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.dao.AccountUserDAO;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Account record
 */
@Path("account-users")
public class AccountUserEndpoint extends HubEndpoint {
  private AccountUserDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public AccountUserEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(AccountUserDAO.class);
  }

  /**
   Get Users in one account.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    return readMany(crc, dao(), accountId);
  }

  /**
   Create new account user

   @param payload with which to of Account User
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ADMIN)
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one AccountUser by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Delete one AccountUser by accountId and userId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(UserRoleType.ADMIN)
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
