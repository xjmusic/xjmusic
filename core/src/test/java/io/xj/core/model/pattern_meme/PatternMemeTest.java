// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.PatternMemeRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.PATTERN_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PatternMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PatternMeme()
      .setPatternId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new PatternMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PatternMeme()
      .setPatternId(BigInteger.valueOf(23678))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    PatternMemeRecord record = new PatternMemeRecord();
    record.setId(ULong.valueOf(12));
    record.setPatternId(ULong.valueOf(23678));
    record.setName("Miccheckonetwo");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    PatternMeme result = new PatternMeme()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(23678), result.getPatternId());
    assertEquals("Miccheckonetwo", result.getName());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new PatternMeme().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new PatternMeme()
      .setPatternId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(23678), result.get(PATTERN_MEME.PATTERN_ID));
    assertEquals("Miccheckonetwo", result.get(PATTERN_MEME.NAME));
  }

}
