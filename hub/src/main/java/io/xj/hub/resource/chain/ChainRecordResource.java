// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.chain;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ChainDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainWrapper;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;

/**
 Chain record
 */
@Path("chains/{id}")
public class ChainRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainDAO DAO = injector.getInstance(ChainDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one chain.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(id) || id.length() == 0)
      return response.notAcceptable("Chain id is required");

    try {
      return response.readOne(
        Chain.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one chain

   @param data with which to update Chain record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response update(ChainWrapper data, @Context ContainerRequestContext crc) {
    try {
      DAO.update(Access.fromContext(crc), ULong.valueOf(id), data.getChain());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one chain
   <p>
   [#294] Eraseworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
   Hub DELETE /chains/<id> is actually a state update to ERASE
   Hub cannot invoke chain destroy DAO method!

   @return Response
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response erase(@Context ContainerRequestContext crc) {
    try {
      DAO.erase(Access.fromContext(crc), ULong.valueOf(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
