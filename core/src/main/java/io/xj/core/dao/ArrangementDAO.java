// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.app.access.impl.Access;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.tables.records.ArrangementRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ArrangementDAO {
  /**
   Create a new Arrangement

   @param entity for the new Arrangement.
   @return newly readMany Arrangement record.
   */
  ArrangementRecord create(Access access, Arrangement entity) throws Exception;

  /**
   Fetch one Arrangement by id, if accessible

   @param access        control
   @param arrangementId to fetch
   @return Arrangement if found
   @throws Exception on failure
   */
  @Nullable
  ArrangementRecord readOne(Access access, ULong arrangementId) throws Exception;

  /**
   Read all Arrangements that are accessible

   @param access control
   @return array of arrangements as JSON
   @throws Exception on failure
   */
  Result<ArrangementRecord> readAll(Access access, ULong choiceId) throws Exception;

  /**
   Update a specified Arrangement

   @param arrangementId of specific Arrangement to update.
   @param entity        for the updated Arrangement.
   */
  void update(Access access, ULong arrangementId, Arrangement entity) throws Exception;

  /**
   Delete a specified Arrangement

   @param arrangementId of specific Arrangement to delete.
   */
  void delete(Access access, ULong arrangementId) throws Exception;
}
