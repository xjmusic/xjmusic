// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.app.access.impl.Access;
import io.xj.core.model.account.Account;
import io.xj.core.tables.records.AccountRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface AccountDAO {
  /**
   (ADMIN ONLY)
   Create a new Account

   @param access control
   @param entity for the new Account.
   @return newly readMany Account record.
   */
  AccountRecord create(Access access, Account entity) throws Exception;

  /**
   Fetch one Account by id, if accessible

   @param access control
   @param id     to fetch
   @return Account if found
   @throws Exception on failure
   */
  @Nullable
  AccountRecord readOne(Access access, ULong id) throws Exception;

  /**
   Read all Accounts that are accessible

   @param access control
   @return array of accounts as JSON
   @throws Exception on failure
   */
  Result<AccountRecord> readAll(Access access) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Account

   @param access control
   @param id     of specific Account to update.
   @param entity for the updated Account.
   */
  void update(Access access, ULong id, Account entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified Account

   @param access    control
   @param accountId of specific Account to delete.
   */
  void delete(Access access, ULong accountId) throws Exception;
}
