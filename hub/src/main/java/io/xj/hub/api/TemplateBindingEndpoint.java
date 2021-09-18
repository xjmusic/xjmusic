// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.TemplateBindingDAO;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

/**
 TemplateBindings
 */
@Path("api/1/template-bindings")
public class TemplateBindingEndpoint extends HubJsonapiEndpoint {
  private final TemplateBindingDAO dao;

  /**
   Constructor
   */
  @Inject
  public TemplateBindingEndpoint(
    TemplateBindingDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all templateBindings.

   @param accountId  to get templateBindings for
   @param templateId to get templateBindings for
   @param detailed   whether to include memes
   @return set of all templateBindings
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("templateId") String templateId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplateBinding> templateBindings;

      // how we source templateBindings depends on the query parameters
      templateBindings = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(templateId)));

      // add templateBindings as plural data in payload
      for (TemplateBinding templateBinding : templateBindings)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templateBinding));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new templateBinding

   @param jsonapiPayload with which to update TemplateBinding record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {

    try {
      TemplateBinding templateBinding = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
      TemplateBinding created;
      created = dao().create(
        HubAccess.fromContext(crc),
        templateBinding);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }


  /**
   Get one templateBinding.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one templateBinding

   @param jsonapiPayload with which to update TemplateBinding record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one templateBinding

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private TemplateBindingDAO dao() {
    return dao;
  }
}
