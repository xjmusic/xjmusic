// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.Instrument;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;
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
import java.util.Objects;
import java.util.UUID;

/**
 Instruments
 */
@Path("instruments")
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
          dao().readAllInAccount(access, UUID.fromString(accountId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.ok(
        new Payload().setDataEntities(
          dao().readMany(access, ImmutableList.of(UUID.fromString(libraryId)))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAll(Access access) {
    try {
      return response.ok(
        new Payload().setDataEntities(
          dao().readAll(access)));

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
          UUID.fromString(cloneId),
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
   Get DAO of injector

   @return DAO
   */
  private InstrumentDAO dao() {
    return injector.getInstance(InstrumentDAO.class);
  }
}
