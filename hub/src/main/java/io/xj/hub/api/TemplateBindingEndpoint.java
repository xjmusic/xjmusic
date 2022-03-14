// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

/**
 TemplateBindings
 */
@Path("api/1/template-bindings")
public class TemplateBindingEndpoint extends HubJsonapiEndpoint<TemplateBinding> {
  private final TemplateBindingManager manager;

  /**
   Constructor
   */
  @Inject
  public TemplateBindingEndpoint(
    TemplateBindingManager manager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
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
      HubAccess access = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplateBinding> templateBindings;

      // how we source templateBindings depends on the query parameters
      templateBindings = manager().readMany(access, ImmutableList.of(UUID.fromString(templateId)));

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
      TemplateBinding templateBinding = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      TemplateBinding created;
      created = manager().create(
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
    return readOne(crc, manager(), id);
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
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one templateBinding

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private TemplateBindingManager manager() {
    return manager;
  }
}
