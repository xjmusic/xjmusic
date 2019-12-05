// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource;

import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.app.AppResource;
import io.xj.core.dao.ChainDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

/**
 Chains
 */
@Path("chains")
public class ChainResource extends AppResource {
  private ChainDAO dao;


  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ChainResource(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ChainDAO.class);
  }


  /**
   Get all chains.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    return readMany(crc, dao(), accountId);
  }

  /**
   Create new chain
   -or-
   [#160299309] Engineer wants a *revived* action for a live production chain

   @param payload with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("reviveId") String reviveId) {
    try {
      Chain chain;

      // test if we will revive a prior chain
      if (!Objects.isNull(reviveId) && !reviveId.isEmpty())
        chain = dao().revive(Access.fromContext(crc), UUID.fromString(reviveId));
      else
        chain = dao().create(Access.fromContext(crc), dao().newInstance().consume(payload));

      Payload result = new Payload();
      result.setDataOne(chain.toPayloadObject());
      return response.create(result);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get one chain.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @PermitAll
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    Access access = Access.fromContext(crc);

    if (Objects.isNull(id) || id.isEmpty())
      return response.notAcceptable("Chain id is required");

    try {

      // will only have value if this can parse a uuid from string
      // otherwise, ignore the exception on attempt and store a null value for uuid
      UUID uuidId;
      try {
        uuidId = UUID.fromString(id);
      } catch (Exception ignored) {
        uuidId = null;
      }

      // chain is either by uuid or embed key
      Chain chain;
      if (Objects.nonNull(uuidId))
        chain = dao().readOne(access, uuidId); // uuid
      else
        chain = dao().readOne(access, id); // embed key

      Payload payload = new Payload();
      payload.setDataEntity(chain);
      return response.ok(payload);

    } catch (CoreException ignored) {
      return response.notFound("chains", id); // either embed key or uuid

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one chain

   @param payload with which to update Chain record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one chain
   <p>
   [#294] Eraseworker finds Segments and Audio in deleted state and actually deletes the records, child entities and S3 objects
   Hub DELETE /chains/# is actually a state update to ERASE
   Hub cannot invoke chain destroy chainDAO method!

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(UserRoleType.ARTIST)
  public Response erase(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    try {
      dao().erase(Access.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ChainDAO dao() {
    return dao;
  }
}
