// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.tables.records.InstrumentRecord;

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
