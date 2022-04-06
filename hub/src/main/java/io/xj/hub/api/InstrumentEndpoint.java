// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.InstrumentMemeManager;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.util.CSV;

import javax.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    @Nullable @QueryParam("accountId") UUID accountId,
    @Nullable @QueryParam("libraryId") UUID libraryId,
    @Nullable @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<Instrument> instruments;

      // how we source instruments depends on the query parameters
      if (Objects.nonNull(libraryId))
        instruments = manager().readMany(access, ImmutableList.of(libraryId));
      else if (Objects.nonNull(accountId))
        instruments = manager().readManyInAccount(access, accountId);
      else
        instruments = manager().readMany(access);

      // add instruments as plural data in payload
      for (Instrument instrument : instruments) jsonapiPayload.addData(payloadFactory.toPayloadObject(instrument));
      Set<UUID> instrumentIds = Entities.idsOf(instruments);

      // if detailed, add Instrument Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          instrumentMemeManager.readMany(access, instrumentIds)));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument, potentially cloning an existing instrument

   @param jsonapiPayload with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(
    @Context ContainerRequestContext crc,
    JsonapiPayload jsonapiPayload,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      Instrument instrument = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<Instrument> cloner = manager().clone(access, cloneId, instrument);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, instrument)));
      }

      return response.create(responseJsonapiPayload);

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
  public Response readOne(
    @Context ContainerRequestContext crc,
    @PathParam("id") UUID id,
    @Nullable @QueryParam("include") String include
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);

      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(manager().readOne(access, id)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : manager().readChildEntities(access, ImmutableList.of(id), CSV.split(include))) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        jsonapiPayload.setIncluded(list);
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
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
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
