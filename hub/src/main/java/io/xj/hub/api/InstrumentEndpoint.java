// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.InstrumentMemeManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.CSV;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 Instruments
 */
@Path("api/1/instruments")
public class InstrumentEndpoint extends HubJsonapiEndpoint<Instrument> {
  private final InstrumentManager manager;
  private final InstrumentMemeManager instrumentMemeManager;

  /**
   Constructor
   */
  @Inject
  public InstrumentEndpoint(
    InstrumentManager manager,
    InstrumentMemeManager instrumentMemeManager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.instrumentMemeManager = instrumentMemeManager;
  }

  /**
   Get all instruments.

   @param accountId to get instruments for
   @param libraryId to get instruments for
   @param detailed  whether to include memes and bindings
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
        instruments = manager().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        instruments = manager().readManyInAccount(hubAccess, accountId);
      else
        instruments = manager().readMany(hubAccess);

      // add instruments as plural data in payload
      for (Instrument instrument : instruments) jsonapiPayload.addData(payloadFactory.toPayloadObject(instrument));
      Set<UUID> instrumentIds = Entities.idsOf(instruments);

      // if detailed, add Instrument Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          instrumentMemeManager.readMany(hubAccess, instrumentIds)));

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
      Instrument instrument = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      Instrument created;
      if (Objects.nonNull(cloneId))
        created = manager().clone(
          HubAccess.fromContext(crc),
          UUID.fromString(cloneId),
          instrument);
      else
        created = manager().create(
          HubAccess.fromContext(crc),
          instrument);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }


  /**
   Get one instrument with included child entities

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id, @QueryParam("include") String include) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      var uuid = UUID.fromString(id);

      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(manager().readOne(hubAccess, uuid)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : manager().readChildEntities(hubAccess, ImmutableList.of(uuid), CSV.split(include))) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        jsonapiPayload.setIncluded(
          list);
      }

      return response.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return response.notFound(manager.newInstance().getClass(), id);

    } catch (Exception e) {
      return response.failure(e);
    }
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
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private InstrumentManager manager() {
    return manager;
  }
}
