// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.AccountRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.ACCOUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Account()
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Account name is required");

    new Account()
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    AccountRecord record = new AccountRecord();
    record.setId(ULong.valueOf(12));
    record.setName("Mic Check One Two");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Account result = new Account()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals("Mic Check One Two", result.getName());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Account().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Account()
      .setName("Mic Check One Two")
      .updatableFieldValueMap();

    assertEquals("Mic Check One Two", result.get(ACCOUNT.NAME));
  }

}
