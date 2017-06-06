// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain_idea;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.tables.records.ChainIdeaRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.CHAIN_IDEA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainIdeaTest {
  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainIdea()
      .setIdeaId(BigInteger.valueOf(125434))
      .setChainId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new ChainIdea()
      .setIdeaId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void validate_failsWithoutIdeaId() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Idea ID is required");

    new ChainIdea()
      .setChainId(BigInteger.valueOf(125434))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChainIdeaRecord record = new ChainIdeaRecord();
    record.setId(ULong.valueOf(12));
    record.setChainId(ULong.valueOf(6));
    record.setIdeaId(ULong.valueOf(87));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    ChainIdea result = new ChainIdea()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(6), result.getChainId());
    assertEquals(ULong.valueOf(87), result.getIdeaId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new ChainIdea().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new ChainIdea()
      .setChainId(BigInteger.valueOf(6))
      .setIdeaId(BigInteger.valueOf(87))
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(6), result.get(CHAIN_IDEA.CHAIN_ID));
    assertEquals(ULong.valueOf(87), result.get(CHAIN_IDEA.IDEA_ID));
  }

}
