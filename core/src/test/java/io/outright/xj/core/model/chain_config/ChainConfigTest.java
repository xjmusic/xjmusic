// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_config;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.ChainConfigRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_CONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainConfigTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType(ChainConfig.OUTPUT_CHANNELS)
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new ChainConfig()
      .setType(ChainConfig.OUTPUT_CHANNELS)
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'JIGGLE' is not a valid type");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("jiggle")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutValue() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Value is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType(ChainConfig.OUTPUT_CHANNELS)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChainConfigRecord record = new ChainConfigRecord();
    record.setId(ULong.valueOf(12));
    record.setChainId(ULong.valueOf(974));
    record.setType(ChainConfig.OUTPUT_CHANNELS);
    record.setValue(String.valueOf(4));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    ChainConfig result = new ChainConfig()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(974), result.getChainId());
    assertEquals(ChainConfig.OUTPUT_CHANNELS, result.getType());
    assertEquals(String.valueOf(4), result.getValue());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new ChainConfig().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType(ChainConfig.OUTPUT_CHANNELS)
      .setValue(String.valueOf(4))
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(974), result.get(CHAIN_CONFIG.CHAIN_ID));
    assertEquals(ChainConfig.OUTPUT_CHANNELS, result.get(CHAIN_CONFIG.TYPE));
    assertEquals(String.valueOf(4), result.get(CHAIN_CONFIG.VALUE));
  }

}
