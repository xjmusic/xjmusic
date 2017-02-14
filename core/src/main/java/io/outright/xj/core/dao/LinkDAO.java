// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.model.link.LinkWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface LinkDAO {
  /**
   * Create a new Link
   * @param data for the new Link.
   * @return newly created Link record.
   */
  JSONObject create(AccessControl access, LinkWrapper data) throws Exception;

  /**
   * Fetch one Link by id, if accessible
   *
   * @param access control
   * @param linkId to fetch
   * @return Link if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong linkId) throws Exception;

  /**
   * Read all Links that are accessible
   *
   * @param access control
   * @return array of links as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception;

  /**
   * Update a specified Link
   * @param linkId of specific Link to update.
   * @param data for the updated Link.
   */
  void update(AccessControl access, ULong linkId, LinkWrapper data) throws Exception;

  /**
   * Delete a specified Link
   * @param linkId of specific Link to delete.
   */
  void delete(AccessControl access, ULong linkId) throws Exception;
}
