// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_meme.LinkMeme;

import java.math.BigInteger;
import java.util.Collection;

public interface LinkMemeDAO extends DAO<LinkMeme> {

  /**
   Fetch many linkMeme for many Links by id, if accessible

   @param access  control
   @param linkIds to fetch linkMemes for.
   @return JSONArray of linkMemes.
   @throws Exception on failure
   */
  Collection<LinkMeme> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

}
