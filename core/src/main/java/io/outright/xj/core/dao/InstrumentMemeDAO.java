// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.instrument_meme.InstrumentMemeWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface InstrumentMemeDAO {

  /**
   * Create a new Instrument Meme
   *
   * @param access control
   * @param data   for the new Instrument Meme.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, InstrumentMemeWrapper data) throws Exception;

  /**
   * Fetch one InstrumentMeme if accessible
   *
   * @param access control
   * @param id     of InstrumentMeme
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many InstrumentMeme for one Instrument by id, if accessible
   *
   * @param access control
   * @param instrumentId to fetch instrumentMemes for.
   * @return JSONArray of instrumentMemes.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong instrumentId) throws Exception;

  /**
   * Delete a specified InstrumentMeme
   *
   * @param access control
   * @param id     of specific InstrumentMeme to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
