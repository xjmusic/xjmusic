// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.tables.records.ChainInstrumentRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ChainInstrumentDAO {

  /**
   Create a new Chain Instrument

   @param entity for the new Chain Instrument.
   @return newly readMany record
   */
  ChainInstrumentRecord create(Access access, ChainInstrument entity) throws Exception;

  /**
   Fetch one ChainInstrument if accessible

   @param id of ChainInstrument
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainInstrumentRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many ChainInstrument for one Chain by id, if accessible

   @param chainId to fetch chainInstruments for.
   @return JSONArray of chainInstruments.
   @throws Exception on failure
   */
  Result<ChainInstrumentRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainInstrument

   @param id of specific ChainInstrument to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
