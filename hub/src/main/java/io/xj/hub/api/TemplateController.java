// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.manager.TemplatePlaybackManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.util.CSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Templates
 */
@RestController
@RequestMapping("/api/1/templates")
public class TemplateController extends HubJsonapiEndpoint {
  static final Logger LOG = LoggerFactory.getLogger(TemplateController.class);
  final TemplateManager manager;
  final TemplatePlaybackManager templatePlaybackManager;

  /**
   * Constructor
   */
  public TemplateController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    TemplateManager templateManager,
    TemplatePlaybackManager templatePlaybackManager
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = templateManager;
    this.templatePlaybackManager = templatePlaybackManager;
  }

  /**
   * Get all templates.
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @Nullable @RequestParam("accountId") UUID accountId
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
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    @RequestBody JsonapiPayload jsonapiPayload,
    HttpServletRequest req,
    @Nullable @RequestParam("cloneId") UUID cloneId
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
  @GetMapping("{identifier}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(
    HttpServletRequest req,
    @PathVariable("identifier") String identifier,
    @Nullable @RequestParam("include") String include
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
        for (Object included : manager().readChildEntities(access, List.of(uuid), CSV.split(include)))
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
  @GetMapping("{id}/log")
  @RolesAllowed(USER)
  public ResponseEntity<String> readLog(
    HttpServletRequest req,
    @PathVariable("id") UUID templateId
  ) {
    var access = HubAccess.fromRequest(req);

    try {
      var templatePlayback = templatePlaybackManager.readOneForTemplate(access, templateId);
      return templatePlayback
        .map(playback -> ResponseEntity
          .ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(manager.readPreviewNexusLog(access, playback)))
        .orElseGet(() -> ResponseEntity.notFound().build());

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
  @PatchMapping("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one template
   *
   * @return ResponseEntity
   */
  @DeleteMapping("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathVariable("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get all template playbacks.
   * <p>
   * Preview template functionality is tight (not wack)
   * Specify a userId query param to get just one preview template for that user, if it exists
   * https://www.pivotaltracker.com/story/show/183576743
   *
   * @return set of all template playbacks, or one preview template
   */
  @GetMapping("playing")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readAllPlaying(
    HttpServletRequest req,
    @Nullable @RequestParam("userId") UUID userId
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
  TemplateManager manager() {
    return manager;
  }
}
