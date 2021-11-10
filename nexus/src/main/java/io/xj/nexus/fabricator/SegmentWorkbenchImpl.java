// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.*;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 The SegmentWorkbench is a delegate to manipulate the segment currently in progress during the fabrication.
 The pattern here is that all child entities of this segment are held in memory only within this delegate
 until the very end of the process, when the trigger is pulled and all the entities are written to the database
 using a special segment Manager method that does them all in one transaction.
 <p>
 SegmentWorkbench.done()
 Called at the end of Segment fabrication.
 Sends added records to segmentManager batch insert method
 */
class SegmentWorkbenchImpl implements SegmentWorkbench {
  private final Chain chain;
  private final SegmentManager segmentManager;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final EntityStore benchStore;
  private Segment segment;
  private Collection<SegmentChord> segmentChords;

  @Inject
  public SegmentWorkbenchImpl(
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    EntityStore entityStore
  ) throws NexusException {
    this.chain = chain;
    this.segment = segment;
    this.segmentManager = segmentManager;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.benchStore = entityStore;

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      entityStore.putAll(segmentManager.readManySubEntities(ImmutableList.of(segment.getId()), true));
    } catch (ManagerFatalException | ManagerPrivilegeException | EntityStoreException e) {
      throw new NexusException("Failed to load Segment for Workbench!", e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentChoiceArrangements() {
    return benchStore.getAll(SegmentChoiceArrangement.class);
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices() {
    return benchStore.getAll(SegmentChoice.class);
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() {
    if (Objects.isNull(segmentChords)) {
      segmentChords = benchStore.getAll(SegmentChord.class)
        .stream()
        .sorted(Comparator.comparing((SegmentChord::getPosition)))
        .collect(Collectors.toList());
    }

    return segmentChords;
  }

  @Override
  public Collection<SegmentChordVoicing> getSegmentChordVoicings() {
    return benchStore.getAll(SegmentChordVoicing.class);
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return benchStore.getAll(SegmentMeme.class);
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages() {
    return benchStore.getAll(SegmentMessage.class);
  }

  @Override
  public Collection<SegmentMetadata> getSegmentMetadatas() {
    return benchStore.getAll(SegmentMetadata.class);
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks() {
    return benchStore.getAll(SegmentChoiceArrangementPick.class);
  }

  @Override
  public Segment getSegment() {
    return segment;
  }

  @Override
  public void setSegment(Segment segment) {
    this.segment = segment;
  }

  @Override
  public void putReport(String key, Object value) {
    report.put(key, value);
  }

  @Override
  public void done() throws NexusException {
    try {
      sendReportToSegmentMessage();

      segmentManager.update(getSegment().getId(), getSegment());

      segmentManager.createAllSubEntities(benchStore.getAll(SegmentMeme.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentMessage.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentMetadata.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChord.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChordVoicing.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoice.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoiceArrangement.class)); // after choices
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoiceArrangementPick.class)); // after arrangements

    } catch (JsonapiException | ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException | ManagerValidationException e) {
      throw new NexusException("Failed to build and update payload for Segment!", e);
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceOfType(ProgramType type) {
    return getSegmentChoices().stream()
      .filter(c -> c.getProgramType().equals(type.toString())).findFirst();
  }

  @Override
  public Collection<SegmentChoice> getChoicesOfType(ProgramType type) {
    return getSegmentChoices().stream()
      .filter(c -> c.getProgramType().equals(type.toString()))
      .collect(Collectors.toList());
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public <N> N add(N entity) throws NexusException {
    try {
      // [#179078453] Segment shouldn't have two of the same meme
      if (SegmentMeme.class.equals(entity.getClass()) && alreadyHasMeme((SegmentMeme) entity)) return entity;
      return benchStore.put(entity);
    } catch (EntityStoreException e) {
      throw new NexusException(e);
    }
  }

  /**
   Returns the current report map as json, and clears the report, so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws JsonapiException, NexusException {
    String reported = jsonapiPayloadFactory.serialize(report);
    var msg = new SegmentMessage();
    msg.setId(UUID.randomUUID());
    msg.setSegmentId(segment.getId());
    msg.setType(SegmentMessageType.DEBUG);
    msg.setBody(reported);
    add(msg);
    report.clear();
  }

  /**
   Whether the workbench already has a meme of this name

   @param meme to test for existence
   @return true if a meme already exists with this name
   */
  private boolean alreadyHasMeme(SegmentMeme meme) {
    var name = Text.toMeme(meme.getName());
    return getSegmentMemes().stream().anyMatch(existing -> existing.getName().equals(name));
  }
}
