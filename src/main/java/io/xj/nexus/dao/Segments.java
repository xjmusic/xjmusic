// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import com.google.common.base.Strings;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.nexus.dao.exception.DAOExistenceException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 Utilities for working with segments
 */
public enum Segments {
  ;

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
}
