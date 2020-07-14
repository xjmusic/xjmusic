// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.inject.Injector;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.dao.ChainBindingDAO;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Chain record
 */
@Path("chain-bindings")
public class ChainBindingEndpoint extends NexusEndpoint {
  private ChainBindingDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ChainBindingEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ChainBindingDAO.class);
  }

  /**
   Get Bindings in one chain.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("chainId") String chainId) {
    return readMany(crc, dao(), chainId);
  }

  /**
   Create new chain binding

   @param payload with which to of Chain Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one ChainBinding by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Delete one ChainBinding by chainId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ChainBindingDAO dao() {
    return dao;
  }

}
