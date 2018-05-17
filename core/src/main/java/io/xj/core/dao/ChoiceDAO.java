// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.sequence.SequenceType;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChoiceDAO extends DAO<Choice> {

  /**
   Read one choice, binding a given sequence to a given segment

   @param access    control
   @param segmentId    to get choice for
   @param sequenceId to get choice for
   @return choice, or null if none exists
   @throws Exception on failure
   */
  @Nullable
  Choice readOneSegmentSequence(Access access, BigInteger segmentId, BigInteger sequenceId) throws Exception;

  /**
   Read the Choice of given type of Sequence for a given Segment,
   including the pattern offset, and all eitherOr
   pattern offsets for that Sequence.

   @param access      control
   @param segmentId      segment to get choice for
   @param sequenceType type for choice to get
   @return record of choice, and eitherOr pattern offsets
   */
  @Nullable
  Choice readOneSegmentTypeWithAvailablePatternOffsets(Access access, BigInteger segmentId, SequenceType sequenceType) throws Exception;

  /**
   Fetch many choice for many Segments by id, if accessible

   @param access  control
   @param segmentIds to fetch choices for.
   @return JSONArray of choices.
   @throws Exception on failure
   */
  Collection<Choice> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception;

}
