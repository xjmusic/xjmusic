// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.instrument_meme;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.InstrumentMemeRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.INSTRUMENT_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class InstrumentMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(23678))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    InstrumentMemeRecord record = new InstrumentMemeRecord();
    record.setId(ULong.valueOf(12));
    record.setInstrumentId(ULong.valueOf(23678));
    record.setName("Miccheckonetwo");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    InstrumentMeme result = new InstrumentMeme()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(23678), result.getInstrumentId());
    assertEquals("Miccheckonetwo", result.getName());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new InstrumentMeme().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(23678), result.get(INSTRUMENT_MEME.INSTRUMENT_ID));
    assertEquals("Miccheckonetwo", result.get(INSTRUMENT_MEME.NAME));
  }

}
