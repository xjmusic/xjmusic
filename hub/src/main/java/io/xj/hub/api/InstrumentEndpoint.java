// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.InstrumentDAO;
import io.xj.hub.dao.InstrumentMemeDAO;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
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
@Path("api/1/instruments")
public class InstrumentEndpoint extends HubJsonapiEndpoint<Instrument> {
  private final InstrumentDAO dao;
  private final InstrumentMemeDAO instrumentMemeDAO;

  /**
   Constructor
   */
  @Inject
  public InstrumentEndpoint(
    InstrumentDAO dao,
    InstrumentMemeDAO instrumentMemeDAO,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.dao = dao;
    this.instrumentMemeDAO = instrumentMemeDAO;
  }

  /**
   Get all instruments.

   @param accountId to get instruments for
   @param libraryId to get instruments for
   @param detailed  whether to include memes
   @return set of all instruments
   */
  @GET
  @RolesAllowed(USER)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<Instrument> instruments;

      // how we source instruments depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        instruments = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        instruments = dao().readManyInAccount(hubAccess, accountId);
      else
        instruments = dao().readMany(hubAccess);

      // add instruments as plural data in payload
      for (Instrument instrument : instruments) jsonapiPayload.addData(payloadFactory.toPayloadObject(instrument));
      Set<UUID> instrumentIds = Entities.idsOf(instruments);

      // if detailed, seek and add events to payload
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          instrumentMemeDAO.readMany(hubAccess, instrumentIds)));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument

   @param jsonapiPayload with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {

    try {
      Instrument instrument = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
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

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }


  /**
   Get one instrument.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one instrument

   @param jsonapiPayload with which to update Instrument record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
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
