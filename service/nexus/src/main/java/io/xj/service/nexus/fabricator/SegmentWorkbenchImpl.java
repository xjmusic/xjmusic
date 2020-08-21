// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.MessageType;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;
import io.xj.service.nexus.entity.SegmentMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
  private final Segment segment;
  private final SegmentDAO segmentDAO;
  private final PayloadFactory payloadFactory;
  private final HubClientAccess access;
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final EntityStore store;

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
    this.store = entityStore;

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      entityStore.putAll(segmentDAO.readManySubEntities(access, ImmutableList.of(segment.getId()), true));
    } catch (DAOFatalException | DAOPrivilegeException | EntityStoreException e) {
      throw new FabricationException("Failed to load Segment for Workbench!", e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements() throws FabricationException {
    try {
      return store.getAll(SegmentChoiceArrangement.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices() throws FabricationException {
    try {
      return store.getAll(SegmentChoice.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() throws FabricationException {
    try {
      return store.getAll(SegmentChord.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() throws FabricationException {
    try {
      return store.getAll(SegmentMeme.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages() throws FabricationException {
    try {
      return store.getAll(SegmentMessage.class);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentPicks() throws FabricationException {
    try {
      return store.getAll(SegmentChoiceArrangementPick.class);
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

      segmentDAO.createAllSubEntities(access, store.getAll(SegmentMessage.class));
      segmentDAO.createAllSubEntities(access, store.getAll(SegmentMeme.class));
      segmentDAO.createAllSubEntities(access, store.getAll(SegmentChord.class));
      segmentDAO.createAllSubEntities(access, store.getAll(SegmentChoice.class));
      segmentDAO.createAllSubEntities(access, store.getAll(SegmentChoiceArrangement.class)); // after choices
      segmentDAO.createAllSubEntities(access, store.getAll(SegmentChoiceArrangementPick.class)); // after arrangements

    } catch (JsonApiException | DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException | EntityStoreException e) {
      throw new FabricationException("Failed to build and update payload for Segment!", e);
    }
  }

  @Override
  public SegmentChoice getChoiceOfType(ProgramType type) throws FabricationException {
    Optional<SegmentChoice> choice = getSegmentChoices().stream().filter(c -> c.getType().equals(type)).findFirst();
    if (choice.isEmpty()) throw new FabricationException(String.format("No %s-type choice in workbench segment", type));
    return choice.get();
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public <N extends Entity> N add(N entity) throws FabricationException {
    try {
      return store.put(entity);
    } catch (EntityStoreException e) {
      throw new FabricationException(e);
    }
  }

  /**
   Returns the current report map as json, and clears the report so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws JsonApiException, FabricationException {
    String reported = payloadFactory.serialize(report);
    add(SegmentMessage.create(segment, MessageType.Info, reported));
    report.clear();
  }

}
