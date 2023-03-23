// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplatePlaybackManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.PayloadDataType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * TemplatePlaybacks
 */
@RestController
@RequestMapping("/api/1")
public class TemplatePlaybackController extends HubJsonapiEndpoint {
  private final TemplatePlaybackManager manager;

  /**
   * Constructor
   */
  public TemplatePlaybackController(
    TemplatePlaybackManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get all templatePlaybacks.
   *
   * @param templateId to get templatePlaybacks for
   * @return set of all templatePlaybacks
   */
  @GetMapping("templates/{templateId}/playback")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readManyForTemplate(HttpServletRequest req, @PathVariable("templateId") String templateId) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePlayback> templatePlaybacks;

      // how we source templatePlaybacks depends on the query parameters
      templatePlaybacks = manager().readMany(access, ImmutableList.of(UUID.fromString(templateId)));

      // add templatePlaybacks as plural data in payload
      for (TemplatePlayback templatePlayback : templatePlaybacks)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templatePlayback));

      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Create new templatePlayback
   *
   * @param jsonapiPayload with which to update TemplatePlayback record.
   * @return ResponseEntity
   */
  @PostMapping("template-playbacks")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req) {

    try {
      TemplatePlayback templatePlayback = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      TemplatePlayback created;
      created = manager().create(
        HubAccess.fromRequest(req),
        templatePlayback);

      return responseProvider.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }


  /**
   * Get one templatePlayback.
   *
   * @return application/json response.
   */
  @GetMapping("users/{userId}/playback")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOneForUser(HttpServletRequest req, @PathVariable("userId") String userId) {
    try {
      var access = HubAccess.fromRequest(req);
      Optional<TemplatePlayback> playback = manager.readOneForUser(access, UUID.fromString(String.valueOf(userId)));
      if (playback.isEmpty())
        return responseProvider.notFound(TemplatePlayback.class);
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(playback.get()));
      return responseProvider.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return responseProvider.notFound(manager.newInstance().getClass(), UUID.fromString(String.valueOf(userId)));

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Update one templatePlayback
   *
   * @param jsonapiPayload with which to update TemplatePlayback record.
   * @return ResponseEntity
   */
  @PatchMapping("template-playbacks/{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one templatePlayback
   *
   * @return ResponseEntity
   */
  @DeleteMapping("template-playbacks/{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathVariable("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private TemplatePlaybackManager manager() {
    return manager;
  }
}
