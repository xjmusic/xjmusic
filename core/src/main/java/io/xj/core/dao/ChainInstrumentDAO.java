// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_instrument.ChainInstrument;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChainInstrumentDAO {

  /**
   Create a new Chain Instrument

   @param entity for the new Chain Instrument.
   @return newly readMany record
   */
  ChainInstrument create(Access access, ChainInstrument entity) throws Exception;

  /**
   Fetch one ChainInstrument if accessible

   @param id of ChainInstrument
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainInstrument readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many ChainInstrument for one Chain by id, if accessible

   @param chainId to fetch chainInstruments for.
   @return JSONArray of chainInstruments.
   @throws Exception on failure
   */
  Collection<ChainInstrument> readAll(Access access, BigInteger chainId) throws Exception;

  /**
   Delete a specified ChainInstrument

   @param id of specific ChainInstrument to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
