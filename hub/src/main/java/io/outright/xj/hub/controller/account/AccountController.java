// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.tables.records.AccountRecord;

import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.sql.ResultSet;

public interface AccountController {
  /**
   * Create a new Account
   * @param data for the new Account.
   * @return newly created Account record.
   */
  AccountRecord create(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one Account by id
   *
   * @param accountId to fetch.
   * @return Account Record.
   * @throws DatabaseException on failure
   */
  @Nullable
  AccountRecord read(ULong accountId) throws DatabaseException;

  /**
   * Fetch many Accounts
   *
   * @return ResultSet of accounts
   * @throws DatabaseException on failure
   */
  @Nullable
  ResultSet readAll() throws DatabaseException;

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
