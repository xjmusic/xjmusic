// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
      .setState("Crafted")
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
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setOffset(BigInteger.valueOf(473))
      .setState("Crafted")
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
      .setState("Crafted")
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
      .setState("Crafted")
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
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("State is required");

    Link link = new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0);
    link.validate();
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
  public void validate_okWithSetEnumState() throws Exception {
    Link link = new Link()
      .setChainId(BigInteger.valueOf(180923))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473))
      .setKey("G minor")
      .setTempo(121.0);

    link.setStateEnum(LinkState.Crafting);
    link.validate();
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
    assertEquals(BigInteger.valueOf(0),
      new Link().setOffset(BigInteger.valueOf(1)).getPreviousOffset());
    assertEquals(BigInteger.valueOf(234),
      new Link().setOffset(BigInteger.valueOf(235)).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialLink() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Cannot get previous id of initial Link");

    new Link().setOffset(BigInteger.valueOf(0)).getPreviousOffset();
  }

}
