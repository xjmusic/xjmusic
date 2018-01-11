// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument_meme.InstrumentMeme;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface InstrumentMemeDAO {

  /**
   Create a new Instrument Meme

   @param access control
   @param entity for the new Instrument Meme.
   @return newly readMany record
   */
  InstrumentMeme create(Access access, InstrumentMeme entity) throws Exception;

  /**
   Fetch one InstrumentMeme if accessible

   @param access control
   @param id     of InstrumentMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  InstrumentMeme readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many InstrumentMeme for one Instrument by id, if accessible

   @param access       control
   @param instrumentId to fetch instrumentMemes for.
   @return JSONArray of instrumentMemes.
   @throws Exception on failure
   */
  Collection<InstrumentMeme> readAll(Access access, BigInteger instrumentId) throws Exception;

  /**
   Delete a specified InstrumentMeme

   @param access control
   @param id     of specific InstrumentMeme to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
