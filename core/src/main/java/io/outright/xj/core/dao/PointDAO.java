// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.point.Point;
import io.outright.xj.core.tables.records.PointRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PointDAO {
  /**
   Create a new Point

   @param entity for the new Point.
   @return newly readMany Point record.
   */
  PointRecord create(Access access, Point entity) throws Exception;

  /**
   Fetch one Point by id, if accessible

   @param access control
   @param id     to fetch
   @return Point if found
   @throws Exception on failure
   */
  @Nullable
  PointRecord readOne(Access access, ULong id) throws Exception;

  /**
   Read all Points that are accessible

   @param access control
   @return array of points as JSON
   @throws Exception on failure
   */
  Result<PointRecord> readAll(Access access, ULong morphId) throws Exception;

  /**
   Update a specified Point

   @param pointId of specific Point to update.
   @param entity  for the updated Point.
   */
  void update(Access access, ULong pointId, Point entity) throws Exception;

  /**
   Delete a specified Point

   @param pointId of specific Point to delete.
   */
  void delete(Access access, ULong pointId) throws Exception;
}
