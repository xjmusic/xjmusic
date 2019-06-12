// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.payload.MediaType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Objects;

/**
 Instruments
 */
@Path("instruments")
// TODO don't necessarily include all sub-entities--- only do so on request, and probably not at all for the index resources
public class InstrumentIndexResource extends HubResource {

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
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) {
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
      return response.ok(
        new Payload().setDataEntities(
          dao().readAllInAccount(access, new BigInteger(accountId)), false));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.ok(
        new Payload().setDataEntities(
          dao().readMany(access, ImmutableList.of(new BigInteger(libraryId))), false));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAll(Access access) {
    try {
      return response.ok(
        new Payload().setDataEntities(
          dao().readAll(access), false));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument

   @param payload with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc) {

    try {
      Instrument instrument = dao().newInstance().consume(payload);
      Instrument created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          instrument);
      else
        created = dao().create(
          Access.fromContext(crc),
          instrument);

      return response.create(new Payload().setDataEntity(created));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private InstrumentDAO dao() {
    return injector.getInstance(InstrumentDAO.class);
  }
}
