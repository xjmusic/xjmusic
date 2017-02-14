// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
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
  JSONObject create(AccessControl access, AccountUserWrapper data) throws Exception;

  /**
   * Fetch one AccountUser if accessible
   *
   * @param id of AccountUser
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many AccountUser for one Account by id, if accessible
   *
   * @param accountId to fetch accountUsers for.
   * @return JSONArray of accountUsers.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception;

  /**
   * (ADMIN ONLY)
   * Delete a specified AccountUser
   * @param id of specific AccountUser to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
