// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.protobuf.MessageLite;
import io.xj.Chain;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;

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

  private Segment segment;

  private final SegmentDAO segmentDAO;
  private final PayloadFactory payloadFactory;
  private final HubClientAccess access;
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final EntityStore bench;
  private Collection<SegmentChord> segmentChords;

  @Inject
  public SegmentWorkbenchImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment,
    SegmentDAO segmentDAO,
    PayloadFactory payloadFactory,
    EntityStore entityStore
  ) throws FabricationException {
    this.access = access;
    this.chain = chain;
    this.segment = segment;
    this.segmentDAO = segmentDAO;
    this.payloadFactory = payloadFactory;
    this.bench = entityStore;

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      entityStore.putAll(segmentDAO.readManySubEntities(access, ImmutableList.of(segment.getId()), true));
    } catch (DAOFatalException | DAOPrivilegeException | EntityStoreException e) {
      throw new FabricationException("Failed to load Segment for Workbench!", e);
    }
  }

  @Override
  public void setSegment(Segment segment) {
    this.segment = segment;
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements() throws FabricationException {
    try {
      return bench.getAll(SegmentChoiceArrangement.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices() throws FabricationException {
    try {
      return bench.getAll(SegmentChoice.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() throws FabricationException {
    if (Objects.isNull(segmentChords))
      try {
        segmentChords = bench.getAll(SegmentChord.class)
          .stream()
          .sorted(Comparator.comparing((SegmentChord::getPosition)))
          .collect(Collectors.toList());
      } catch (EntityStoreException e) {
        throw new FabricationException(e);
      }

    return segmentChords;
  }


  @Override
  public Collection<SegmentChordVoicing> getSegmentChordVoicings() throws FabricationException {
    try {
      return bench.getAll(SegmentChordVoicing.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() throws FabricationException {
    try {
      return bench.getAll(SegmentMeme.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages() throws FabricationException {
    try {
      return bench.getAll(SegmentMessage.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks() throws FabricationException {
    try {
      return bench.getAll(SegmentChoiceArrangementPick.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Segment getSegment() {
    return segment;
  }

  @Override
  public void putReport(String key, Object value) {
    report.put(key, value);
  }

  @Override
  public void done() throws FabricationException {
    try {
      sendReportToSegmentMessage();

      segmentDAO.update(access, getSegment().getId(), getSegment());

      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentMessage.class));
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentMeme.class));
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentChord.class));
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentChordVoicing.class));
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentChoice.class));
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentChoiceArrangement.class)); // after choices
      segmentDAO.createAllSubEntities(access, bench.getAll(SegmentChoiceArrangementPick.class)); // after arrangements

    } catch (JsonApiException | DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException | EntityStoreException e) {
      throw new FabricationException("Failed to build and update payload for Segment!", e);
    }
  }

  @Override
  public SegmentChoice getChoiceOfType(Program.Type type) throws FabricationException {
    Optional<SegmentChoice> choice = getSegmentChoices().stream().filter(c -> c.getProgramType().equals(type)).findFirst();
    if (choice.isEmpty()) throw new FabricationException(String.format("No %s-type choice in workbench segment", type));
    return choice.get();
  }

  @Override
  public Collection<SegmentChoice> getChoicesOfType(Program.Type type)
    throws FabricationException {
    return getSegmentChoices().stream()
      .filter(c -> c.getProgramType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public <N extends MessageLite> N add(N entity) throws FabricationException {
    try {
      return bench.put(entity);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  /**
   Returns the current report map as json, and clears the report so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws JsonApiException, FabricationException {
    String reported = payloadFactory.serialize(report);
    add(SegmentMessage.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setType(SegmentMessage.Type.Info)
      .setBody(reported)
      .build());
    report.clear();
  }

}
