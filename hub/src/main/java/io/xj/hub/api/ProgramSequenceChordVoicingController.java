// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
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
import java.util.UUID;

/**
 * ProgramSequenceChordVoicing endpoint
 */
@RestController
@RequestMapping("/api/1/program-sequence-chord-voicings")
public class ProgramSequenceChordVoicingController extends HubJsonapiEndpoint {
  final ProgramSequenceChordVoicingManager manager;

  /**
   * Constructor
   */
  public ProgramSequenceChordVoicingController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ProgramSequenceChordVoicingManager manager
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Create new programSequence chordVoicing
   *
   * @param jsonapiPayload with which to of ProgramSequence ChordVoicing
   * @return ResponseEntity
   */
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    @RequestBody JsonapiPayload jsonapiPayload,
    HttpServletRequest req
  ) {
    if (PayloadDataType.Many == jsonapiPayload.getDataType())
      return updateMany(req, manager(), jsonapiPayload);
    return create(req, manager(), jsonapiPayload);
  }

  /**
   * Get one ProgramSequenceChordVoicing by id
   *
   * @return application/json response.
   */
  @GetMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(
    HttpServletRequest req,
    @PathVariable("id") UUID id
  ) {
    return readOne(req, manager(), id);
  }

  /**
   * Get ChordVoicings in one programSequence.
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @RequestParam("programSequenceChordId") UUID programSequenceChordId
  ) {
    return readMany(req, manager(), programSequenceChordId);
  }

  /**
   * Update one ProgramSequenceChordVoicing
   *
   * @param jsonapiPayload with which to update record.
   * @return ResponseEntity
   */
  @PatchMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(
    @RequestBody JsonapiPayload jsonapiPayload,
    HttpServletRequest req,
    @PathVariable("id") UUID id
  ) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one ProgramSequenceChordVoicing by programSequenceId and chordVoicingId
   *
   * @return application/json response.
   */
  @DeleteMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(
    HttpServletRequest req,
    @PathVariable("id") UUID id
  ) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  ProgramSequenceChordVoicingManager manager() {
    return manager;
  }

}
