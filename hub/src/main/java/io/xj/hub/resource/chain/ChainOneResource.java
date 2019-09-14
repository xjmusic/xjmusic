// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.payload.MediaType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.util.Value;
import io.xj.hub.HubResource;

import javax.annotation.security.PermitAll;
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
import java.math.BigInteger;
import java.util.Objects;

/**
 Chain record
 */
@Path("chains/{id}")
public class ChainOneResource extends HubResource {

  @PathParam("id")
  String id;

  /**
   Get one chain.

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response readOne(@Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);

    if (Objects.isNull(id) || id.isEmpty())
      return response.notAcceptable("Chain id is required");

    try {
      Chain chain = Value.isInteger(id) ?
        dao().readOne(access, new BigInteger(id)) :
        dao().readOne(access, id);

      Payload payload = new Payload();
      payload.setDataEntity(chain);
      return response.ok(payload);

    } catch (CoreException ignored) {
      return response.notFound(dao().newInstance().setId(new BigInteger(id)));

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
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc) {
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
  @RolesAllowed(UserRoleType.ARTIST)
  public Response erase(@Context ContainerRequestContext crc) {
    try {
      dao().erase(Access.fromContext(crc), new BigInteger(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private ChainDAO dao() {
    return injector.getInstance(ChainDAO.class);
  }
}
