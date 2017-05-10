// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.morph;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.MorphRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.MORPH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MorphTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Arrangement ID is required");

    new Morph()
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setDuration(1.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setNote("G5")
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setDuration(1.5)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    MorphRecord record = new MorphRecord();
    record.setId(ULong.valueOf(12));
    record.setArrangementId(ULong.valueOf(987));
    record.setPosition(3.5);
    record.setDuration(1.5);
    record.setNote("G5");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Morph result = new Morph()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(987), result.getArrangementId());
    assertEquals(Double.valueOf(3.5), result.getPosition());
    assertEquals(Double.valueOf(1.5), result.getDuration());
    assertEquals("G5", result.getNote());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Morph().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Morph()
      .setArrangementId(BigInteger.valueOf(987))
      .setPosition(3.5)
      .setDuration(1.5)
      .setNote("G5")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(987), result.get(MORPH.ARRANGEMENT_ID));
    assertEquals(3.5, result.get(MORPH.POSITION));
    assertEquals(1.5, result.get(MORPH.DURATION));
    assertEquals("G5", result.get(MORPH.NOTE));
  }

}
