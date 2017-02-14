// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.account.AccountWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AccountDAO {
  /**
   * (ADMIN ONLY)
   * Create a new Account
   * @param access control
   * @param data for the new Account.
   * @return newly created Account record.
   */
  JSONObject create(AccessControl access, AccountWrapper data) throws Exception;

  /**
   * Fetch one Account by id, if accessible
   *
   * @param access control
   * @param id to fetch
   * @return Account if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Read all Accounts that are accessible
   *
   * @param access control
   * @return array of accounts as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAll(AccessControl access) throws Exception;

  /**
   * (ADMIN ONLY)
   * Update a specified Account
   * @param access control
   * @param id of specific Account to update.
   * @param data for the updated Account.
   */
  void update(AccessControl access, ULong id, AccountWrapper data) throws Exception;

  /**
   * (ADMIN ONLY)
   * Delete a specified Account
   * @param access control
   * @param accountId of specific Account to delete.
   */
  void delete(AccessControl access, ULong accountId) throws Exception;
}
