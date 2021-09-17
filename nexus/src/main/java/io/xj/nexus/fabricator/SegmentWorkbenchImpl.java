// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Chain;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentMessageType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 The SegmentWorkbench is a delegate to manipulate the segment currently in progress during the fabrication.
 The pattern here is that all child entities of this segment are held in memory only within this delegate
 until the very end of the process, when the trigger is pulled and all the entities are written to the database
 using a special segment DAO method that does them all in one transaction.
 <p>
 SegmentWorkbench.done()
 Called at the end of Segment fabrication.
 Sends added records to segmentDAO batch insert method
 */
class SegmentWorkbenchImpl implements SegmentWorkbench {
  private final Chain chain;
  private final SegmentDAO segmentDAO;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final HubClientAccess access;
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final EntityStore benchStore;
  private Segment segment;
  private Collection<SegmentChord> segmentChords;

  @Inject
  public SegmentWorkbenchImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment,
    SegmentDAO segmentDAO,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    EntityStore entityStore
  ) throws NexusException {
    this.access = access;
    this.chain = chain;
    this.segment = segment;
    this.segmentDAO = segmentDAO;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.benchStore = entityStore;

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      entityStore.putAll(segmentDAO.readManySubEntities(access, ImmutableList.of(segment.getId()), true));
    } catch (DAOFatalException | DAOPrivilegeException | EntityStoreException e) {
      throw new NexusException("Failed to load Segment for Workbench!", e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements() {
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

      segmentDAO.update(access, getSegment().getId(), getSegment());

      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentMessage.class));
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentMeme.class));
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentChord.class));
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentChordVoicing.class));
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentChoice.class));
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentChoiceArrangement.class)); // after choices
      segmentDAO.createAllSubEntities(access, benchStore.getAll(SegmentChoiceArrangementPick.class)); // after arrangements

    } catch (JsonapiException | DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
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
