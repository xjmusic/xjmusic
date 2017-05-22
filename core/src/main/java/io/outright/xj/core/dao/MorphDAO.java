// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.morph.Morph;
import io.outright.xj.core.tables.records.MorphRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface MorphDAO {
  /**
   Create a new Morph

   @param entity for the new Morph.
   @return newly readMany Morph record.
   */
  MorphRecord create(Access access, Morph entity) throws Exception;

  /**
   Fetch one Morph by id, if accessible

   @param access  control
   @param morphId to fetch
   @return Morph if found
   @throws Exception on failure
   */
  @Nullable
  MorphRecord readOneRecord(Access access, ULong morphId) throws Exception;

  /**
   Read all Morphs that are accessible

   @param access control
   @return array of morphs as JSON
   @throws Exception on failure
   */
  Result<MorphRecord> readAll(Access access, ULong arrangementId) throws Exception;

  /**
   Update a specified Morph

   @param morphId of specific Morph to update.
   @param entity  for the updated Morph.
   */
  void update(Access access, ULong morphId, Morph entity) throws Exception;

  /**
   Delete a specified Morph

   @param morphId of specific Morph to delete.
   */
  void delete(Access access, ULong morphId) throws Exception;
}
