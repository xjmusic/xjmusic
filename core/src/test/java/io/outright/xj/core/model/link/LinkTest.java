// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.LinkRecord;

import org.jooq.Field;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LinkTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_setAsTimestamps() throws Exception {
    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAtTimestamp(Timestamp.valueOf("2014-08-12 12:17:02.527142"))
      .setEndAtTimestamp(Timestamp.valueOf("2014-09-12 12:17:34.262679"))
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setOffset(BigInteger.valueOf(473))
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new Link()
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_failsWithoutBeginAt() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Begin-at is required");

    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("State is required");

    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'pensive' is not a valid state");

    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("pensive")
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    LinkRecord record = new LinkRecord();
    record.setId(ULong.valueOf(12));
    record.setChainId(ULong.valueOf(180923));
    record.setBeginAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setEndAt(Timestamp.valueOf("2014-09-12 12:17:34.262679"));
    record.setTotal(UInteger.valueOf(64));
    record.setDensity(0.754);
    record.setOffset(ULong.valueOf(473));
    record.setKey("G minor");
    record.setTempo(121.0);
    record.setState(Link.CRAFTED);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Link result = new Link()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(180923), result.getChainId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:34.262679"), result.getEndAt());
    assertEquals(UInteger.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.754), result.getDensity());
    assertEquals(ULong.valueOf(473), result.getOffset());
    assertEquals("G minor", result.getKey());
    assertEquals(Double.valueOf(121.0), result.getTempo());
    assertEquals(Link.CRAFTED, result.getState());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Link().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0)
      .setState(Link.CRAFTED)
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(180923), result.get(LINK.CHAIN_ID));
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.get(LINK.BEGIN_AT));
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:34.262679"), result.get(LINK.END_AT));
    assertEquals(UInteger.valueOf(64), result.get(LINK.TOTAL));
    assertEquals(0.754, result.get(LINK.DENSITY));
    assertEquals(ULong.valueOf(473), result.get(LINK.OFFSET));
    assertEquals("G minor", result.get(LINK.KEY));
    assertEquals(121.0, result.get(LINK.TEMPO));
    assertEquals(Link.CRAFTED, result.get(LINK.STATE));
  }

  @Test
  public void isInitial() throws Exception {
    assertTrue(new Link()
      .setOffset(BigInteger.valueOf(0))
      .isInitial());
    assertFalse(new Link()
      .setOffset(BigInteger.valueOf(1))
      .isInitial());
    assertFalse(new Link()
      .setOffset(BigInteger.valueOf(981))
      .isInitial());
  }

  @Test
  public void getPreviousOffset() throws Exception {
    assertEquals(ULong.valueOf(0),
      new Link().setOffset(BigInteger.valueOf(1)).getPreviousOffset());
    assertEquals(ULong.valueOf(234),
      new Link().setOffset(BigInteger.valueOf(235)).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialLink() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Cannot get previous id of initial Link");

    new Link().setOffset(BigInteger.valueOf(0)).getPreviousOffset();
  }

}
