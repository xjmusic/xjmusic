// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.voice;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.VoiceRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class VoiceTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Voice()
      .setPhaseId(BigInteger.valueOf(251))
      .setType(Voice.HARMONIC)
      .setDescription("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase ID is required");

    new Voice()
      .setType(Voice.HARMONIC)
      .setDescription("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Voice()
      .setPhaseId(BigInteger.valueOf(251))
      .setDescription("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'chimney' is not a valid type");

    new Voice()
      .setPhaseId(BigInteger.valueOf(251))
      .setType("chimney")
      .setDescription("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutDescription() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Description is required");

    new Voice()
      .setPhaseId(BigInteger.valueOf(251))
      .setType(Voice.HARMONIC)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    VoiceRecord record = new VoiceRecord();
    record.setId(ULong.valueOf(12));
    record.setPhaseId(ULong.valueOf(251));
    record.setType(Voice.HARMONIC);
    record.setDescription("Mic Check One Two");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Voice result = new Voice()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(251), result.getPhaseId());
    assertEquals(Voice.HARMONIC, result.getType());
    assertEquals("Mic Check One Two", result.getDescription());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Voice().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Voice()
      .setPhaseId(BigInteger.valueOf(251))
      .setType(Voice.HARMONIC)
      .setDescription("Mic Check One Two")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(251), result.get(VOICE.PHASE_ID));
    assertEquals(Voice.HARMONIC, result.get(VOICE.TYPE));
    assertEquals("Mic Check One Two", result.get(VOICE.DESCRIPTION));
  }

}
