// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.link_meme.LinkMeme;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface LinkMemeDAO {

  /**
   Create a new Link Meme

   @param access control
   @param entity for the new Link Meme.
   @return newly readMany record
   */
  LinkMeme create(Access access, LinkMeme entity) throws Exception;

  /**
   Fetch one LinkMeme if accessible

   @param access control
   @param id     of LinkMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkMeme readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many LinkMeme for one Link by id, if accessible

   @param access control
   @param linkId to fetch linkMemes for.
   @return JSONArray of linkMemes.
   @throws Exception on failure
   */
  Collection<LinkMeme> readAll(Access access, BigInteger linkId) throws Exception;

  /**
   Fetch many linkMeme for many Links by id, if accessible

   @param access  control
   @param linkIds to fetch linkMemes for.
   @return JSONArray of linkMemes.
   @throws Exception on failure
   */
  Collection<LinkMeme> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception;

  /**
   Delete a specified LinkMeme

   @param access control
   @param id     of specific LinkMeme to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
