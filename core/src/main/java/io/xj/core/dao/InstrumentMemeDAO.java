// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.tables.records.InstrumentMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface InstrumentMemeDAO {

  /**
   Create a new Instrument Meme

   @param access control
   @param entity for the new Instrument Meme.
   @return newly readMany record
   */
  InstrumentMemeRecord create(Access access, InstrumentMeme entity) throws Exception;

  /**
   Fetch one InstrumentMeme if accessible

   @param access control
   @param id     of InstrumentMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  InstrumentMemeRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many InstrumentMeme for one Instrument by id, if accessible

   @param access       control
   @param instrumentId to fetch instrumentMemes for.
   @return JSONArray of instrumentMemes.
   @throws Exception on failure
   */
  Result<InstrumentMemeRecord> readAll(Access access, ULong instrumentId) throws Exception;

  /**
   Delete a specified InstrumentMeme

   @param access control
   @param id     of specific InstrumentMeme to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
