// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.idea_meme;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.IdeaMemeRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.IDEA_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class IdeaMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new IdeaMeme()
      .setIdeaId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutIdeaID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Idea ID is required");

    new IdeaMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new IdeaMeme()
      .setIdeaId(BigInteger.valueOf(23678))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    IdeaMemeRecord record = new IdeaMemeRecord();
    record.setId(ULong.valueOf(12));
    record.setIdeaId(ULong.valueOf(23678));
    record.setName("Miccheckonetwo");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    IdeaMeme result = new IdeaMeme()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(23678), result.getIdeaId());
    assertEquals("Miccheckonetwo", result.getName());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new IdeaMeme().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new IdeaMeme()
      .setIdeaId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(23678), result.get(IDEA_MEME.IDEA_ID));
    assertEquals("Miccheckonetwo", result.get(IDEA_MEME.NAME));
  }

}
