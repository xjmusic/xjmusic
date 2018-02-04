// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
import java.math.BigInteger;
import java.util.Objects;

/**
 Instruments
 */
@Path("instruments")
public class InstrumentIndexResource extends HubResource {
  private final InstrumentDAO instrumentDAO = injector.getInstance(InstrumentDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  @QueryParam("cloneId")
  String cloneId;

  /**
   Get all instruments.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (null != libraryId && !libraryId.isEmpty()) {
      return readAllInLibrary(Access.fromContext(crc));
    } else if (null != accountId && !accountId.isEmpty()) {
      return readAllInAccount(Access.fromContext(crc));
    } else {
      return readAll(Access.fromContext(crc));
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        instrumentDAO.readAllInAccount(
          access,
          new BigInteger(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        instrumentDAO.readAll(
          access,
          ImmutableList.of(new BigInteger(libraryId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAll(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        instrumentDAO.readAll(
          access));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument

   @param data with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    try {
      Instrument created;
      if (Objects.nonNull(cloneId)) {
        created = instrumentDAO.clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          data.getInstrument());
      } else {
        created = instrumentDAO.create(
          Access.fromContext(crc),
          data.getInstrument());
      }
      return response.create(
        Instrument.KEY_MANY,
        Instrument.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
