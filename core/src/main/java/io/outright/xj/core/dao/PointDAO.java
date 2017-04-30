// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.point.PointWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface PointDAO {
  /**
   Create a new Point

   @param data for the new Point.
   @return newly created Point record.
   */
  JSONObject create(AccessControl access, PointWrapper data) throws Exception;

  /**
   Fetch one Point by id, if accessible

   @param access control
   @param id     to fetch
   @return Point if found
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Read all Points that are accessible

   @param access control
   @return array of points as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong morphId) throws Exception;

  /**
   Update a specified Point

   @param pointId of specific Point to update.
   @param data    for the updated Point.
   */
  void update(AccessControl access, ULong pointId, PointWrapper data) throws Exception;

  /**
   Delete a specified Point

   @param pointId of specific Point to delete.
   */
  void delete(AccessControl access, ULong pointId) throws Exception;
}
