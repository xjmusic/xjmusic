// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.S3UploadPolicy;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;

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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// future test: permissions of different users to readMany vs. create vs. update or delete audios
@RunWith(MockitoJUnitRunner.class)
public class AudioIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private AudioDAO testDAO;
  @Mock AmazonProvider amazonProvider;
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // audio waveform config
    System.setProperty("audio.file.bucket", "xj-audio-test");

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Library "palm tree" has pattern "leaves" and pattern "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");

    // Pattern "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audios "Kick" and "Snare"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", 0.0023, 1.05, 131.0, 702);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  private static void setUpTwo() throws Exception {
    // Event and Chord on Audio 1
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioChord(1, 1, 4, "D major");

    // Pattern, Phase, Voice
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");

    // Chain, Link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Choice, Arrangement, Pick
    IntegrationTestEntity.insertChoice(7, 1, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(1, 7, 8, 1);
    IntegrationTestEntity.insertPick(1, 1, 1, 0.125, 1.23, 0.94, 440);
    IntegrationTestEntity.insertPick(2, 1, 1, 1.125, 1.23, 0.94, 220);
    IntegrationTestEntity.insertPick(3, 1, 1, 2.125, 1.23, 0.94, 110);
    IntegrationTestEntity.insertPick(4, 1, 1, 3.125, 1.23, 0.94, 55);

  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;

    System.clearProperty("audio.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2))
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    when(amazonProvider.generateKey("instrument-2-audio", "wav"))
      .thenReturn("instrument-2-audio-h2a34j5s34fd987gaw3.wav");

    Audio result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertNotNull(result.getWaveformKey());
    assertEquals(0.009, result.getStart(), 0.01);
    assertEquals(0.21, result.getLength(), 0.01);
    assertEquals(80.5, result.getTempo(), 0.01);
    assertEquals(1567.0, result.getPitch(), 0.01);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    testDAO.create(access, inputData);
  }

  @Test
  public void create_SucceedsWithoutWaveformKey() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2))
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    when(amazonProvider.generateKey("instrument-2-audio", "wav"))
      .thenReturn("instrument-2-audio-h2a34j5s34fd987gaw3.wav");

    testDAO.create(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2))
      .setName("cannons fifty nine");
    when(amazonProvider.generateKey(any(), any())).thenReturn("superAwesomeKey123");

    Audio result = testDAO.clone(access, BigInteger.valueOf(1), inputData);

    assertEquals("cannons fifty nine", result.getName());
    assertEquals(BigInteger.valueOf(2), result.getInstrumentId());
    assertEquals("superAwesomeKey123", result.getWaveformKey());
    assertEquals(AudioState.Published, result.getState());
    assertEquals(0.01, result.getStart(),0.01);
    assertEquals(2.123, result.getLength(),0.001);
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(440.0, result.getPitch(), 0.01);

    // Verify enqueued audio clone jobs
    verify(workManager).scheduleAudioClone(eq(0), eq(BigInteger.valueOf(1)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Audio result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getInstrumentId());
    assertEquals("Snare", result.getName());
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.0023), result.getStart());
    assertEquals(Double.valueOf(1.05), result.getLength());
    assertEquals(Double.valueOf(131.0), result.getTempo());
    assertEquals(Double.valueOf(702.0), result.getPitch());
  }

  @Test
  public void uploadOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    when(amazonProvider.generateAudioUploadPolicy())
      .thenReturn(new S3UploadPolicy("MyId", "MySecret", "bucket-owner-is-awesome", "xj-audio-test", "", 5));
    when(amazonProvider.getUploadURL())
      .thenReturn("https://manuts.com");
    when(amazonProvider.getCredentialId())
      .thenReturn("MyId");
    when(amazonProvider.getAudioBucketName())
      .thenReturn("xj-audio-test");
    when(amazonProvider.getAudioUploadACL())
      .thenReturn("bucket-owner-is-awesome");

    JSONObject result = testDAO.authorizeUpload(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", result.get("waveformKey"));
    assertEquals("xj-audio-test", result.get("bucketName"));
    assertNotNull(result.get("uploadPolicySignature"));
    assertEquals("https://manuts.com", result.get("uploadUrl"));
    assertEquals("MyId", result.get("awsAccessKeyId"));
    assertNotNull(result.get("uploadPolicy"));
    assertEquals("bucket-owner-is-awesome", result.get("acl"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    Audio result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result2 = (JSONObject) result.get(0);
    assertEquals("Snare", result2.get("name"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals("Kick", result1.get("name"));
  }

  @Test
  public void readAll_excludesAudiosInEraseState() throws Exception {
    IntegrationTestEntity.insertAudio(27, 1, "Erase", "shammy", "instrument-1-audio-09897fhjdf.wav", 0, 1, 120, 440);
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result2 = (JSONObject) result.get(0);
    assertEquals("Snare", result2.get("name"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals("Kick", result1.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllPickedForLink() throws Exception {
    setUpTwo();

    JSONArray result = JSON.arrayOf(testDAO.readAllPickedForLink(Access.internal(), BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(4, result.length());
  }

  @Test
  public void update_FailsWithoutInstrumentID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument ID is required");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentInstrument() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(7))
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    failure.expect(BusinessException.class);
    failure.expectMessage("Instrument does not exist");

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("Snare", result.getName());
      assertEquals(BigInteger.valueOf(1), result.getInstrumentId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2))
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.009), result.getStart());
    assertEquals(Double.valueOf(0.21), result.getLength());
    assertEquals(Double.valueOf(80.5), result.getTempo());
    assertEquals(Double.valueOf(1567.0), result.getPitch());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void erase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.erase(access, BigInteger.valueOf(1));

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals(AudioState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Audio does not exist");

    testDAO.erase(access, BigInteger.valueOf(1));
  }

  @Test
  public void erase_SucceedsEvenWithChilds() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertAudioEvent(1, 1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

    try {
      testDAO.erase(access, BigInteger.valueOf(1));

    } catch (Exception e) {
      Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
      assertNotNull(result);
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.internal();

    testDAO.destroy(access, BigInteger.valueOf(1));

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test
  public void destroy_afterAudioHasBeenPicked() throws Exception {
    setUpTwo(); // create picks for audio id 1
    Access access = Access.internal();

    testDAO.destroy(access, BigInteger.valueOf(1));

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }


  // future test: AudioDAO cannot delete record unless user has account access

  // future test: AudioDAO cannot write to WaveformKey value on create or update- ONLY updated by generating an upload policy

}
