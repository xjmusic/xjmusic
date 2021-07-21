// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.nexus.NexusException;

import java.util.Collection;
import java.util.Optional;

public interface SegmentRetrospective {

  /**
   @return entity cache of SegmentChoiceArrangementPick
   @param segment to get picks for
   @param includeInertial
   */
  Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks(Segment segment, Boolean includeInertial);

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices(Segment segment, Boolean includeInertial);

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
   Get the segment immediately previous to the current segment

   @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   */
  Optional<SegmentChoice> getPreviousSegmentChoiceOfType(Program.Type type);

  /**
   Add an Entity

   @param entity to add
   @param <N>    type of Entity
   @return entity that was added
   @throws NexusException on failure
   */
  <N> N add(N entity) throws NexusException;
}
