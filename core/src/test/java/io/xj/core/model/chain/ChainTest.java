// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonNull;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
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
    failure.expect(CoreException.class);
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
      .setAccountId(UUID.randomUUID())
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
    failure.expect(CoreException.class);
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
    failure.expect(CoreException.class);
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
    failure.expect(CoreException.class);
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
    failure.expect(CoreException.class);
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
  public void startsNow() throws CoreException {
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
  public void valueOf_now() throws CoreException {
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

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name", "state", "type", "startAt", "stopAt", "embedKey"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    payloadObject
      .setId(id.toString())
      .setType("chains")
      .setAttributes(ImmutableMap.<String, Object>builder()
        .put("name", "Cool Ambience")
        .put("state", "Draft")
        .put("type", "Production")
        .put("startAt", "2014-08-12T12:17:02.527142Z")
        .put("stopAt", "")
        .put("embedKey", "coolambience")
        .build())
      .add("account", Payload.referenceTo("accounts", accountId.toString()));

    subject.consume(payloadObject);

    assertEquals(id, subject.getId());
    assertEquals(accountId, subject.getAccountId());
    assertEquals("Cool Ambience", subject.getName());
    assertEquals(ChainState.Draft, subject.getState());
    assertEquals(ChainType.Production, subject.getType());
    assertEquals(Instant.parse("2014-08-12T12:17:02.527142Z"), subject.getStartAt());
    assertNull(subject.getStopAt());
    assertEquals("coolambience", subject.getEmbedKey());
  }

  @Test
  public void toPayloadObject() {
    Chain chain = Chain.create()
      .setAccountId(UUID.randomUUID())
      .setName("Cool Ambience")
      .setStateEnum(ChainState.Draft)
      .setTypeEnum(ChainType.Production)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setEmbedKey("coolambience");

    PayloadObject result = chain.toPayloadObject();

    assertEquals(chain.getId().toString(), result.getId());
    assertEquals("chains", result.getType());
    assertEquals("Cool Ambience", result.getAttributes().get("name"));
    assertEquals(ChainState.Draft, result.getAttributes().get("state"));
    assertEquals(ChainType.Production, result.getAttributes().get("type"));
    assertEquals(Instant.parse("2014-08-12T12:17:02.527142Z"), result.getAttributes().get("startAt"));
    assertEquals(JsonNull.INSTANCE, result.getAttributes().get("stopAt"));
    assertEquals("coolambience", result.getAttributes().get("embedKey"));
    assertTrue(result.getRelationships().get("account").getDataOne().isPresent());
    assertEquals(chain.getAccountId().toString(), result.getRelationships().get("account").getDataOne().get().getId());
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
  FUTURE adapt these Chain unit tests (of legacy integration tests)

  @Test
  public void getConfig() throws CoreException {
    Chain chain = of();
    ChainConfig config = of(of(), ChainConfigType.OutputChannels, "4");

    assertNotNull(chain.getConfig(config.getId()));
  }

  @Test
  public void getBinding() throws CoreException {
    Chain chain = chainFactory.of(BigInteger.valueOf(72));
    ChainBinding binding = chain.add(of("Library", 7));

    assertNotNull(chain.getBinding(binding.getId()));
  }

    @Test
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputFrameRate")
      .setValue("3,4,74");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfAlreadyExists() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInChainAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutChainID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutLibraryId() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainConfig inputData = new ChainConfig()
      .setChainId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    ChainConfig result = testDAO.readOne(access, BigInteger.valueOf(1000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(ChainConfigType.OutputSampleBits, result.getType());
    assertEquals("1", result.getValue());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<ChainConfig> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<ChainConfig> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

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
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfAlreadyExists() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInChainAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInInstrumentAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutChainID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutInstrumentId() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    ChainInstrument result = testDAO.readOne(access, BigInteger.valueOf(1003000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<ChainInstrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<ChainInstrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

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
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfAlreadyExists() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInChainAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutChainID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutLibraryId() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    ChainLibrary result = testDAO.readOne(access, BigInteger.valueOf(1001000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<ChainLibrary> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<ChainLibrary> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

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
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setChainId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfAlreadyExists() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setChainId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInChainAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setChainId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailIfUserNotInSequenceAccount() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setChainId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutChainID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutSequenceId() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ChainSequence inputData = new ChainSequence()
      .setChainId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    ChainSequence result = testDAO.readOne(access, BigInteger.valueOf(1003000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getProgramSequenceId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<ChainSequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<ChainSequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

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
