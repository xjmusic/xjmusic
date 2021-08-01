// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import com.google.common.base.Strings;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.nexus.dao.exception.DAOExistenceException;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 Utilities for working with segments
 */
public enum Segments {
  ;
  private static final long MILLIS_PER_SECOND = 1000;

  /**
   Find first segment choice of a given type in a collection of segment choices

   @param segmentChoices to filter from
   @param type           to find one of
   @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, Program.Type type) throws DAOExistenceException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getProgramType().equals(type)).findFirst();
    if (found.isEmpty()) throw new DAOExistenceException(String.format("No %s-type choice found", type));
    return found.get();
  }

  /**
   Get the identifier or a Segment: embed key if available, else ID

   @param segment to get identifier of
   @return embed key if available, else ID
   */
  public static String getIdentifier(@Nullable Segment segment) {
    if (Objects.isNull(segment)) return "N/A";
    return Strings.isNullOrEmpty(segment.getStorageKey()) ? segment.getId() : segment.getStorageKey();
  }


  /**
   Get the last dubbed from any collection of Segments

   @param segments to get last dubbed from
   @return last dubbed segment from collection
   */
  public static Optional<Segment> getLastDubbed(Collection<Segment> segments) {
    return getLast(getDubbed(segments));
  }

  /**
   Get the last from any collection of Segments

   @param segments to get last from
   @return last segment from collection
   */
  public static Optional<Segment> getLast(Collection<Segment> segments) {
    return segments
      .stream()
      .max(Comparator.comparing(Segment::getOffset));
  }

  /**
   Get only the dubbed from any collection of Segments

   @param segments to get dubbed from
   @return dubbed segments from collection
   */
  public static Collection<Segment> getDubbed(Collection<Segment> segments) {
    return segments
      .stream()
      .filter(segment -> Segment.State.Dubbed == segment.getState())
      .collect(Collectors.toList());
  }

  /**
   Get the length of a Segment in seconds

   @param segment for which to get length
   @return length of segment in seconds
   */
  public static float getLengthSeconds(Segment segment) {
    return (float) (Instant.parse(segment.getEndAt()).toEpochMilli() - Instant.parse(segment.getBeginAt()).toEpochMilli()) / MILLIS_PER_SECOND;
  }
}
