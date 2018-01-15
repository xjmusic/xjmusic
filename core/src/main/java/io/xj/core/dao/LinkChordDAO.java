// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_chord.LinkChord;

import java.math.BigInteger;
import java.util.Collection;

public interface LinkChordDAO extends DAO<LinkChord> {

  /**
   Fetch many linkChord for many Links by id, if accessible
   order by position descending, ala other "in chain" results

   @param access  control
   @param linkIds to fetch linkChords for.
   @return JSONArray of linkChords.
   @throws Exception on failure
   */
  Collection<LinkChord> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

}
