// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.segment_chord.SegmentChord;

import java.math.BigInteger;
import java.util.Collection;

public interface SegmentChordDAO extends DAO<SegmentChord> {

  /**
   Fetch many segmentChord for many Segments by id, if accessible
   order by position descending, ala other "in chain" results

   @param access  control
   @param segmentIds to fetch segmentChords for.
   @return JSONArray of segmentChords.
   @throws Exception on failure
   */
  Collection<SegmentChord> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception;

}
