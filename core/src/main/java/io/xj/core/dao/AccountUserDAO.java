// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.account_user.AccountUser;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface AccountUserDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @param entity for the new Account User.
   @return newly readMany record
   */
  AccountUser create(Access access, AccountUser entity) throws Exception;

  /**
   Fetch one AccountUser if accessible

   @param id of AccountUser
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AccountUser readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many AccountUser for one Account by id, if accessible

   @param accountId to fetch accountUsers for.
   @return JSONArray of accountUsers.
   @throws Exception on failure
   */
  Collection<AccountUser> readAll(Access access, BigInteger accountId) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified AccountUser

   @param id of specific AccountUser to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
