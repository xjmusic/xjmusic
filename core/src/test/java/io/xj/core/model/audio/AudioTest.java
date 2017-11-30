// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.AudioRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.AUDIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AudioTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Audio()
      .setInstrumentId(BigInteger.valueOf(53))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new Audio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53))
      .setLength(3.4)
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Tempo is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutRootPitch() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Root Pitch is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    AudioRecord record = new AudioRecord();
    record.setId(ULong.valueOf(12));
    record.setInstrumentId(ULong.valueOf(53));
    record.setLength(3.4);
    record.setName("Mic Check One Two");
    record.setState("Published");
    record.setPitch(57.4);
    record.setStart(0.212);
    record.setTempo(120.0);
    record.setWaveformKey("instument-audio-1234543");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Audio result = new Audio()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(53), result.getInstrumentId());
    assertEquals(Double.valueOf(3.4), result.getLength());
    assertEquals("Mic Check One Two", result.getName());
    assertEquals(Double.valueOf(57.4), result.getPitch());
    assertEquals(Double.valueOf(0.212), result.getStart());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals("instument-audio-1234543", result.getWaveformKey());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Audio().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Audio()
      .setInstrumentId(BigInteger.valueOf(53))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(53), result.get(AUDIO.INSTRUMENT_ID));
    assertEquals(3.4, result.get(AUDIO.LENGTH));
    assertEquals("Mic Check One Two", result.get(AUDIO.NAME));
    assertEquals(57.4, result.get(AUDIO.PITCH));
    assertEquals(0.212, result.get(AUDIO.START));
    assertEquals(120.0, result.get(AUDIO.TEMPO));
  }

}
