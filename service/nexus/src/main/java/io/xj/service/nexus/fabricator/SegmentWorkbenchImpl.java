// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.cache.EntityCache;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentMessage;

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
  private final Segment segment;
  private final SegmentDAO segmentDAO;
  private final PayloadFactory payloadFactory;
  private final EntityCache<SegmentChoiceArrangement> segmentArrangements;
  private final EntityCache<SegmentChoice> segmentChoices;
  private final EntityCache<SegmentChord> segmentChords;
  private final EntityCache<SegmentMeme> segmentMemes;
  private final EntityCache<SegmentMessage> segmentMessages;
  private final EntityCache<SegmentChoiceArrangementPick> segmentPicks;
  private final Access access;
  private Map<String, Object> report = Maps.newConcurrentMap();

  @Inject
  public SegmentWorkbenchImpl(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment,
    SegmentDAO segmentDAO,
    PayloadFactory payloadFactory
  ) throws HubException {
    this.access = access;
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
    segmentDAO.readAllSubEntities(access, ImmutableList.of(segment.getId()), true)
      .forEach(entity -> {
        switch (entity.getClass().getSimpleName()) {
          case "SegmentChoiceArrangementPick":
            segmentPicks.add((SegmentChoiceArrangementPick) entity);
            break;
          case "SegmentChoiceArrangement":
            segmentArrangements.add((SegmentChoiceArrangement) entity);
            break;
          case "SegmentChoice":
            segmentChoices.add((SegmentChoice) entity);
            break;
          case "SegmentChord":
            segmentChords.add((SegmentChord) entity);
            break;
          case "SegmentMeme":
            segmentMemes.add((SegmentMeme) entity);
            break;
        }
      });

    // flush all inserts, so that the workbench can tell the difference between content that was loaded before work began, and new content that needs to be inserted
    segmentPicks.flushInserts();
    segmentArrangements.flushInserts();
    segmentChoices.flushInserts();
    segmentChords.flushInserts();
    segmentMemes.flushInserts();
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
  public void done() throws HubException, RestApiException, ValueException {
    sendReportToSegmentMessage();

    segmentDAO.update(access, getSegment().getId(), getSegment());

    segmentDAO.createAllSubEntities(access,
      ImmutableList.<Entity>builder()
        .addAll(segmentMessages.flushInserts())
        .addAll(segmentMemes.flushInserts())
        .addAll(segmentChords.flushInserts())
        .addAll(segmentChoices.flushInserts())
        .addAll(segmentArrangements.flushInserts()) // after choices
        .addAll(segmentPicks.flushInserts()) // after arrangements
        .build());
    // TODO write entire segment payload to JSON file
  }

  @Override
  public SegmentChoice getChoiceOfType(ProgramType type) throws HubException {
    Optional<SegmentChoice> choice = getSegmentChoices().getAll().stream().filter(c -> c.getType().equals(type)).findFirst();
    if (choice.isEmpty()) throw new HubException(String.format("No %s-type choice in workbench segment", type));
    return choice.get();
  }

  /**
   Returns the current report map as json, and clears the report so it'll only be reported once
   */
  private void sendReportToSegmentMessage() throws RestApiException {
    String reported = payloadFactory.serialize(report);
    segmentMessages.add(SegmentMessage.create(segment, MessageType.Info, reported));
    report.clear();
  }

}
