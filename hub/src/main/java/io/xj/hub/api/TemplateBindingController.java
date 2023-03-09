// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.util.Collection;
import java.util.UUID;

/**
 * TemplateBindings
 */
@Path("api/1/template-bindings")
public class TemplateBindingController extends HubJsonapiEndpoint {
  private final TemplateBindingManager manager;

  /**
   * Constructor
   */
  public TemplateBindingController(
    TemplateBindingManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get all templateBindings.
   *
   * @param accountId  to get templateBindings for
   * @param templateId to get templateBindings for
   * @param detailed   whether to include memes
   * @return set of all templateBindings
   */
  @GET
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req, HttpServletResponse res,
    @QueryParam("accountId") String accountId,
    @QueryParam("templateId") String templateId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplateBinding> templateBindings;

      // how we source templateBindings depends on the query parameters
      templateBindings = manager().readMany(access, ImmutableList.of(UUID.fromString(templateId)));

      // add templateBindings as plural data in payload
      for (TemplateBinding templateBinding : templateBindings)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templateBinding));

      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Create new templateBinding
   *
   * @param jsonapiPayload with which to update TemplateBinding record.
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req, HttpServletResponse res) {

    try {
      TemplateBinding templateBinding = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      TemplateBinding created;
      created = manager().create(
        HubAccess.fromRequest(req),
        templateBinding);

      return responseProvider.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }


  /**
   * Get one templateBinding.
   *
   * @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathParam("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one templateBinding
   *
   * @param jsonapiPayload with which to update TemplateBinding record.
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
   * Delete one templateBinding
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
  private TemplateBindingManager manager() {
    return manager;
  }
}
