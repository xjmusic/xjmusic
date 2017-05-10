// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.pick.Pick;
import io.outright.xj.core.tables.records.PickRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PickDAO {
  /**
   Create a new Pick

   @param entity for the new Pick.
   @return newly readMany Pick record.
   */
  PickRecord create(Access access, Pick entity) throws Exception;

  /**
   Fetch one Pick by id, if accessible

   @param access control
   @param id     to fetch
   @return Pick if found
   @throws Exception on failure
   */
  @Nullable
  PickRecord readOne(Access access, ULong id) throws Exception;

  /**
   Read all Picks that are accessible

   @param access control
   @return array of picks as JSON
   @throws Exception on failure
   */
  Result<PickRecord> readAll(Access access, ULong arrangementId) throws Exception;

  /**
   Update a specified Pick

   @param pickId of specific Pick to update.
   @param entity for the updated Pick.
   */
  void update(Access access, ULong pickId, Pick entity) throws Exception;

  /**
   Delete a specified Pick

   @param pickId of specific Pick to delete.
   */
  void delete(Access access, ULong pickId) throws Exception;
}
