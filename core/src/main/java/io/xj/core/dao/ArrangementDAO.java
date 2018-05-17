// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.arrangement.Arrangement;

import java.math.BigInteger;
import java.util.Collection;

public interface ArrangementDAO extends DAO<Arrangement> {
  /**
   Fetch many arrangement for many Segments by id, if accessible

   @param access  control
   @param segmentIds to fetch arrangements for.
   @return JSONArray of arrangements.
   @throws Exception on failure
   */
  Collection<Arrangement> readAllInSegments(Access access, Collection<BigInteger> segmentIds) throws Exception;
}
