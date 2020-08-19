// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;
import io.xj.service.nexus.entity.SegmentMessage;

import java.util.Collection;

public interface SegmentWorkbench {

  /**
   Get the segment that's being worked on.
   Modify this object's properties.
   At the end of the fabrication process, this segment is saved via DAO

   @return current segment
   */
  Segment getSegment();

  /**
   @return entity cache of SegmentChoiceArrangement
   */
  Collection<SegmentChoiceArrangement> getSegmentArrangements() throws FabricationException;

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices() throws FabricationException;

  /**
   @return entity cache of SegmentChord
   */
  Collection<SegmentChord> getSegmentChords() throws FabricationException;

  /**
   @return entity cache of SegmentMeme
   */
  Collection<SegmentMeme> getSegmentMemes() throws FabricationException;

  /**
   @return entity cache of SegmentMessage
   */
  Collection<SegmentMessage> getSegmentMessages() throws FabricationException;

  /**
   @return entity cache of SegmentChoiceArrangementPick
   */
  Collection<SegmentChoiceArrangementPick> getSegmentPicks() throws FabricationException;

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard content JSON

   @param key   to put
   @param value to put
   */
  void putReport(String key, Object value);

  /***
   Called at the end of Segment fabrication.
   Sends added records to segmentDAO batch insert method
   */
  void done() throws FabricationException, JsonApiException, ValueException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws FabricationException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(ProgramType type) throws FabricationException;

  /**
   Get the Chain this Segment Workbench is working within

   @return Chain that this this Segment Workbench is working within
   */
  Chain getChain();

  /**
   Add an Entity

   @param entity to add
   @param <N>    type of Entity
   @return entity that was added
   @throws FabricationException on failure
   */
  <N extends Entity> N add(N entity) throws FabricationException;
}
