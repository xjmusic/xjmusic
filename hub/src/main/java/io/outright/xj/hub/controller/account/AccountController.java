// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.tables.records.AccountRecord;

import org.jooq.Record;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.sql.ResultSet;

public interface AccountController {
  /**
   * Fetch one Account by id
   *
   * @param accountId to fetch.
   * @return Account Record.
   */
  @Nullable
  Record fetchAccount(ULong accountId) throws DatabaseException;

  /**
   * Fetch many Accounts
   *
   * @return AccountRecord.
   */
  @Nullable
  ResultSet fetchAccounts() throws DatabaseException;

  /**
   * Create a new Account
   * @param data for the createAccount.
   * @return accountId of newly created Account record.
   */
  AccountRecord createAccount(AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Update a specified Account
   * @param accountId of specific Account to update.
   * @param data for the updated Account.
   */
  void updateAccount(ULong accountId, AccountWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Delete a specified Account
   * @param accountId of specific Account to delete.
   */
  void deleteAccount(ULong accountId) throws DatabaseException, ConfigException, BusinessException;
}
