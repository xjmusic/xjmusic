// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.account;

import io.xj.core.dao.AccountDAO;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;
import io.xj.core.model.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Account record
 */
@Path("accounts/{id}")
public class AccountOneResource extends HubResource {

  @PathParam("id")
  String id;

  /**
   Get one account.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one account

   @param payload comprising updated entities
   @return Response
   */
  @PATCH
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ADMIN)
  public Response update(Payload payload, @Context ContainerRequestContext crc) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one account

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ADMIN)
  public Response delete(@Context ContainerRequestContext crc) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private AccountDAO dao() {
    return injector.getInstance(AccountDAO.class);
  }
}
