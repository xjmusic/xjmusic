// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.account.Account;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface AccountDAO {
  /**
   (ADMIN ONLY)
   Create a new Account

   @param access control
   @param entity for the new Account.
   @return newly readMany Account record.
   */
  Account create(Access access, Account entity) throws Exception;

  /**
   Fetch one Account by id, if accessible

   @param access control
   @param id     to fetch
   @return Account if found
   @throws Exception on failure
   */
  @Nullable
  Account readOne(Access access, BigInteger id) throws Exception;

  /**
   Read all Accounts that are accessible

   @param access control
   @return array of accounts as JSON
   @throws Exception on failure
   */
  Collection<Account> readAll(Access access) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Account

   @param access control
   @param id     of specific Account to update.
   @param entity for the updated Account.
   */
  void update(Access access, BigInteger id, Account entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified Account
   * @param access    control
   @param id of specific Account to delete.

   */
  void delete(Access access, BigInteger id) throws Exception;
}
