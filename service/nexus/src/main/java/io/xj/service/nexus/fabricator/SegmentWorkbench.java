// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import io.xj.lib.entity.EntityCache;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.entity.*;

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
  EntityCache<SegmentChoiceArrangement> getSegmentArrangements();

  /**
   @return entity cache of SegmentChoice
   */
  EntityCache<SegmentChoice> getSegmentChoices();

  /**
   @return entity cache of SegmentChord
   */
  EntityCache<SegmentChord> getSegmentChords();

  /**
   @return entity cache of SegmentMeme
   */
  EntityCache<SegmentMeme> getSegmentMemes();

  /**
   @return entity cache of SegmentMessage
   */
  EntityCache<SegmentMessage> getSegmentMessages();

  /**
   @return entity cache of SegmentChoiceArrangementPick
   */
  EntityCache<SegmentChoiceArrangementPick> getSegmentPicks();

  /**
   Put a key-value pair into the report
   [#162999779] only exports data as a sub-field of the standard content JSON

   @param key   to put
   @param value to put
   */
  void putReport(String key, Object value);

  /***
   Called at the end of Segment fabrication.
   First, turns the report map into a json payload of a new segment message (in the EntityCache)
   Calls each EntityCache writeInserts() methods- which gets only the added records and clears the added-records queue
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
}
