// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.*;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Program;
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
 * Programs
 */
@Path("api/1/programs")
public class ProgramEndpoint extends HubJsonapiEndpoint {
  private final ProgramSequenceBindingMemeManager programSequenceBindingMemeManager;
  private final ProgramManager manager;
  private final ProgramMemeManager programMemeManager;

  /**
   * Constructor
   */
  public ProgramEndpoint(
    ProgramManager manager,
    ProgramSequenceBindingMemeManager programSequenceBindingMemeManager,
    ProgramMemeManager programMemeManager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.programSequenceBindingMemeManager = programSequenceBindingMemeManager;
    this.programMemeManager = programMemeManager;
  }

  /**
   * Get all programs.
   *
   * @param accountId to get programs for
   * @param libraryId to get programs for
   * @param detailed  whether to include memes and bindings
   * @return set of all programs
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
      Collection<Program> programs;

      // how we source programs depends on the query parameters
      if (Objects.nonNull(libraryId))
        programs = manager().readMany(access, ImmutableList.of(libraryId));
      else if (Objects.nonNull(accountId))
        programs = manager().readManyInAccount(access, accountId);
      else
        programs = manager().readMany(access);

      // add programs as plural data in payload
      for (Program program : programs) jsonapiPayload.addData(payloadFactory.toPayloadObject(program));
      Set<UUID> programIds = Entities.idsOf(programs);

      // if detailed, add Program Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programMemeManager.readMany(access, programIds)));

      // if detailed, add Program Sequence Binding Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programSequenceBindingMemeManager.readMany(access, programIds)));

      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Create new program, potentially cloning an existing program
   *
   * @param jsonapiPayload with which to update Program record.
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    JsonapiPayload jsonapiPayload,
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {

    try {
      HubAccess access = HubAccess.fromRequest(req);
      Program program = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<Program> cloner = manager().clone(access, cloneId, program);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, program)));
      }

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one program with included child entities
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
        jsonapiPayload.setIncluded(
          list);
      }

      return responseProvider.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return responseProvider.notFound(manager.newInstance().getClass(), id);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Update one program
   *
   * @param jsonapiPayload with which to update Program record.
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
   * Delete one program
   *
   * @return ResponseEntity
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> destroy(HttpServletRequest req, HttpServletResponse res, @PathParam("id") UUID id) {
    try {
      manager().destroy(HubAccess.fromRequest(req), id);
      return responseProvider.deletedOk();

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private ProgramManager manager() {
    return manager;
  }
}
