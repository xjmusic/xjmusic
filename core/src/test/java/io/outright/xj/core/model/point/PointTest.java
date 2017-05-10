// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.point;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.PointRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.POINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PointTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutMorphID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Morph ID is required");

    new Point()
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceEventID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("VoiceEvent ID is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    PointRecord record = new PointRecord();
    record.setId(ULong.valueOf(12));
    record.setMorphId(ULong.valueOf(457832));
    record.setVoiceEventId(ULong.valueOf(76943));
    record.setPosition(1.25);
    record.setNote("F");
    record.setDuration(0.74);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Point result = new Point()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(457832), result.getMorphId());
    assertEquals(ULong.valueOf(76943), result.getVoiceEventId());
    assertEquals(Double.valueOf(1.25), result.getPosition());
    assertEquals("F", result.getNote());
    assertEquals(Double.valueOf(0.74), result.getDuration());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Point().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(457832), result.get(POINT.MORPH_ID));
    assertEquals(ULong.valueOf(76943), result.get(POINT.VOICE_EVENT_ID));
    assertEquals(1.25, result.get(POINT.POSITION));
    assertEquals("F", result.get(POINT.NOTE));
    assertEquals(0.74, result.get(POINT.DURATION));
  }

}
