// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.tables.records.LinkMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.List;

public interface LinkMemeDAO {

  /**
   Create a new Link Meme

   @param access control
   @param entity for the new Link Meme.
   @return newly readMany record
   */
  LinkMemeRecord create(Access access, LinkMeme entity) throws Exception;

  /**
   Fetch one LinkMeme if accessible

   @param access control
   @param id     of LinkMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  LinkMemeRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many LinkMeme for one Link by id, if accessible

   @param access control
   @param linkId to fetch linkMemes for.
   @return JSONArray of linkMemes.
   @throws Exception on failure
   */
  Result<LinkMemeRecord> readAll(Access access, ULong linkId) throws Exception;

  /**
   Fetch many linkMeme for many Links by id, if accessible

   @return JSONArray of linkMemes.
   @throws Exception on failure
    @param access control
   @param linkIds to fetch linkMemes for.
   */
  Result<LinkMemeRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception;

  /**
   Delete a specified LinkMeme

   @param access control
   @param id     of specific LinkMeme to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
