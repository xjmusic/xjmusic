// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.tables.records.InstrumentRecord;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface InstrumentDAO {

  /**
   Create a new Instrument

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  InstrumentRecord create(Access access, Instrument entity) throws Exception;

  /**
   Fetch one instrument if accessible

   @param access control
   @param id     of instrument
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  InstrumentRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @param access    control
   @param accountId to fetch instruments for.
   @return JSONArray of instruments.
   @throws Exception on failure
   */
  Result<InstrumentRecord> readAllInAccount(Access access, ULong accountId) throws Exception;

  /**
   Fetch many instrument for one Library by id, if accessible

   @param access    control
   @param libraryId to fetch instruments for.
   @return JSONArray of instruments.
   @throws Exception on failure
   */
  Result<InstrumentRecord> readAllInLibrary(Access access, ULong libraryId) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch instruments for.
   @param instrumentType  to fetch
   */
  Result<? extends Record> readAllBoundToChain(Access access, ULong chainId, InstrumentType instrumentType) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch instruments for.
   @param instrumentType  to fetch
   */
  Result<? extends Record> readAllBoundToChainLibrary(Access access, ULong chainId, InstrumentType instrumentType) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Instrument

   @param access       control
   @param instrumentId of specific Instrument to update.
   @param entity       for the updated Instrument.
   */
  void update(Access access, ULong instrumentId, Instrument entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified instrument

   @param access control
   @param id     of specific instrument to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
