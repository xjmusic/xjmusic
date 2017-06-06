// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.ArrangementRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.ARRANGEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ArrangementTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setChoiceId(BigInteger.valueOf(879))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Voice ID is required");

    new Arrangement()
      .setChoiceId(BigInteger.valueOf(879))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutChoiceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Choice ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setInstrumentId(BigInteger.valueOf(432))
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setChoiceId(BigInteger.valueOf(879))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ArrangementRecord record = new ArrangementRecord();
    record.setId(ULong.valueOf(12));
    record.setVoiceId(ULong.valueOf(354));
    record.setChoiceId(ULong.valueOf(879));
    record.setInstrumentId(ULong.valueOf(432));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Arrangement result = new Arrangement()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(354), result.getVoiceId());
    assertEquals(ULong.valueOf(879), result.getChoiceId());
    assertEquals(ULong.valueOf(432), result.getInstrumentId());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Arrangement().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Arrangement()
      .setVoiceId(BigInteger.valueOf(354))
      .setChoiceId(BigInteger.valueOf(879))
      .setInstrumentId(BigInteger.valueOf(432))
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(354), result.get(ARRANGEMENT.VOICE_ID));
    assertEquals(ULong.valueOf(879), result.get(ARRANGEMENT.CHOICE_ID));
    assertEquals(ULong.valueOf(432), result.get(ARRANGEMENT.INSTRUMENT_ID));
  }

}
