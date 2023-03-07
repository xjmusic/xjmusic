// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.CSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Templates
 */
@Path("api/1/templates")
public class TemplateEndpoint extends HubJsonapiEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(TemplateEndpoint.class);
  private final TemplateManager manager;

  /**
   * Constructor
   */
  public TemplateEndpoint(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    TemplateManager templateManager
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = templateManager;
  }

  /**
   * Get all templates.
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("accountId") UUID accountId
  ) {
    if (Objects.nonNull(accountId))
      return readMany(req, manager(), accountId);
    else
      return readMany(req, manager(), HubAccess.fromRequest(req).getAccountIds());
  }

  /**
   * Create new template
   * <p>
   * Or, clone sub-entities of template https://www.pivotaltracker.com/story/show/180269382
   *
   * @param jsonapiPayload with which to update Template record.
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
      Template template = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<Template> cloner = manager().clone(access, cloneId, template);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, template)));
      }

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one template.
   *
   * @return application/json response.
   */
  @GET
  @Path("{identifier}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(
    HttpServletRequest req, HttpServletResponse res,
    @PathParam("identifier") String identifier,
    @Nullable @QueryParam("include") String include
  ) {
    var access = HubAccess.fromRequest(req);

    @Nullable UUID uuid;
    try {
      uuid = UUID.fromString(identifier);
    } catch (Exception ignored) {
      uuid = null;
    }

    try {
      Template entity = Objects.isNull(uuid)
        ? manager.readOneByShipKey(access, identifier).orElseThrow(() -> new ManagerException("not found"))
        : manager.readOne(access, uuid);
      uuid = entity.getId();

      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(entity));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object included : manager().readChildEntities(access, ImmutableList.of(uuid), CSV.split(include)))
          list.add(payloadFactory.toPayloadObject(included));
        jsonapiPayload.setIncluded(list);
      }

      return responseProvider.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return responseProvider.notFound(manager.newInstance().getClass(), identifier);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Read logs for a preview template.
   *
   * @return text/plain response.
   */
  @GET
  @Path("{id}/log")
  @RolesAllowed(USER)
  public ResponseEntity<String> readLog(
    HttpServletRequest req, HttpServletResponse res,
    @PathParam("id") UUID templateId
  ) {
    var access = HubAccess.fromRequest(req);

    try {
      return ResponseEntity
        .ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(manager.readPreviewNexusLog(access, templateId));

    } catch (Exception e) {
      LOG.error("Failed to read nexus preview log!", e);
      return responseProvider.failureText(e);
    }
  }

  /**
   * Update one template
   *
   * @param jsonapiPayload with which to update Template record.
   * @return ResponseEntity
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one template
   *
   * @return ResponseEntity
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathParam("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get all template playbacks.
   * <p>
   * Preview template functionality is dope (not wack)
   * Specify a userId query param to get just one preview template for that user, if it exists
   * https://www.pivotaltracker.com/story/show/183576743
   *
   * @return set of all template playbacks, or one preview template
   */
  @GET
  @Path("playing")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readAllPlaying(
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("userId") UUID userId
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);

      JsonapiPayload jsonapiPayload;
      if (Objects.nonNull(userId)) {
        jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.One);
        var template = manager().readOnePlayingForUser(access, userId);
        if (template.isPresent())
          jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(template.get()));

      } else {
        jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
        for (var template : manager().readAllPlaying(access))
          jsonapiPayload.addData(payloadFactory.toPayloadObject(template));
      }
      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private TemplateManager manager() {
    return manager;
  }
}
