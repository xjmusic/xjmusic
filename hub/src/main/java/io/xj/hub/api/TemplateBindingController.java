// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.TemplateBinding;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.UUID;

/**
 * TemplateBindings
 */
@RestController
@RequestMapping("/api/1/template-bindings")
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
   * @param templateId to get templateBindings for
   * @return set of all templateBindings
   */
  @GetMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @RequestParam("templateId") String templateId
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
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req) {

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
  @GetMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one templateBinding
   *
   * @param jsonapiPayload with which to update TemplateBinding record.
   * @return ResponseEntity
   */
  @PatchMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one templateBinding
   *
   * @return ResponseEntity
   */
  @DeleteMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathVariable("id") UUID id) {
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
