// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain_instrument;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Chain Instrument record
 */
@Path("chain-instruments/{id}")
public class ChainInstrumentOneResource extends HubResource {
  private final ChainInstrumentDAO chainInstrumentDAO = injector.getInstance(ChainInstrumentDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one ChainInstrument by id

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        ChainInstrument.KEY_ONE,
        chainInstrumentDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one ChainInstrument

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER, UserRoleType.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      chainInstrumentDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
