// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.tables.records.AccountUserRecord;

import org.jooq.types.ULong;
import org.json.JSONArray;

import javax.annotation.Nullable;
import java.sql.ResultSet;

public interface AccountUserController {

  /**
   * Create a new Account User
   * @param data for the new Account User.
   * @return newly created AccountUser record.
   */
  AccountUserRecord create(AccountUserWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one AccountUser.
   *
   * @param id of AccountUser.
   * @return AccountUserRecord.
   * @throws DatabaseException on failure
   */
  @Nullable
  AccountUserRecord read(ULong id) throws DatabaseException;

  /**
   * Fetch many AccountUser for one Account by id
   *
   * @param accountId to fetch accountUsers for.
   * @return JSONArray of accountUsers.
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAll(ULong accountId) throws DatabaseException;

  /**
   * Delete a specified AccountUser
   * @param id of specific AccountUser to delete.
   */
  void delete(ULong id) throws DatabaseException, ConfigException, BusinessException;
}
