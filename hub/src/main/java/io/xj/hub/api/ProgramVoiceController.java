// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.manager.ProgramVoiceManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
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
import java.util.UUID;

/**
 * ProgramVoice endpoint
 */
@RestController
@RequestMapping("/api/1/program-voices")
public class ProgramVoiceController extends HubJsonapiEndpoint {
  private final ProgramVoiceManager manager;
  private final ProgramSequenceChordVoicingManager voicingManager;

  /**
   * Constructor
   */
  public ProgramVoiceController(
    ProgramVoiceManager manager,
    ProgramSequenceChordVoicingManager voicingManager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.voicingManager = voicingManager;
  }

  /**
   * Create new programVoice binding
   *
   * @param jsonapiPayload with which to of ProgramVoice Binding
   * @return ResponseEntity
   */
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      JsonapiPayload responseData = new JsonapiPayload();

      ProgramVoice created = manager.create(access, payloadFactory.consume(manager.newInstance(), jsonapiPayload));
      responseData.setDataOne(payloadFactory.toPayloadObject(created));
      responseData.addAllToIncluded(payloadFactory.toPayloadObjects(voicingManager.createEmptyVoicings(access, created)));
      return responseProvider.create(responseData);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one ProgramVoice by id
   *
   * @return application/json response.
   */
  @GetMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Get many program voices
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @RequestParam("programId") UUID programId) {
    return readMany(req, manager(), programId);
  }

  /**
   * Update one ProgramVoice
   *
   * @param jsonapiPayload with which to update record.
   * @return ResponseEntity
   */
  @PatchMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one ProgramVoice by programVoiceId and bindingId
   *
   * @return application/json response.
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
  private ProgramVoiceManager manager() {
    return manager;
  }

}
