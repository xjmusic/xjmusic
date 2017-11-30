// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.AudioEventRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.AUDIO_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AudioEventTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Audio ID is required");

    new AudioEvent()
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
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

    new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .validate();
  }


  @Test
  public void setFromRecord() throws Exception {
    AudioEventRecord record = new AudioEventRecord();
    record.setId(ULong.valueOf(12));
    record.setAudioId(ULong.valueOf(1235));
    record.setPosition(0.75);
    record.setDuration(3.45);
    record.setInflection("SMACK");
    record.setNote("D6");
    record.setTonality(0.6);
    record.setVelocity(0.9);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    AudioEvent result = new AudioEvent()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(1235), result.getAudioId());
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
    assertNull(new AudioEvent().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new AudioEvent()
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .setDuration(3.45)
      .setInflection("SMACK")
      .setNote("D6")
      .setTonality(0.6)
      .setVelocity(0.9)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(1235), result.get(AUDIO_EVENT.AUDIO_ID));
    assertEquals(0.75, result.get(AUDIO_EVENT.POSITION));
    assertEquals(3.45, result.get(AUDIO_EVENT.DURATION));
    assertEquals("SMACK", result.get(AUDIO_EVENT.INFLECTION));
    assertEquals("D6", result.get(AUDIO_EVENT.NOTE));
    assertEquals(0.6, result.get(AUDIO_EVENT.TONALITY));
    assertEquals(0.9, result.get(AUDIO_EVENT.VELOCITY));

  }

}
