// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.tables.records.AccountUserRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface AccountUserDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @param entity for the new Account User.
   @return newly readMany record
   */
  AccountUserRecord create(Access access, AccountUser entity) throws Exception;

  /**
   Fetch one AccountUser if accessible

   @param id of AccountUser
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AccountUserRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many AccountUser for one Account by id, if accessible

   @param accountId to fetch accountUsers for.
   @return JSONArray of accountUsers.
   @throws Exception on failure
   */
  Result<AccountUserRecord> readAll(Access access, ULong accountId) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified AccountUser

   @param id of specific AccountUser to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
