// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.instrument.InstrumentWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface InstrumentDAO {

  /**
   * (ADMIN ONLY)
   * Create a new Account User
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, InstrumentWrapper data) throws Exception;

  /**
   * Fetch one instrument if accessible
   *
   * @param access control
   * @param id     of instrument
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many instrument for one Account by id, if accessible
   *
   * @param access    control
   * @param accountId to fetch instruments for.
   * @return JSONArray of instruments.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllInAccount(AccessControl access, ULong accountId) throws Exception;

  /**
   * Fetch many instrument for one Library by id, if accessible
   *
   * @param access    control
   * @param libraryId to fetch instruments for.
   * @return JSONArray of instruments.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllInLibrary(AccessControl access, ULong libraryId) throws Exception;

  /**
   * (ADMIN ONLY)
   * Update a specified Instrument
   *
   * @param access control
   * @param instrumentId of specific Instrument to update.
   * @param data   for the updated Instrument.
   */
  void update(AccessControl access, ULong instrumentId, InstrumentWrapper data) throws Exception;

  /**
   * (ADMIN ONLY)
   * Delete a specified instrument
   *
   * @param access control
   * @param id of specific instrument to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
