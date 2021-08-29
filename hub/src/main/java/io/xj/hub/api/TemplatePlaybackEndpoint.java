// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.api.TemplatePlayback;
import io.xj.hub.HubEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.TemplatePlaybackDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.PayloadDataType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
public class TemplatePlaybackEndpoint extends HubEndpoint {
  private final TemplatePlaybackDAO dao;

  /**
   Constructor
   */
  @Inject
  public TemplatePlaybackEndpoint(
    TemplatePlaybackDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all templatePlaybacks.

   @param templateId to get templatePlaybacks for
   @return set of all templatePlaybacks
   */
  @GET
  @Path("templates/{templateId}/playback")
  @RolesAllowed(USER)
  public Response readManyForTemplate(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePlayback> templatePlaybacks;

      // how we source templatePlaybacks depends on the query parameters
      templatePlaybacks = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(templateId)));

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
      TemplatePlayback templatePlayback = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
      TemplatePlayback created;
      created = dao().create(
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
  @RolesAllowed(USER)
  public Response readOneForUser(@Context ContainerRequestContext crc, @PathParam("userId") String userId) {
    try {
      Optional<TemplatePlayback> playback = dao.readOneForUser(HubAccess.fromContext(crc), UUID.fromString(String.valueOf(userId)));
      if (playback.isEmpty())
        return response.noContent();
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(playback.get()));
      return response.ok(jsonapiPayload);

    } catch (DAOException ignored) {
      return response.notFound(dao.newInstance().getClass(), UUID.fromString(String.valueOf(userId)));

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
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one templatePlayback

   @return Response
   */
  @DELETE
  @Path("template-playbacks/{id}")
  @RolesAllowed(ADMIN)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private TemplatePlaybackDAO dao() {
    return dao;
  }
}
