// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

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
   * Create a new Account
   * @param data for the new Account.
   * @return newly created Account record.
   */
  JSONObject create(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one Account by id
   *
   * @param accountId to fetch
   * @return Account if found
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOne(ULong accountId) throws DatabaseException;

  /**
   * Fetch one Account by id
   *
   * @param fromUserId of User from which account must be visible
   * @param accountId to fetch
   * @return Account if found and visible
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneVisible(ULong fromUserId, ULong accountId) throws DatabaseException;

  /**
   * Read all Accounts
   *
   * @return array of accounts as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAll() throws DatabaseException;

  /**
   * Read all Accounts visible to a specified User
   *
   * @param fromUserId accounts must be visible-to
   * @return array of accounts as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllVisible(ULong fromUserId) throws DatabaseException;

  /**
   * Update a specified Account
   * @param accountId of specific Account to update.
   * @param data for the updated Account.
   */
  void update(ULong accountId, AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Delete a specified Account
   * @param accountId of specific Account to delete.
   */
  void delete(ULong accountId) throws DatabaseException, ConfigException, BusinessException;
}
