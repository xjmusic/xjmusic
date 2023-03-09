// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.InstrumentMemeManager;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.CSV;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Instruments
 */
@Path("api/1/instruments")
public class InstrumentController extends HubJsonapiEndpoint {
  private final InstrumentManager manager;
  private final InstrumentMemeManager instrumentMemeManager;

  /**
   * Constructor
   */
  public InstrumentController(
    InstrumentManager manager,
    InstrumentMemeManager instrumentMemeManager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.instrumentMemeManager = instrumentMemeManager;
  }

  /**
   * Get all instruments.
   *
   * @param accountId to get instruments for
   * @param libraryId to get instruments for
   * @param detailed  whether to include memes and bindings
   * @return set of all instruments
   */
  @GET
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("accountId") UUID accountId,
    @Nullable @QueryParam("libraryId") UUID libraryId,
    @Nullable @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
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

      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Create new instrument, potentially cloning an existing instrument
   *
   * @param jsonapiPayload with which to update Instrument record.
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    HttpServletRequest req, HttpServletResponse res,
    JsonapiPayload jsonapiPayload,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
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

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one instrument with included child entities
   *
   * @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(
    HttpServletRequest req, HttpServletResponse res,
    @PathParam("id") UUID id,
    @Nullable @QueryParam("include") String include
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);

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

      return responseProvider.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return responseProvider.notFound(manager.newInstance().getClass(), id);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Update one instrument
   *
   * @param jsonapiPayload with which to update Instrument record.
   * @return ResponseEntity
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one instrument
   *
   * @return ResponseEntity
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathParam("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private InstrumentManager manager() {
    return manager;
  }
}
