// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ChainTest {

  private static final long MATCH_THRESHOLD_MILLIS = 100;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Instant now;
  private Chain subject;

  @Before
  public void setUp() {
    now = Instant.now();
    subject = Chain.create();
  }

  @Test
  public void validateProductionChain() throws Exception {
    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_stopAtNotRequired() throws Exception {
    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validatePreviewChain_stopAtIsRequired() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Stop-at time (for non-production chain) is required.");

    subject
      .setName("Mic Check One Two")
      .setType("Preview")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutAccountID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToPreviewWithoutType() throws Exception {
    Chain chain = subject
      .setName("Mic Check One Two")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z");

    chain.validate();
    assertEquals(ChainType.Preview, chain.getType());
  }

  @Test
  public void validateProductionChain_failsWithInvalidType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("'bungle' is not a valid type");

    subject
      .setName("Mic Check One Two")
      .setType("bungle")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_defaultsToDraftState() throws Exception {
    Chain chain = subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z");

    chain.validate();

    assertEquals(ChainState.Draft, chain.getState());
  }

  @Test
  public void validateProductionChain_failsWithInvalidState() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("'dangling' is not a valid state");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("dangling")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    subject
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("2015-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutStartAt() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Start-at time is required.");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithInvalidStopTime() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Stop-at is invalid because Text 'totally illegitimate expression create time' could not be parsed at index 0");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("totally illegitimate expression create time")
      .validate();
  }

  @Test
  public void create_okToSetNullTimestamp() throws Exception {
    subject
      .setAccountId(UUID.randomUUID())
      .setName("coconuts")
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
  public void startsNow() throws ValueException {
    Chain chain = subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("now");
    chain.validate();
    Instant actual = chain.getStartAt();

    assertThat("now", MATCH_THRESHOLD_MILLIS > Duration.between(actual, now).toMillis());
  }

  /**
   [#150279615] For time field in chain specification, enter "now" for current timestamp utc
   */
  @Test
  public void valueOf_now() throws ValueException {
    Chain chain = subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(UUID.randomUUID())
      .setStartAt("   NO!w!!    ");
    chain.validate();
    Instant actual = chain.getStartAt();

    assertThat("value create string 'now' (stripped create non-alphanumeric characters; case insensitive)", MATCH_THRESHOLD_MILLIS > Duration.between(actual, now).toMillis());
  }
}
