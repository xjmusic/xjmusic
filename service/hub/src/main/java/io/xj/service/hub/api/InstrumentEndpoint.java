// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.InstrumentMemeDAO;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentMeme;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 Instruments
 */
@Path("instruments")
public class InstrumentEndpoint extends HubEndpoint {
  private InstrumentDAO dao;
  private InstrumentMemeDAO instrumentMemeDAO;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public InstrumentEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(InstrumentDAO.class);
    instrumentMemeDAO = injector.getInstance(InstrumentMemeDAO.class);
  }

  /**
   Get all instruments.

   @param accountId to get instruments for
   @param libraryId to get instruments for
   @param include   (optional) "memes" or null
   @return set of all instruments
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("include") String include
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      Collection<Instrument> instruments;

      // how we source instruments depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        instruments = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        instruments = dao().readAllInAccount(hubAccess, UUID.fromString(accountId));
      else
        instruments = dao().readAll(hubAccess);

      // add instruments as plural data in payload
      for (Instrument instrument : instruments) payload.addData(payloadFactory.toPayloadObject(instrument));
      Set<UUID> instrumentIds = Entity.idsOf(instruments);

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("memes"))
        for (InstrumentMeme instrumentMeme : instrumentMemeDAO.readMany(hubAccess, instrumentIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(instrumentMeme));

      return response.ok(payload);

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
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {

    try {
      Instrument instrument = payloadFactory.consume(dao().newInstance(), payload);
      Instrument created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(
          HubAccess.fromContext(crc),
          UUID.fromString(cloneId),
          instrument);
      else
        created = dao().create(
          HubAccess.fromContext(crc),
          instrument);

      return response.create(new Payload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }


  /**
   Get one instrument.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one instrument

   @param payload with which to update Instrument record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one instrument

   @return Response
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
  private InstrumentDAO dao() {
    return dao;
  }
}
