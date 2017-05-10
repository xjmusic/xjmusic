// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_instrument;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.ChainInstrumentRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainInstrumentTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainInstrument()
      .setInstrumentId(BigInteger.valueOf(125434))
      .setChainId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new ChainInstrument()
      .setInstrumentId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    new ChainInstrument()
      .setChainId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChainInstrumentRecord record = new ChainInstrumentRecord();
    record.setId(ULong.valueOf(12));
    record.setChainId(ULong.valueOf(6));
    record.setInstrumentId(ULong.valueOf(87));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    ChainInstrument result = new ChainInstrument()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(6), result.getChainId());
    assertEquals(ULong.valueOf(87), result.getInstrumentId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new ChainInstrument().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new ChainInstrument()
      .setChainId(BigInteger.valueOf(6))
      .setInstrumentId(BigInteger.valueOf(87))
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(6), result.get(CHAIN_INSTRUMENT.CHAIN_ID));
    assertEquals(ULong.valueOf(87), result.get(CHAIN_INSTRUMENT.INSTRUMENT_ID));
  }

}
