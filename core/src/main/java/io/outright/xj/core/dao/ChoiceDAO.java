// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.model.choice.ChoiceWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ChoiceDAO {
  /**
   * Create a new Choice
   * @param data for the new Choice.
   * @return newly created Choice record.
   */
  JSONObject create(AccessControl access, ChoiceWrapper data) throws Exception;

  /**
   * Fetch one Choice by id, if accessible
   *
   * @param access control
   * @param choiceId to fetch
   * @return Choice if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong choiceId) throws Exception;

  /**
   * Read all Choices that are accessible
   *
   * @param access control
   * @return array of choices as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong linkId) throws Exception;

  /**
   * Update a specified Choice
   * @param choiceId of specific Choice to update.
   * @param data for the updated Choice.
   */
  void update(AccessControl access, ULong choiceId, ChoiceWrapper data) throws Exception;

  /**
   * Delete a specified Choice
   * @param choiceId of specific Choice to delete.
   */
  void delete(AccessControl access, ULong choiceId) throws Exception;
}
