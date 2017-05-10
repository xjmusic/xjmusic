// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice_event;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.VoiceEventRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.VOICE_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class VoiceEventTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Voice ID is required");

    new VoiceEvent()
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutInflection() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Inflection is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutTonality() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Tonality is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVelocity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Velocity is required");

    new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .validate();
  }


  @Test
  public void setFromRecord() throws Exception {
    VoiceEventRecord record = new VoiceEventRecord();
    record.setId(ULong.valueOf(12));
    record.setVoiceId(ULong.valueOf(1235));
    record.setPosition(0.75);
    record.setDuration(3.45);
    record.setInflection("SMACK");
    record.setNote("D6");
    record.setTonality(0.6);
    record.setVelocity(0.9);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    VoiceEvent result = new VoiceEvent()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(1235), result.getVoiceId());
    assertEquals(Double.valueOf(0.75), result.getPosition());
    assertEquals(Double.valueOf(3.45), result.getDuration());
    assertEquals("SMACK", result.getInflection());
    assertEquals("D6", result.getNote());
    assertEquals(Double.valueOf(0.6), result.getTonality());
    assertEquals(Double.valueOf(0.9), result.getVelocity());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new VoiceEvent().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new VoiceEvent()
      .setVoiceId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(1235), result.get(VOICE_EVENT.VOICE_ID));
    assertEquals(0.75, result.get(VOICE_EVENT.POSITION));
    assertEquals(3.45, result.get(VOICE_EVENT.DURATION));
    assertEquals("SMACK", result.get(VOICE_EVENT.INFLECTION));
    assertEquals("D6", result.get(VOICE_EVENT.NOTE));
    assertEquals(0.6, result.get(VOICE_EVENT.TONALITY));
    assertEquals(0.9, result.get(VOICE_EVENT.VELOCITY));

  }

}
