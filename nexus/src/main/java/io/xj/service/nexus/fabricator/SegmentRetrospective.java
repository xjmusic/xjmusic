// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentMeme;
import io.xj.service.nexus.NexusException;

import java.util.Collection;
import java.util.Optional;

public interface SegmentRetrospective {

  /**
   @param segment to get picks for
   @return entity cache of SegmentChoiceArrangementPick
   */
  Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks(Segment segment);

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  /**
   @return entity cache of SegmentChoiceArrangement
   */
  Collection<SegmentChoiceArrangement> getSegmentChoiceArrangements(Segment segment);

  /**
   @return entity cache of SegmentMeme
   */
  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  /**
   @return all cached segments
   */
  Collection<Segment> getSegments();

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getChoiceOfType(Segment segment, Program.Type type);

  /**
   Get the arrangements for a given choice

   @param segmentChoice to get arrangements for
   @return arrangements for choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice);

  /**
   Get the segment immediately previous to the current segment

   @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousChoiceOfType(Program.Type type);

  /**
   Add an Entity

   @param entity to add
   @param <N>    type of Entity
   @return entity that was added
   @throws NexusException on failure
   */
  <N> N add(N entity) throws NexusException;
}
