// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonNull;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ChainTest extends CoreTest {

  private static final long MATCH_THRESHOLD_MILLIS = 100;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Instant now;
  private Chain subject;

  @Before
  public void setUp() {
    now = Instant.now();
    subject = chainFactory.newChain();
  }

  @Test
  public void validateProductionChain() throws Exception {
    subject
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
    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validatePreviewChain_stopAtIsRequired() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Stop-at time (for non-production chain) is required.");

    subject
      .setName("Mic Check One Two")
      .setType("Preview")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithoutAccountID() throws Exception {
    failure.expect(CoreException.class);
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

    subject
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
    Chain chain = subject
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

    subject
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

    subject
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
    failure.expectMessage("Start-at time is required.");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .validate();
  }

  @Test
  public void validateProductionChain_failsWithInvalidStopTime() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Stop-at is invalid because Text 'totally illegitimate expression of time' could not be parsed at index 0");

    subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt("totally illegitimate expression of time")
      .validate();
  }

  @Test
  public void create_okToSetNullTimestamp() throws Exception {
    subject
      .setAccountId(BigInteger.valueOf(1L))
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
  public void startsNow() throws CoreException {
    Chain chain = subject
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
    Chain chain = subject
      .setName("Mic Check One Two")
      .setType("Production")
      .setState("Draft")
      .setAccountId(BigInteger.valueOf(9743L))
      .setStartAt("   NO!w!!    ");
    chain.validate();
    Instant actual = chain.getStartAt();

    assertThat("value of string 'now' (stripped of non-alphanumeric characters; case insensitive)", MATCH_THRESHOLD_MILLIS > Duration.between(actual, now).toMillis());
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("createdAt", "updatedAt", "name", "state", "type", "startAt", "stopAt", "embedKey"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    payloadObject
      .setId("72")
      .setType("chains")
      .setAttributes(ImmutableMap.<String, Object>builder()
        .put("name", "Cool Ambience")
        .put("state", "Draft")
        .put("type", "Production")
        .put("startAt", "2014-08-12T12:17:02.527142Z")
        .put("stopAt", "")
        .put("embedKey", "coolambience")
        .build())
      .add("account", Payload.referenceTo("accounts", "43"));

    subject.consume(payloadObject);

    assertEquals(BigInteger.valueOf(72), subject.getId());
    assertEquals(BigInteger.valueOf(43), subject.getAccountId());
    assertEquals("Cool Ambience", subject.getName());
    assertEquals(ChainState.Draft, subject.getState());
    assertEquals(ChainType.Production, subject.getType());
    assertEquals(Instant.parse("2014-08-12T12:17:02.527142Z"), subject.getStartAt());
    assertNull(subject.getStopAt());
    assertEquals("coolambience", subject.getEmbedKey());
  }

  @Test
  public void toPayloadObject() {
    Chain chain = chainFactory.newChain(BigInteger.valueOf(72));
    chain
      .setAccountId(BigInteger.valueOf(43))
      .setName("Cool Ambience")
      .setStateEnum(ChainState.Draft)
      .setTypeEnum(ChainType.Production)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setEmbedKey("coolambience");

    PayloadObject result = chain.toPayloadObject();

    assertEquals("72", result.getId());
    assertEquals("chains", result.getType());
    assertEquals("Cool Ambience", result.getAttributes().get("name"));
    assertEquals("Draft", result.getAttributes().get("state"));
    assertEquals("Production", result.getAttributes().get("type"));
    assertEquals("2014-08-12T12:17:02.527142Z", result.getAttributes().get("startAt"));
    assertEquals(JsonNull.INSTANCE, result.getAttributes().get("stopAt"));
    assertEquals("coolambience", result.getAttributes().get("embedKey"));
    assertTrue(result.getRelationships().get("account").getDataOne().isPresent());
    assertEquals("43", result.getRelationships().get("account").getDataOne().get().getId());
  }

  @Test
  public void getConfig() throws CoreException {
    Chain chain = chainFactory.newChain(BigInteger.valueOf(72));
    ChainConfig config = chain.add(newChainConfig(ChainConfigType.OutputChannels, "4"));

    assertNotNull(chain.getConfig(config.getId()));
  }

  @Test
  public void getBinding() throws CoreException {
    Chain chain = chainFactory.newChain(BigInteger.valueOf(72));
    ChainBinding binding = chain.add(newChainBinding("Library", 7));

    assertNotNull(chain.getBinding(binding.getId()));
  }

  // FUTURE unit test: Add a ChainBinding

  // FUTURE unit test: Add a ChainConfig

  // FUTURE unit test require(accountId, "Account ID");

  // FUTURE unit test if (Objects.isNull(type) || type.toString().isEmpty())  type = ChainType.Preview;

  // FUTURE unit test if (Objects.isNull(state) || state.toString().isEmpty())  state = ChainState.Draft;

  // FUTURE unit test if (Objects.isNull(name) || name.isEmpty())    throw new CoreException("Name is required.");

  // FUTURE unit test if (Objects.nonNull(startAtException))    throw new CoreException("Invalid start-at value.", startAtException);

  // FUTURE unit test if (Objects.nonNull(stopAtException))    throw new CoreException("Invalid stop-at value.", stopAtException);

  // FUTURE unit test if (ChainType.Production != type)     if (Objects.isNull(stopAt))      throw new CoreException("Stop-at is required for non-production chains.");

  // FUTURE unit test if (ChainState.Fabricate == state)    if (bindings.isEmpty())     throw new CoreException(String.format("Chain must be bound to %s in order to enter Fabricate state.", "at least one Library, Sequence, or Instrument"));

  // FUTURE unit test if (Objects.nonNull(stateException))    throw new CoreException("Invalid state value.", stateException);

  // FUTURE unit test if (Objects.nonNull(typeException))    throw new CoreException("Invalid type value.", typeException);


  /*
  FUTURE adapt these Chain unit tests (from legacy integration tests)

    @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputFrameRate")
      .setValue("3,4,74");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(3L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ChainConfig result = testDAO.readOne(access, BigInteger.valueOf(1000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(ChainConfigType.OutputSampleBits, result.getType());
    assertEquals("1", result.getValue());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainConfig> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainConfig> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));

    assertNotExist(testDAO, BigInteger.valueOf(1000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(3L))
      .setInstrumentId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInInstrumentAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setInstrumentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutInstrumentId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    ChainInstrument result = testDAO.readOne(access, BigInteger.valueOf(1003000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainInstrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainInstrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));

    assertNotExist(testDAO, BigInteger.valueOf(1003000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setLibraryId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ChainLibrary result = testDAO.readOne(access, BigInteger.valueOf(1001000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainLibrary> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainLibrary> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1001000L));

    assertNotExist(testDAO, BigInteger.valueOf(1001000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1001000L));
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(3L))
      .setSequenceId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInSequenceAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setSequenceId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutSequenceId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    ChainSequence result = testDAO.readOne(access, BigInteger.valueOf(1003000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getSequenceId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainSequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainSequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));

    assertNotExist(testDAO, BigInteger.valueOf(1003000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));
  }

   */

}
