// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.chain_instrument.ChainInstrumentWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ChainInstrumentDAO {

  /**
   Create a new Chain Instrument

   @param data for the new Chain Instrument.
   @return newly created record as JSON
   */
  JSONObject create(AccessControl access, ChainInstrumentWrapper data) throws Exception;

  /**
   Fetch one ChainInstrument if accessible

   @param id of ChainInstrument
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Fetch many ChainInstrument for one Chain by id, if accessible

   @param chainId to fetch chainInstruments for.
   @return JSONArray of chainInstruments.
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainInstrument

   @param id of specific ChainInstrument to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
