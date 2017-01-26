// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account_user.AccountUserWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface AccountUserDAO {

  /**
   * (ADMIN ONLY)
   * Create a new Account User
   *
   * @param data for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccountUserWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one AccountUser if accessible
   *
   * @param id of AccountUser
   * @return retrieved record as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException;

  /**
   * Fetch many AccountUser for one Account by id, if accessible
   *
   * @param accountId to fetch accountUsers for.
   * @return JSONArray of accountUsers.
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access, ULong accountId) throws DatabaseException;

  /**
   * (ADMIN ONLY)
   * Delete a specified AccountUser
   * @param id of specific AccountUser to delete.
   */
  void delete(ULong id) throws DatabaseException, ConfigException, BusinessException;
}
