// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.ChainConfigRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.CHAIN_CONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainConfigTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("OutputChannels")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new ChainConfig()
      .setType("OutputChannels")
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
    failure.expectMessage("'jello' is not a valid type");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("jello")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutValue() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Value is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("OutputChannels")
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChainConfigRecord record = new ChainConfigRecord();
    record.setId(ULong.valueOf(12));
    record.setChainId(ULong.valueOf(974));
    record.setType(ChainConfigType.OutputChannels.toString());
    record.setValue(String.valueOf(4));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    ChainConfig result = new ChainConfig()
      .setFromRecord(record);

    assertNotNull(result);
    Assert.assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(974), result.getChainId());
    assertEquals(ChainConfigType.OutputChannels, result.getType());
    assertEquals(String.valueOf(4), result.getValue());
    Assert.assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    Assert.assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new ChainConfig().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    ChainConfig chainConfig = new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("OutputChannels")
      .setValue(String.valueOf(4));
    chainConfig.validate();
    Map<Field, Object> result = chainConfig.updatableFieldValueMap();

    assertEquals(ULong.valueOf(974), result.get(CHAIN_CONFIG.CHAIN_ID));
    assertEquals(ChainConfigType.OutputChannels, result.get(CHAIN_CONFIG.TYPE));
    assertEquals(String.valueOf(4), result.get(CHAIN_CONFIG.VALUE));
  }

}
