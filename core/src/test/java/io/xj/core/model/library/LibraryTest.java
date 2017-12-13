// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.LibraryRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.tables.Library.LIBRARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LibraryTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Library()
      .setAccountId(BigInteger.valueOf(562))
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    new Library()
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new Library()
      .setAccountId(BigInteger.valueOf(562))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    LibraryRecord record = new LibraryRecord();
    record.setId(ULong.valueOf(12));
    record.setAccountId(ULong.valueOf(562));
    record.setName("Mic Check One Two");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Library result = new Library()
      .setFromRecord(record);

    assertNotNull(result);
    Assert.assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(562), result.getAccountId());
    assertEquals("Mic Check One Two", result.getName());
    Assert.assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    Assert.assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Library().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Library()
      .setAccountId(BigInteger.valueOf(562))
      .setName("Mic Check One Two")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(562), result.get(LIBRARY.ACCOUNT_ID));
    assertEquals("Mic Check One Two", result.get(LIBRARY.NAME));
  }

}
