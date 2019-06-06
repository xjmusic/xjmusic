// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.CoreException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChainTest {

  private static final long MATCH_THRESHOLD_MILLIS = 100;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Instant now;

  @Before
  public void setUp() {
    now = Instant.now();
  }

  @Test
  public void validateProductionChain() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_stopAtNotRequired() throws Exception {
    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
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
    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToPreviewWithoutType() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z");

    chain.validate();
    assertEquals(ChainType.Preview, chain.getType());
  }

  @Test
  public void validateProductionChain_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'bungle' is not a valid type");

    new Chain()
      .setName("Mic Check One Two")
      .setType("bungle")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToDraftState() throws Exception {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z");

    chain.validate();

    assertEquals(ChainState.Draft, chain.getState());
  }

  @Test
  public void validateProductionChain_failsWithInvalidState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'dangling' is not a valid state");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("dangling")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new Chain()
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutStartAt() throws Exception {
    failure.expect(CoreException.class);
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
    failure.expect(CoreException.class);
    failure.expectMessage("Stop-at is not isValid");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("totally illegitimate expression of time")
      .validate();
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   */
  @Test
  public void reviveChain() throws Exception {
    Chain result = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Fabricate")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .revived();

    assertNull("no id", result.getId());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   */
  @Test
  public void reviveChain_failsIfNotFabricateState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Fabricate-state Chain");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Ready")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .revived();
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   */
  @Test
  public void reviveChain_failsIfNotProductionType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Production-type Chain");

    new Chain()
      .setName("Mic Check One Two")
      .setType("Preview")
      .setState("Fabricate")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .revived();
  }

  @Test
  public void create_okToSetNullTimestamp() throws Exception {
    new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setEmbedKey("my $% favorite THINGS")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt(null)
      .validate();
  }

  /**
   [#150279615] For time field in chain specification, enter "now" for current timestamp utc
   */
  @Test
  public void now() throws CoreException {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("now");
    chain.validate();
    Instant actual = chain.getStartAt();

    assertThat("now", MATCH_THRESHOLD_MILLIS > Duration.between(actual, now).toMillis());
  }

  /**
   [#150279615] For time field in chain specification, enter "now" for current timestamp utc
   */
  @Test
  public void valueOf_now() throws CoreException {
    Chain chain = new Chain()
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("   NO!w!!    ");
    chain.validate();
    Instant actual = chain.getStartAt();

    assertThat("value of string 'now' (stripped of non-alphanumeric characters; case insensitive)", MATCH_THRESHOLD_MILLIS > Duration.between(actual, now).toMillis());
  }

}
