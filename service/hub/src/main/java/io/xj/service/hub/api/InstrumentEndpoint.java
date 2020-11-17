// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Instrument;
import io.xj.lib.entity.Entities;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.InstrumentMemeDAO;

import javax.annotation.security.RolesAllowed;
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
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 Instruments
 */
@Path("instruments")
public class InstrumentEndpoint extends HubEndpoint {
  private final InstrumentDAO dao;
  private final InstrumentMemeDAO instrumentMemeDAO;

  /**
   Constructor
   */
  @Inject
  public InstrumentEndpoint(
    InstrumentDAO dao,
    InstrumentMemeDAO instrumentMemeDAO,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
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
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      Collection<Instrument> instruments;

      // how we source instruments depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        instruments = dao().readMany(hubAccess, ImmutableList.of(libraryId));
      else if (null != accountId && !accountId.isEmpty())
        instruments = dao().readManyInAccount(hubAccess, accountId);
      else
        instruments = dao().readMany(hubAccess);

      // add instruments as plural data in payload
      for (Instrument instrument : instruments) payload.addData(payloadFactory.toPayloadObject(instrument));
      Set<String> instrumentIds = Entities.idsOf(instruments);

      // if detailed, seek and add events to payload
      if (Objects.nonNull(detailed) && detailed)
        payload.addAllToIncluded(payloadFactory.toPayloadObjects(
          instrumentMemeDAO.readMany(hubAccess, instrumentIds)));

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
  @RolesAllowed(ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {

    try {
      Instrument instrument = payloadFactory.consume(dao().newInstance(), payload);
      Instrument created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(
          HubAccess.fromContext(crc),
          cloneId,
          instrument);
      else
        created = dao().create(
          HubAccess.fromContext(crc),
          instrument);

      return response.create(new Payload().setDataOne(payloadFactory.toPayloadObject(created)));

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

   @param payload with which to update Instrument record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ADMIN)
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
