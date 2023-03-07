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
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * TemplatePlaybacks
 */
@Path("api/1")
public class TemplatePlaybackEndpoint extends HubJsonapiEndpoint {
  private final TemplatePlaybackManager manager;

  /**
   * Constructor
   */
  public TemplatePlaybackEndpoint(
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
  @GET
  @Path("templates/{templateId}/playback")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readManyForTemplate(
    HttpServletRequest req, HttpServletResponse res,
    @PathParam("templateId") String templateId
  ) {
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
  @POST
  @Path("template-playbacks")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req, HttpServletResponse res) {

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
  @GET
  @Path("users/{userId}/playback")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOneForUser(HttpServletRequest req, HttpServletResponse res, @PathParam("userId") String userId) {
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
  @PATCH
  @Path("template-playbacks/{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one templatePlayback
   *
   * @return ResponseEntity
   */
  @DELETE
  @Path("template-playbacks/{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathParam("id") UUID id) {
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
