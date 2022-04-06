// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplatePlaybackManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 TemplatePlaybacks
 */
@Path("api/1")
public class TemplatePlaybackEndpoint extends HubJsonapiEndpoint<TemplatePlayback> {
  private final TemplatePlaybackManager manager;

  /**
   Constructor
   */
  @Inject
  public TemplatePlaybackEndpoint(
    TemplatePlaybackManager manager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   Get all templatePlaybacks.

   @param templateId to get templatePlaybacks for
   @return set of all templatePlaybacks
   */
  @GET
  @Path("templates/{templateId}/playback")
  @RolesAllowed(ARTIST)
  public Response readManyForTemplate(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePlayback> templatePlaybacks;

      // how we source templatePlaybacks depends on the query parameters
      templatePlaybacks = manager().readMany(access, ImmutableList.of(UUID.fromString(templateId)));

      // add templatePlaybacks as plural data in payload
      for (TemplatePlayback templatePlayback : templatePlaybacks)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templatePlayback));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new templatePlayback

   @param jsonapiPayload with which to update TemplatePlayback record.
   @return Response
   */
  @POST
  @Path("template-playbacks")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {

    try {
      TemplatePlayback templatePlayback = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      TemplatePlayback created;
      created = manager().create(
        HubAccess.fromContext(crc),
        templatePlayback);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }


  /**
   Get one templatePlayback.

   @return application/json response.
   */
  @GET
  @Path("users/{userId}/playback")
  @RolesAllowed(ARTIST)
  public Response readOneForUser(@Context ContainerRequestContext crc, @PathParam("userId") String userId) {
    try {
      Optional<TemplatePlayback> playback = manager.readOneForUser(HubAccess.fromContext(crc), UUID.fromString(String.valueOf(userId)));
      if (playback.isEmpty())
        return response.noContent();
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(playback.get()));
      return response.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return response.notFound(manager.newInstance().getClass(), UUID.fromString(String.valueOf(userId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one templatePlayback

   @param jsonapiPayload with which to update TemplatePlayback record.
   @return Response
   */
  @PATCH
  @Path("template-playbacks/{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one templatePlayback

   @return Response
   */
  @DELETE
  @Path("template-playbacks/{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private TemplatePlaybackManager manager() {
    return manager;
  }
}
