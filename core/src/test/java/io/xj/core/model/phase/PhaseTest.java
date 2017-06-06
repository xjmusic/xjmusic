// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.phase;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.PhaseRecord;

import org.jooq.Field;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PhaseTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Phase()
      .setIdeaId(BigInteger.valueOf(9812))
      .setName("Mic Check One Two")
      .setKey("D major")
      .setTotal(64)
      .setOffset(BigInteger.valueOf(14))
      .setDensity(0.6)
      .setTempo(140.5)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Phase()
      .setIdeaId(BigInteger.valueOf(9812))
      .setOffset(BigInteger.valueOf(14))
      .validate();
  }

  @Test
  public void validate_failsWithoutIdeaID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Idea ID is required");

    new Phase()
      .setOffset(BigInteger.valueOf(14))
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new Phase()
      .setIdeaId(BigInteger.valueOf(9812))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    PhaseRecord record = new PhaseRecord();
    record.setId(ULong.valueOf(12));
    record.setIdeaId(ULong.valueOf(9812));
    record.setName("Mic Check One Two");
    record.setKey("D major");
    record.setTotal(UInteger.valueOf(64));
    record.setOffset(ULong.valueOf(14));
    record.setDensity(0.6);
    record.setTempo(140.5);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Phase result = new Phase()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(9812), result.getIdeaId());
    assertEquals("Mic Check One Two", result.getName());
    assertEquals("D major", result.getKey());
    assertEquals(UInteger.valueOf(64), result.getTotal());
    assertEquals(ULong.valueOf(14), result.getOffset());
    assertEquals(Double.valueOf(0.6), result.getDensity());
    assertEquals(Double.valueOf(140.5), result.getTempo());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Phase().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Phase()
      .setIdeaId(BigInteger.valueOf(9812))
      .setName("Mic Check One Two")
      .setKey("D major")
      .setTotal(64)
      .setOffset(BigInteger.valueOf(14))
      .setDensity(0.6)
      .setTempo(140.5)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(9812), result.get(PHASE.IDEA_ID));
    assertEquals("Mic Check One Two", result.get(PHASE.NAME));
    assertEquals("D major", result.get(PHASE.KEY));
    assertEquals(UInteger.valueOf(64), result.get(PHASE.TOTAL));
    assertEquals(ULong.valueOf(14), result.get(PHASE.OFFSET));
    assertEquals(0.6, result.get(PHASE.DENSITY));
    assertEquals(140.5, result.get(PHASE.TEMPO));
  }

}
