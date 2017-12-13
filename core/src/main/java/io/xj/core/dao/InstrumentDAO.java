// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.tables.records.InstrumentRecord;

import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.Collection;

public interface InstrumentDAO {

  /**
   Create a new Instrument

   @return newly readMany record
    @param access control
   @param entity for the new Account User.
   */
  Instrument create(Access access, Instrument entity) throws Exception;

  /**
   Fetch one instrument if accessible

   @return retrieved record
   @throws Exception on failure
    @param access control
   @param id     of instrument
   */
  @Nullable
  Instrument readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param accountId to fetch instruments for.
   */
  Collection<Instrument> readAllInAccount(Access access, ULong accountId) throws Exception;

  /**
   Fetch many instrument for one Library by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param libraryId to fetch instruments for.
   */
  Collection<Instrument> readAllInLibrary(Access access, ULong libraryId) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch instruments for.
   @param instrumentType  to fetch
   */
  Collection<Instrument> readAllBoundToChain(Access access, ULong chainId, InstrumentType instrumentType) throws Exception;

  /**
   Fetch many instrument for one Account by id, if accessible

   @return JSONArray of instruments.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch instruments for.
   @param instrumentType  to fetch
   */
  Collection<Instrument> readAllBoundToChainLibrary(Access access, ULong chainId, InstrumentType instrumentType) throws Exception;

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
