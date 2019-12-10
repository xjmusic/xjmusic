// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import io.xj.core.cache.EntityCache;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentMessage;

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
  void done() throws CoreException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(ProgramType type) throws CoreException;
}
