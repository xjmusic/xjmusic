// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityStoreException;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentMeme;

import java.util.Collection;
import java.util.Optional;

public interface SegmentRetrospective {

  /**
   @param segment to get picks for
   @return entity cache of SegmentChoiceArrangementPick
   */
  Collection<SegmentChoiceArrangementPick> getSegmentPicks(Segment segment) throws FabricationException;

  /**
   @return entity cache of SegmentChoice
   */
  Collection<SegmentChoice> getSegmentChoices(Segment segment) throws FabricationException;

  /**
   @return entity cache of SegmentMeme
   */
  Collection<SegmentMeme> getSegmentMemes(Segment segment) throws FabricationException;

  /**
   @return all cached segments
   */
  Collection<Segment> getSegments() throws FabricationException;

  /**
   Get the choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws FabricationException if no such choice type exists
   */
  SegmentChoice getChoiceOfType(Segment segment, ProgramType type) throws FabricationException, EntityStoreException;

  /**
   Get the arrangements for a given choice

   @param segmentChoice to get arrangements for
   @return arrangements for choice
   */
  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice) throws FabricationException;

  /**
   Get the segment immediately previous to the current segment

   @return previous segment
   */
  Optional<Segment> getPreviousSegment();

  /**
   Get the previous-segment choice of a given type

   @param type of choice to get
   @return choice of given type
   @throws FabricationException if no such choice type exists
   */
  SegmentChoice getPreviousChoiceOfType(ProgramType type) throws FabricationException;

  /**
   Add an Entity

   @param entity to add
   @param <N>    type of Entity
   @return entity that was added
   @throws FabricationException on failure
   */
  <N extends Entity> N add(N entity) throws FabricationException;
}
