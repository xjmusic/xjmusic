// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.segment_meme.SegmentMeme;

import java.math.BigInteger;
import java.util.Collection;

public interface SegmentMemeDAO extends DAO<SegmentMeme> {

  /**
   Fetch many segmentMeme for many Segments by id, if accessible

   @param access  control
   @param segmentIds to fetch segmentMemes for.
   @return JSONArray of segmentMemes.
   @throws Exception on failure
   */
  Collection<SegmentMeme> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception;

}
