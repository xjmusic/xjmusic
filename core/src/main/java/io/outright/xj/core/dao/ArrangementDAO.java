// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.arrangement.ArrangementWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ArrangementDAO {
  /**
   Create a new Arrangement

   @param data for the new Arrangement.
   @return newly created Arrangement record.
   */
  JSONObject create(AccessControl access, ArrangementWrapper data) throws Exception;

  /**
   Fetch one Arrangement by id, if accessible

   @param access        control
   @param arrangementId to fetch
   @return Arrangement if found
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong arrangementId) throws Exception;

  /**
   Read all Arrangements that are accessible

   @param access control
   @return array of arrangements as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong choiceId) throws Exception;

  /**
   Update a specified Arrangement

   @param arrangementId of specific Arrangement to update.
   @param data          for the updated Arrangement.
   */
  void update(AccessControl access, ULong arrangementId, ArrangementWrapper data) throws Exception;

  /**
   Delete a specified Arrangement

   @param arrangementId of specific Arrangement to delete.
   */
  void delete(AccessControl access, ULong arrangementId) throws Exception;
}
