// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class ChainTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validateProductionChain() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validatePreviewChain_timesNotRequired() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Preview")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("2015-08-12 12:17:02.527142")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToDraftState() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
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
      .setAccountId(BigInteger.valueOf(9743L))
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithInvalidStopTime() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Stop-at is not isValid");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12 12:17:02.527142")
      .setStopAt("totally illegitimate expression of time")
      .validate();
  }

}
