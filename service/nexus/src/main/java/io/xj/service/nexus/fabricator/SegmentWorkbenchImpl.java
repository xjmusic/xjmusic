// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityCache;
import io.xj.lib.entity.MessageType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 The SegmentWorkbench is a delegate to manipulate the segment currently in progress during the fabrication.
 The pattern here is that all child entities of this segment are held in memory only within this delegate
 until the very end of the process, when the trigger is pulled and all the entities are written to the database
 using a special segment DAO method that does them all in one transaction.
 <p>
 SegmentWorkbench.done()    Called at the end of Segment fabrication.
 First, turns the report map into a json payload of a new segment message (in the EntityCache)
 Calls each EntityCache writeInserts() methods- which gets only the added records and clears the added-records queue
 Sends added records to segmentDAO batch insert method
 */
class SegmentWorkbenchImpl implements SegmentWorkbench {
  private final Chain chain;
  private final Segment segment;
  private final SegmentDAO segmentDAO;
  private final PayloadFactory payloadFactory;
  private final EntityCache<SegmentChoiceArrangement> segmentArrangements;
  private final EntityCache<SegmentChoice> segmentChoices;
  private final EntityCache<SegmentChord> segmentChords;
  private final EntityCache<SegmentMeme> segmentMemes;
  private final EntityCache<SegmentMessage> segmentMessages;
  private final EntityCache<SegmentChoiceArrangementPick> segmentPicks;
  private final HubClientAccess access;
  private final Map<String, Object> report = Maps.newConcurrentMap();

  @Inject
  public SegmentWorkbenchImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment,
    SegmentDAO segmentDAO,
    PayloadFactory payloadFactory
  ) throws FabricationException {
    this.access = access;
    this.chain = chain;
    this.segment = segment;
    this.segmentDAO = segmentDAO;
    this.payloadFactory = payloadFactory;
    segmentArrangements = new EntityCache<>();
    segmentChoices = new EntityCache<>();
    segmentChords = new EntityCache<>();
    segmentMemes = new EntityCache<>();
    segmentMessages = new EntityCache<>();
    segmentPicks = new EntityCache<>();


    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    try {
      stashAll(segmentDAO.readManySubEntities(access, ImmutableList.of(segment.getId()), true));
    } catch (DAOFatalException | DAOPrivilegeException e) {
      throw new FabricationException("Failed to load Segment for Workbench!", e);
    }

    // flush all inserts, so that the workbench can tell the difference between content that was loaded before work began, and new content that needs to be inserted
    segmentPicks.flushInserts();
    segmentArrangements.flushInserts();
    segmentChoices.flushInserts();
    segmentChords.flushInserts();
    segmentMemes.flushInserts();
  }

  /**
   Create and stash an instance in right class's array

   @param instance to stash
   */
  private <N extends Entity> void stash(N instance) {
    if (instance.getClass().isAssignableFrom(SegmentChoiceArrangementPick.class))
      segmentPicks.add((SegmentChoiceArrangementPick) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChoiceArrangement.class))
      segmentArrangements.add((SegmentChoiceArrangement) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChoice.class))
      segmentChoices.add((SegmentChoice) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChord.class))
      segmentChords.add((SegmentChord) instance);
    else if (instance.getClass().isAssignableFrom(SegmentMeme.class))
      segmentMemes.add((SegmentMeme) instance);
  }

  /**
   Stash all instances

   @param instances to stash
   @param <N>       type of instances
   */
  private <N extends Entity> void stashAll(Collection<N> instances) {
    for (N instance : instances) stash(instance);
  }

  @Override
  public EntityCache<SegmentChoiceArrangement> getSegmentArrangements() {
    return segmentArrangements;
  }

  @Override
  public EntityCache<SegmentChoice> getSegmentChoices() {
    return segmentChoices;
  }

  @Override
  public EntityCache<SegmentChord> getSegmentChords() {
    return segmentChords;
  }

  @Override
  public EntityCache<SegmentMeme> getSegmentMemes() {
    return segmentMemes;
  }

  @Override
  public EntityCache<SegmentMessage> getSegmentMessages() {
    return segmentMessages;
  }

  @Override
  public EntityCache<SegmentChoiceArrangementPick> getSegmentPicks() {
    return segmentPicks;
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

      segmentDAO.createAllSubEntities(access, segmentMessages.flushInserts());
      segmentDAO.createAllSubEntities(access, segmentMemes.flushInserts());
      segmentDAO.createAllSubEntities(access, segmentChords.flushInserts());
      segmentDAO.createAllSubEntities(access, segmentChoices.flushInserts());
      segmentDAO.createAllSubEntities(access, segmentArrangements.flushInserts()); // after choices
      segmentDAO.createAllSubEntities(access, segmentPicks.flushInserts()); // after arrangements

    } catch (JsonApiException | DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
      throw new FabricationException("Failed to build and update payload for Segment!", e);
    }
  }

  @Override
  public SegmentChoice getChoiceOfType(ProgramType type) throws FabricationException {
    Optional<SegmentChoice> choice = getSegmentChoices().getAll().stream().filter(c -> c.getType().equals(type)).findFirst();
    if (choice.isEmpty()) throw new FabricationException(String.format("No %s-type choice in workbench segment", type));
    return choice.get();
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  /**
   Returns the current report map as json, and clears the report so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws JsonApiException {
    String reported = payloadFactory.serialize(report);
    segmentMessages.add(SegmentMessage.create(segment, MessageType.Info, reported));
    report.clear();
  }

}
