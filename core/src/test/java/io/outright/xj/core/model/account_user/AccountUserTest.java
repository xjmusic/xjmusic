// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.account_user;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.AccountUserRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountUserTest {
  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new AccountUser()
      .setUserId(BigInteger.valueOf(125434))
      .setAccountId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    new AccountUser()
      .setUserId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User ID is required");

    new AccountUser()
      .setAccountId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    AccountUserRecord record = new AccountUserRecord();
    record.setId(ULong.valueOf(12));
    record.setAccountId(ULong.valueOf(6));
    record.setUserId(ULong.valueOf(87));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    AccountUser result = new AccountUser()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(6), result.getAccountId());
    assertEquals(ULong.valueOf(87), result.getUserId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new AccountUser().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new AccountUser()
      .setAccountId(BigInteger.valueOf(6))
      .setUserId(BigInteger.valueOf(87))
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(6), result.get(ACCOUNT_USER.ACCOUNT_ID));
    assertEquals(ULong.valueOf(87), result.get(ACCOUNT_USER.USER_ID));
  }

}
