// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.Tables;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.CancelException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.message.MessageType;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.transport.JSON;

import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.LINK_CHORD;
import static io.xj.core.Tables.LINK_MEME;
import static io.xj.core.Tables.LINK_MESSAGE;
import static io.xj.core.Tables.PICK;
import static io.xj.core.tables.Link.LINK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private LinkDAO testDAO;
  @Mock AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("link.file.bucket", "xj-link-test");

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
    injector = null;

    System.clearProperty("link.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState("Planned")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(5), result.get("offset"));
    assertEquals(LinkState.Planned, result.get("state"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.get("beginAt"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.get("endAt"));
    assertEquals(UInteger.valueOf(64), result.get("total"));
    assertEquals(0.74, result.get("density"));
    assertEquals("C# minor 7 b9", result.get("key"));
    assertEquals(120.0, result.get("tempo"));
    assertNotNull(result.get("waveformKey"));
  }

  @Test
  // [#126] Links are always readMany in PLANNED state
  public void create_alwaysInPlannedState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(5), result.get("offset"));
    assertEquals(LinkState.Planned, result.get("state"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.get("beginAt"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.get("endAt"));
    assertEquals(UInteger.valueOf(64), result.get("total"));
    assertEquals(0.74, result.get("density"));
    assertEquals("C# minor 7 b9", result.get("key"));
    assertEquals(120.0, result.get("tempo"));
    assertNotNull(result.get("waveformKey"));
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setOffset(BigInteger.valueOf(4))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("mushamush")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("'mushamush' is not a valid state");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Link result = new Link().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals(ULong.valueOf(1), result.getOffset());
    assertEquals(LinkState.Dubbing, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:32.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("2017-02-14 12:02:04.000001"), result.getEndAt());
    assertEquals(UInteger.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    LinkRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(5, result.length());

    JSONObject result4 = (JSONObject) result.get(4);
    assertEquals("Dubbed", result4.get("state"));
    JSONObject result3 = (JSONObject) result.get(3);
    assertEquals("Dubbing", result3.get("state"));
    JSONObject result2 = (JSONObject) result.get(2);
    assertEquals("Crafted", result2.get("state"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals("Crafting", result1.get("state"));
    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals("Planned", actualResult0.get("state"));
  }

  // TODO read all from seconds UTC

  @Test
  public void readOneInState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    LinkRecord result = testDAO.readOneInState(access, ULong.valueOf(1), LinkState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(5), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get(LINK.CHAIN_ID));
    assertEquals(ULong.valueOf(4), result.get("offset"));
    assertEquals("Planned", result.get("state"));
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:08.000001"), result.get(LINK.BEGIN_AT));
    assertNull(result.get(LINK.END_AT));
  }

  @Test
  public void readOneInState_nullIfNoneInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(2, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    LinkRecord result = testDAO.readOneInState(access, ULong.valueOf(2), LinkState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNull(result);
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState("Dubbed")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    testDAO.update(access, ULong.valueOf(2), inputData);

    LinkRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals("Dubbed", result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.updateState(access, ULong.valueOf(2), LinkState.Crafting);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setOffset(BigInteger.valueOf(4))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test
  public void update_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("what a dumb-ass state")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("'what a dumb-ass state' is not a valid state");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(12))
      .setOffset(BigInteger.valueOf(4))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    try {
      testDAO.update(access, ULong.valueOf(2), inputData);

    } catch (Exception e) {
      LinkRecord result = IntegrationTestService.getDb()
        .selectFrom(LINK)
        .where(LINK.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(ULong.valueOf(1), result.getChainId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    IntegrationTestService.getDb().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();

    testDAO.destroy(Access.internal(), ULong.valueOf(1));

    LinkRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void destroy_succeedsEvenIfLinkHasNullWaveformKey() throws Exception {
    IntegrationTestService.getDb().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();
    IntegrationTestService.getDb().update(LINK)
      .set(LINK.WAVEFORM_KEY, DSL.value((String) null))
      .execute();

    testDAO.destroy(Access.internal(), ULong.valueOf(1));

    verify(amazonProvider, never()).deleteS3Object("xj-link-test", null);

    LinkRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.destroy(access, ULong.valueOf(1));
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    // User "bill"
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertVoiceEvent(1, 8, 0, 1, "KICK", "C", 0.8, 1.0);

    // Library has Instrument with Audio
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Published", "Kick", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(3, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(17, 3, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.mp3");

    // Link Meme
    IntegrationTestEntity.insertLinkMeme(25, 17, "Jams");

    // Link Chord
    IntegrationTestEntity.insertLinkChord(25, 17, 0, "D major 7 b9");

    // Link Message
    IntegrationTestEntity.insertLinkMessage(25, 17, MessageType.Warning, "Consider yourself warned");

    // Choice
    IntegrationTestEntity.insertChoice(1, 17, 1, IdeaType.Macro, 2, -5);

    // Arrangement
    IntegrationTestEntity.insertArrangement(1, 1, 8, 9);

    // Pick is in Morph
    IntegrationTestEntity.insertPick(1, 1, 1, 0.125, 1.23, 0.94, 440);

    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    //
    // Go!
    testDAO.destroy(access, ULong.valueOf(17));
    //
    //

    // [#263] expect request to delete link waveform from Amazon S3
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-97898asdf7892.mp3");

    // Assert destroyed Link
    assertNull(IntegrationTestService.getDb()
      .selectFrom(Tables.LINK)
      .where(Tables.LINK.ID.eq(ULong.valueOf(17)))
      .fetchOne());

    // Assert destroyed Link Meme
    assertNull(IntegrationTestService.getDb()
      .selectFrom(LINK_MEME)
      .where(LINK_MEME.ID.eq(ULong.valueOf(25)))
      .fetchOne());

    // Assert destroyed Link Chord
    assertNull(IntegrationTestService.getDb()
      .selectFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(ULong.valueOf(25)))
      .fetchOne());

    // Assert destroyed Link Message
    assertNull(IntegrationTestService.getDb()
      .selectFrom(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(ULong.valueOf(25)))
      .fetchOne());

    // Assert destroyed Arrangement
    assertNull(IntegrationTestService.getDb()
      .selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Choice
    assertNull(IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Pick
    assertNull(IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.ID.eq(ULong.valueOf(1)))
      .fetchOne());

  }


}
