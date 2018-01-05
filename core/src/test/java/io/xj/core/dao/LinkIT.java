// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.transport.JSON;

import org.jooq.impl.DSL;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
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
import java.util.Collection;
import java.util.Iterator;

import static io.xj.core.Tables.CHAIN;
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
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

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
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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

    Link result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(BigInteger.valueOf(5), result.getOffset());
    assertEquals(LinkState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(),0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(),0.01);
    assertNotNull(result.getWaveformKey());
  }

  @Test
  // [#126] Links are always readMany in PLANNED state
  public void create_alwaysInPlannedState() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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

    Link result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(BigInteger.valueOf(5), result.getOffset());
    assertEquals(LinkState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(),0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(),0.1);
    assertNotNull(result.getWaveformKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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
    failure.expectMessage("Found Link at same offset in Chain");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User"
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Link result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(BigInteger.valueOf(1), result.getOffset());
    assertEquals(LinkState.Dubbing, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:32.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("2017-02-14 12:02:04.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChain() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Link result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Link> result = testDAO.readAll(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(5, result.size());
    Iterator<Link> it = result.iterator();

    Link actualResult0 = it.next();
    assertEquals(LinkState.Planned, actualResult0.getState());

    Link result1 = it.next();
    assertEquals(LinkState.Crafting, result1.getState());

    Link result2 = it.next();
    assertEquals(LinkState.Crafted, result2.getState());

    Link result3 = it.next();
    assertEquals(LinkState.Dubbing, result3.getState());

    Link result4 = it.next();
    assertEquals(LinkState.Dubbed, result4.getState());
  }

  @Test
  public void readOneInState() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "internal"
    ));

    Link result = testDAO.readOneInState(access, BigInteger.valueOf(1), LinkState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(5), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(BigInteger.valueOf(4), result.getOffset());
    assertEquals(LinkState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:08.000001"), result.getBeginAt());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_nullIfNoneInChain() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(2, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    Link result = testDAO.readOneInState(access, BigInteger.valueOf(2), LinkState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNull(result);
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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

    testDAO.update(access, BigInteger.valueOf(2), inputData);

    Link result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(LinkState.Dubbed, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.updateState(access, BigInteger.valueOf(2), LinkState.Crafting);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test
  public void update_FailsWithInvalidState() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
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
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Link result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(BigInteger.valueOf(1), result.getChainId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    IntegrationTestService.getDb().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1));

    Link result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
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

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1));

    verify(amazonProvider, never()).deleteS3Object("xj-link-test", null);

    Link result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    // User "bill"
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Macro, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertVoiceEvent(1, 1, 8, 0, 1, "KICK", "C", 0.8, 1.0);

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
    IntegrationTestEntity.insertChoice(1, 17, 1, PatternType.Macro, 2, -5);

    // Arrangement
    IntegrationTestEntity.insertArrangement(1, 1, 8, 9);

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    //
    // Go!
    testDAO.destroy(access, BigInteger.valueOf(17));
    //
    //

    // [#263] expect request to delete link waveform from Amazon S3
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-97898asdf7892.mp3");

    // Assert destroyed Link
    assertNull(testDAO.readOne(Access.internal(), BigInteger.valueOf(17)));

    // Assert destroyed Link Meme
    assertNull(injector.getInstance(LinkMemeDAO.class).readOne(Access.internal(), BigInteger.valueOf(25)));

    // Assert destroyed Link Chord
    assertNull(injector.getInstance(LinkChordDAO.class).readOne(Access.internal(), BigInteger.valueOf(25)));

    // Assert destroyed Link Message
    assertNull(injector.getInstance(LinkMessageDAO.class).readOne(Access.internal(), BigInteger.valueOf(25)));

    // Assert destroyed Arrangement
    assertNull(injector.getInstance(ArrangementDAO.class).readOne(Access.internal(), BigInteger.valueOf(1)));

    // Assert destroyed Choice
    assertNull(injector.getInstance(ChoiceDAO.class).readOne(Access.internal(), BigInteger.valueOf(1)));

  }


}
