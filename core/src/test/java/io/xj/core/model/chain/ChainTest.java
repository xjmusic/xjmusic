// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.ChainRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.CHAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validateProductionChain() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_stopAtNotRequired() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validatePreviewChain_timesNotRequired() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Preview")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutAccountID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToPreviewWithoutType() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142");

    chain.validate();
    assertEquals(ChainType.Preview, chain.getType());
  }

  @Test
  public void validateProductionChain_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'bungle' is not a valid type");

    new Chain()
      .setName("Mic Check One Two")
      .setType("bungle")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToDraftState() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142");

    chain.validate();

    assertEquals(ChainState.Draft, chain.getState());
  }

  @Test
  public void validateProductionChain_failsWithInvalidState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'dangling' is not a valid state");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("dangling")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new Chain()
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutStartAt() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Start-at is required");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithInvalidStopTime() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Stop-at is not valid");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("totally illegitimate expression of time")
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChainRecord record = new ChainRecord();
    record.setId(ULong.valueOf(12));
    record.setName("Mic Check One Two");
    record.setType("Production");
    record.setState("Draft");
    record.setAccountId(ULong.valueOf(9743));
    record.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setStopAt(Timestamp.valueOf("2015-08-12 12:17:02.527142"));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Chain result = new Chain()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals("Mic Check One Two", result.getName());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ULong.valueOf(9743), result.getAccountId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-08-12 12:17:02.527142"), result.getStopAt());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Chain().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142");
    chain.validate();
    Map<Field, Object> result = chain.updatableFieldValueMap();

    assertEquals("Mic Check One Two", result.get(CHAIN.NAME));
    assertEquals(ChainType.Production, result.get(CHAIN.TYPE));
    assertEquals(ChainState.Draft, result.get(CHAIN.STATE));
    assertEquals(ULong.valueOf(9743), result.get(CHAIN.ACCOUNT_ID));
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.get(CHAIN.START_AT));
    assertEquals(Timestamp.valueOf("2015-08-12 12:17:02.527142"), result.get(CHAIN.STOP_AT));
  }

}
