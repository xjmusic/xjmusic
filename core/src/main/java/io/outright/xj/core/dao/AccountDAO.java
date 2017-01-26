// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account.AccountWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AccountDAO {
  /**
   * (ADMIN ONLY)
   * Create a new Account
   * @param data for the new Account.
   * @return newly created Account record.
   */
  JSONObject create(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one Account by id, if accessible
   *
   * @param access control
   * @param accountId to fetch
   * @return Account if found
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong accountId) throws DatabaseException;

  /**
   * Read all Accounts that are accessible
   *
   * @param access control
   * @return array of accounts as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access) throws DatabaseException;

  /**
   * (ADMIN ONLY)
   * Update a specified Account
   * @param accountId of specific Account to update.
   * @param data for the updated Account.
   */
  void update(ULong accountId, AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * (ADMIN ONLY)
   * Delete a specified Account
   * @param accountId of specific Account to delete.
   */
  void delete(ULong accountId) throws DatabaseException, ConfigException, BusinessException;
}
