// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.pick;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.PickRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PickTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Arrangement ID is required");

    new Pick()
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_withoutMorphID() throws Exception {
    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Audio ID is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutStart() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Start is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutLength() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Length is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAmplitude() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Amplitude is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setPitch(42.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPitch() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pitch is required");

    new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    PickRecord record = new PickRecord();
    record.setId(ULong.valueOf(12));
    record.setArrangementId(ULong.valueOf(1269));
    record.setAudioId(ULong.valueOf(6329));
    record.setStart(0.92);
    record.setLength(2.7);
    record.setAmplitude(0.84);
    record.setPitch(42.9);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Pick result = new Pick()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(1269), result.getArrangementId());
    assertEquals(null, result.getMorphId());
    assertEquals(ULong.valueOf(6329), result.getAudioId());
    assertEquals(Double.valueOf(0.92), result.getStart());
    assertEquals(Double.valueOf(2.7), result.getLength());
    assertEquals(Double.valueOf(0.84), result.getAmplitude());
    assertEquals(Double.valueOf(42.9), result.getPitch());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Pick().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Pick()
      .setArrangementId(BigInteger.valueOf(1269))
      .setMorphId(BigInteger.valueOf(6945))
      .setAudioId(BigInteger.valueOf(6329))
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(1269), result.get(PICK.ARRANGEMENT_ID));
    assertEquals(ULong.valueOf(6329), result.get(PICK.AUDIO_ID));
    assertEquals(0.92, result.get(PICK.START));
    assertEquals(2.7, result.get(PICK.LENGTH));
    assertEquals(0.84, result.get(PICK.AMPLITUDE));
    assertEquals(42.9, result.get(PICK.PITCH));
  }

}
