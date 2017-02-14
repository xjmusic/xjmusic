// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.morph.MorphWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface MorphDAO {
  /**
   * Create a new Morph
   * @param data for the new Morph.
   * @return newly created Morph record.
   */
  JSONObject create(AccessControl access, MorphWrapper data) throws Exception;

  /**
   * Fetch one Morph by id, if accessible
   *
   * @param access control
   * @param morphId to fetch
   * @return Morph if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong morphId) throws Exception;

  /**
   * Read all Morphs that are accessible
   *
   * @param access control
   * @return array of morphs as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong arrangementId) throws Exception;

  /**
   * Update a specified Morph
   * @param morphId of specific Morph to update.
   * @param data for the updated Morph.
   */
  void update(AccessControl access, ULong morphId, MorphWrapper data) throws Exception;

  /**
   * Delete a specified Morph
   * @param morphId of specific Morph to delete.
   */
  void delete(AccessControl access, ULong morphId) throws Exception;
}
