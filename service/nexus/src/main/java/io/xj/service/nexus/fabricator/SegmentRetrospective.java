// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import io.xj.service.hub.HubException;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;

import java.util.Collection;
import java.util.Optional;

public interface SegmentRetrospective {

  /**
   @return entity cache of SegmentChoiceArrangementPick
   @param segment to get picks for
   */
  Collection<SegmentChoiceArrangementPick> getSegmentPicks(Segment segment);

  /**
   @return entity cache of SegmentChoiceArrangement
   */
  Collection<SegmentChoiceArrangement> getSegmentArrangements(SegmentChoice choice);

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  /**
   @return entity cache of SegmentChord
   */
  Collection<SegmentChord> getSegmentChords(Segment segment);

  /**
   @return entity cache of SegmentMeme
   */
  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  /**
   * @return all cached segments
   */
  Collection<Segment> getSegments();

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws HubException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(Segment segment, ProgramType type) throws HubException;

  /**
   * Get the arrangements for a given choice
   * @param segmentChoice to get arrangements for
   * @return arrangements for choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice);

  /**
   * Get the segment immediately previous to the current segment
   * @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws HubException if no such choice type exists
   */
  SegmentChoice getPreviousChoiceOfType(ProgramType type) throws HubException;

}
