// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryHash;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LibraryIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private LibraryDAO testDAO;

  private static void setUpLibraryHashTest() {
    IntegrationTestEntity.insertUser(101, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 101, UserRoleType.Admin);
    Timestamp at = Timestamp.valueOf("2014-08-12 12:17:02.527142");
    //
    IntegrationTestEntity.insertLibrary(10000001, 1, "leaves", at);
    IntegrationTestEntity.insertInstrument(201, 10000001, 101, "808 Drums", InstrumentType.Percussive, 0.9, at);
    IntegrationTestEntity.insertInstrument(202, 10000001, 101, "909 Drums", InstrumentType.Percussive, 0.8, at);
    IntegrationTestEntity.insertInstrumentMeme(301, 201, "Ants", at);
    IntegrationTestEntity.insertInstrumentMeme(302, 201, "Mold", at);
    IntegrationTestEntity.insertInstrumentMeme(303, 202, "Peel", at);
    IntegrationTestEntity.insertAudio(401, 201, "Published", "Beat", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudio(402, 201, "Published", "Chords Cm to D", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440, at);
    IntegrationTestEntity.insertAudioChord(501, 402, 4, "D major", at);
    IntegrationTestEntity.insertAudioChord(502, 402, 0, "C minor", at);
    IntegrationTestEntity.insertAudioEvent(601, 401, 2.5, 1, "KICK", "Eb", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(602, 401, 3, 1, "SNARE", "Ab", 0.1, 0.8, at);
    IntegrationTestEntity.insertAudioEvent(603, 401, 0, 1, "KICK", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertAudioEvent(604, 401, 1, 1, "SNARE", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPattern(701, 101, 10000001, PatternType.Rhythm, "leaves", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPattern(702, 101, 10000001, PatternType.Detail, "coconuts", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPattern(703, 101, 10000001, PatternType.Main, "bananas", 0.27, "Gb", 100.6, at);
    IntegrationTestEntity.insertPatternMeme(801, 701, "Ants", at);
    IntegrationTestEntity.insertPatternMeme(802, 701, "Mold", at);
    IntegrationTestEntity.insertPatternMeme(803, 703, "Peel", at);
    IntegrationTestEntity.insertPhase(901, 701, PhaseType.Main, 0, 16, "growth", 0.342, "C#", 120.4, at);
    IntegrationTestEntity.insertPhase(902, 701, PhaseType.Main, 1, 16, "decay", 0.25, "F#", 110.3, at);
    IntegrationTestEntity.insertPhaseChord(1001, 902, 0, "C minor", at);
    IntegrationTestEntity.insertPhaseChord(1002, 902, 4, "D major", at);
    IntegrationTestEntity.insertPhaseMeme(1101, 901, "Gravel", at);
    IntegrationTestEntity.insertPhaseMeme(1102, 901, "Fuzz", at);
    IntegrationTestEntity.insertPhaseMeme(1103, 902, "Peel", at);
    IntegrationTestEntity.insertVoice(1201, 701, InstrumentType.Percussive, "Drums", at);
    IntegrationTestEntity.insertVoice(1202, 702, InstrumentType.Harmonic, "Bass", at);
    IntegrationTestEntity.insertPhaseEvent(1401, 901, 1201, 0, 1, "BOOM", "C", 0.8, 1.0, at);
    IntegrationTestEntity.insertPhaseEvent(1402, 901, 1201, 1, 1, "SMACK", "G", 0.1, 0.8, at);
    IntegrationTestEntity.insertPhaseEvent(1403, 901, 1201, 2.5, 1, "BOOM", "C", 0.8, 0.6, at);
    IntegrationTestEntity.insertPhaseEvent(1404, 901, 1201, 3, 1, "SMACK", "G", 0.1, 0.9, at);
  }

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "palm tree" has library "leaves" and library "coconuts"
    IntegrationTestEntity.insertAccount(1, "palm tree");
    IntegrationTestEntity.insertLibrary(1, 1, "leaves");
    IntegrationTestEntity.insertLibrary(2, 1, "coconuts");

    // Account "boat" has library "helm" and library "sail"
    IntegrationTestEntity.insertAccount(2, "boat");
    IntegrationTestEntity.insertLibrary(3, 2, "helm");
    IntegrationTestEntity.insertLibrary(4, 2, "sail");

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals("manuts", result.getName());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Library result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Library result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readHash() throws Exception {
    setUpLibraryHashTest();

    LibraryHash result = testDAO.readHash(Access.internal(), BigInteger.valueOf(10000001));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(10000001), result.getLibraryId());
    assertEquals("PhaseEvent-1403=1407871023000,PhaseEvent-1404=1407871023000,PhaseEvent-1401=1407871023000,PhaseEvent-1402=1407871023000,Library-10000001=1407871023000,Audio-402=1407871023000,Audio-401=1407871023000,AudioEvent-604=1407871023000,Pattern-703=1407871023000,AudioEvent-603=1407871023000,Pattern-702=1407871023000,Pattern-701=1407871023000,PhaseChord-1001=1407871023000,PhaseChord-1002=1407871023000,InstrumentMeme-303=1407871023000,AudioEvent-602=1407871023000,AudioEvent-601=1407871023000,Voice-1202=1407871023000,Voice-1201=1407871023000,AudioChord-502=1407871023000,PhaseMeme-1103=1407871023000,PhaseMeme-1102=1407871023000,InstrumentMeme-301=1407871023000,InstrumentMeme-302=1407871023000,PatternMeme-803=1407871023000,PatternMeme-802=1407871023000,PhaseMeme-1101=1407871023000,PatternMeme-801=1407871023000,Phase-902=1407871023000,Phase-901=1407871023000,Instrument-202=1407871023000,Instrument-201=1407871023000,AudioChord-501=1407871023000", result.toString());
    JSONObject resultJson = result.toJSONObject();
    assertEquals(33, resultJson.length());
    assertEquals("e5e683cd746f175d45b1bc77502a361bc67835f9e6206ef80502b0100a50bc70", result.sha256());

    injector.getInstance(PhaseEventDAO.class).update(Access.internal(), BigInteger.valueOf(1404),
      new PhaseEvent()
        .setDuration(0.21)
        .setInflection("ding")
        .setPosition(7.23)
        .setTonality(0.1)
        .setVelocity(0.9)
        .setVoiceId(BigInteger.valueOf(1201))
        .setPhaseId(BigInteger.valueOf(901))
        .setNote("D4"));

    LibraryHash result2 = testDAO.readHash(Access.internal(), BigInteger.valueOf(10000001));

    assertNotNull(result2);
    assertEquals(BigInteger.valueOf(10000001), result2.getLibraryId());
    JSONObject resultJson2 = result2.toJSONObject();
    assertEquals(33, resultJson2.length());
    assertNotEquals("e5e683cd746f175d45b1bc77502a361bc67835f9e6206ef80502b0100a50bc70", result2.sha256()); // should have changed
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("leaves", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("coconuts", result2.get("name"));
  }

  @Test
  public void readAll_fromAllAccounts() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1,2"
    ));

    Collection<Library> result = testDAO.readAll(access, Lists.newArrayList());

    assertEquals(4, result.size());
    Iterator<Library> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setAccountId(BigInteger.valueOf(3));

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(3978));

    try {
      testDAO.update(access, BigInteger.valueOf(3), inputData);

    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(BigInteger.valueOf(2), result.getAccountId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("trunk")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfLibraryHasChilds() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertPattern(301, 101, 2, PatternType.Main, "brilliant", 0.342, "C#", 0.286);

    try {
      testDAO.destroy(access, BigInteger.valueOf(2));
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      throw e;
    }
  }
}
