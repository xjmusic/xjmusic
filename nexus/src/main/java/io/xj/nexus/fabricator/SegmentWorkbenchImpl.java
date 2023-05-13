// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMessageType;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.SegmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class SegmentWorkbenchImpl implements SegmentWorkbench {
  private final Logger LOG = LoggerFactory.getLogger(SegmentWorkbenchImpl.class);
  private final Chain chain;
  private final SegmentManager segmentManager;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final EntityStore benchStore;
  private Segment segment;
  private List<SegmentChord> segmentChords;

  public SegmentWorkbenchImpl(
    Chain chain,
    Segment segment,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) throws NexusException {
    this.chain = chain;
    this.segment = segment;
    this.segmentManager = segmentManager;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.benchStore = new EntityStoreImpl();

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      benchStore.putAll(segmentManager.readManySubEntities(ImmutableList.of(segment.getId()), true));
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
  public List<SegmentChord> getSegmentChords() {
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
  public Collection<SegmentMeta> getSegmentMetas() {
    return benchStore.getAll(SegmentMeta.class);
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
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentMeta.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChord.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChordVoicing.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoice.class));
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoiceArrangement.class)); // after choices
      segmentManager.createAllSubEntities(benchStore.getAll(SegmentChoiceArrangementPick.class)); // after arrangements

    } catch (JsonapiException | ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException |
             ManagerValidationException e) {
      throw new NexusException("Failed to build and update payload for Segment!", e);
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceOfType(ProgramType type) {
    return getSegmentChoices().stream()
      .filter(c -> type.toString().equals(c.getProgramType()))
      .findFirst();
  }

  @Override
  public Collection<SegmentChoice> getChoicesOfType(ProgramType type) {
    return getSegmentChoices().stream()
      .filter(c -> type.toString().equals(c.getProgramType()))
      .collect(Collectors.toList());
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    try {
      // Segment shouldn't have two of the same meme https://www.pivotaltracker.com/story/show/179078453
      if (entity instanceof SegmentMeme && alreadyHasMeme((SegmentMeme) entity)) return entity;

        // Segment meta overwrites existing meta with same key https://www.pivotaltracker.com/story/show/183135787
      else if (entity instanceof SegmentMeta) destroyExistingMeta(((SegmentMeta) entity).getKey());

      return benchStore.put(entity);
    } catch (EntityStoreException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public <N> void delete(N entity) {
    try {
      benchStore.delete(entity.getClass(), Entities.getId(entity));
    } catch (EntityException e) {
      LOG.error("Failed to delete {}", entity);
    }
  }

  @Override
  public Optional<SegmentMeta> getSegmentMeta(String key) {
    return benchStore.getAll(SegmentMeta.class)
      .stream().filter(meta -> Objects.equals(key, meta.getKey()))
      .findAny();
  }

  /**
   * Returns the current report map as json, and clears the report, so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws JsonapiException, NexusException {
    String reported = jsonapiPayloadFactory.serialize(report);
    var msg = new SegmentMessage();
    msg.setId(UUID.randomUUID());
    msg.setSegmentId(segment.getId());
    msg.setType(SegmentMessageType.DEBUG);
    msg.setBody(reported);
    this.put(msg);
    report.clear();
  }

  /**
   * Whether the workbench already has a meme of this name
   *
   * @param meme to test for existence
   * @return true if a meme already exists with this name
   */
  private boolean alreadyHasMeme(SegmentMeme meme) {
    var name = Text.toMeme(meme.getName());
    return getSegmentMemes().stream().anyMatch(existing -> existing.getName().equals(name));
  }

  /**
   * Segment meta overwrites existing meta with same key https://www.pivotaltracker.com/story/show/183135787
   *
   * @param key for which to erase all metas
   */
  private void destroyExistingMeta(String key) {
    getSegmentMetas().stream().filter(meta -> Objects.equals(key, meta.getKey())).forEach(this::delete);
  }
}
