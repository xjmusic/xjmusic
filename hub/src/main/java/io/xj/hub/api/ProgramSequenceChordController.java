// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.analysis.util.ChordVoicingDeduper;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ProgramSequenceChordManager;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.manager.ProgramVoiceManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.util.CSV;
import org.springframework.http.HttpStatus;
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

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProgramSequenceChord endpoint
 */
@RestController
@RequestMapping("/api/1/program-sequence-chords")
public class ProgramSequenceChordController extends HubJsonapiEndpoint {
  private static final String VOICE_TYPE_KEY = "type";
  private final ProgramVoiceManager voiceManager;
  private final ProgramSequenceChordManager manager;
  private final ProgramSequenceChordVoicingManager voicingManager;

  /**
   * Constructor
   */
  public ProgramSequenceChordController(
    ProgramVoiceManager voiceManager,
    ProgramSequenceChordManager manager,
    ProgramSequenceChordVoicingManager voicingManager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.voiceManager = voiceManager;
    this.manager = manager;
    this.voicingManager = voicingManager;
  }

  /**
   * Create a program sequence chord, optionally cloning voicings of an existing chord
   * <p>
   * Chord Search while composing a main program
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    HttpServletRequest req,
    @RequestBody JsonapiPayload jsonapiPayload,
    @Nullable @RequestParam("cloneId") UUID cloneId,
    @Nullable @RequestParam("voicingTypes") String voicingTypes
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      ProgramSequenceChord entity = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      ProgramSequenceChord created;
      JsonapiPayload responseData = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<ProgramSequenceChord> cloner = manager().clone(access, cloneId, entity, Objects.nonNull(voicingTypes) ? CSV.split(voicingTypes).stream().map(InstrumentType::valueOf).toList() : List.of());
        created = cloner.getClone();
        responseData.setDataOne(payloadFactory.toPayloadObject(created));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object obj : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(obj);
          list.add(jsonapiPayloadObject);
        }
        responseData.setIncluded(list);
      } else {
        created = manager().create(access, entity);
        responseData.setDataOne(payloadFactory.toPayloadObject(created));
      }
      responseData.addAllToIncluded(payloadFactory.toPayloadObjects(voicingManager.createEmptyVoicings(access, created)));

      return responseProvider.create(responseData);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one ProgramSequenceChord by id
   *
   * @return application/json response.
   */
  @GetMapping("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Get Chords in program sequence (by specifying programSequenceId)
   * or
   * Chord Search while composing a main program (by specifying search chord name and libraryId)
   * https://www.pivotaltracker.com/story/show/178921705
   * <p>
   * Chord search results voicings have type attribute
   * https://www.pivotaltracker.com/story/show/182220689
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @Nullable @RequestParam("programSequenceId") UUID programSequenceId, // all-chords-for-sequence mode
    @Nullable @RequestParam("libraryId") UUID libraryId, // chord-search mode
    @Nullable @RequestParam("search") String chordName // chord-search mode
  ) {
    if (Objects.nonNull(programSequenceId) && Objects.nonNull(libraryId))
      return responseProvider.failure(HttpStatus.NOT_ACCEPTABLE, "Must specify either parent programSequenceId or libraryId, not both!");

    if (Objects.nonNull(programSequenceId))
      return readMany(req, manager(), programSequenceId);

    try {
      HubAccess access = HubAccess.fromRequest(req);
      var chords = manager().search(access, libraryId, chordName);
      var voicesById = voiceManager.readMany(access, chords.stream().map(ProgramSequenceChord::getProgramId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(ProgramVoice::getId, (v) -> v));
      var uniqueChordVoicings = new ChordVoicingDeduper(voicesById.values(), chords, voicingManager.readManyForChords(access, chords.stream().map(ProgramSequenceChord::getId).toList()));

      var result = new JsonapiPayload().setDataType(PayloadDataType.Many);
      for (var chord : uniqueChordVoicings.getChords()) result.addData(payloadFactory.toPayloadObject(chord));
      for (var voicing : uniqueChordVoicings.getVoicings())
        result.addToIncluded(payloadFactory.toPayloadObject(voicing)
          .setAttribute(VOICE_TYPE_KEY, voicesById.get(voicing.getProgramVoiceId()).getType()));
      return responseProvider.ok(result);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Update one ProgramSequenceChord
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
   * Delete one ProgramSequenceChord by programSequenceId and chordId
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
  private ProgramSequenceChordManager manager() {
    return manager;
  }

}
