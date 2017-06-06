// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.AudioChordRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.AUDIO_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AudioChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new AudioChord()
      .setName("C# minor")
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new AudioChord()
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Audio ID is required");

    new AudioChord()
      .setName("C# minor")
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new AudioChord()
      .setName("C# minor")
      .setAudioId(BigInteger.valueOf(1235))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    AudioChordRecord record = new AudioChordRecord();
    record.setId(ULong.valueOf(12));
    record.setName("C# minor");
    record.setAudioId(ULong.valueOf(1235));
    record.setPosition(0.75);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    AudioChord result = new AudioChord()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals("C# minor", result.getName());
    assertEquals(ULong.valueOf(1235), result.getAudioId());
    assertEquals(Double.valueOf(0.75), result.getPosition());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new AudioChord().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new AudioChord()
      .setName("C# minor")
      .setAudioId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .updatableFieldValueMap();

    assertEquals("C# minor", result.get(AUDIO_CHORD.NAME));
    assertEquals(ULong.valueOf(1235), result.get(AUDIO_CHORD.AUDIO_ID));
    assertEquals(0.75, result.get(AUDIO_CHORD.POSITION));
  }

}
