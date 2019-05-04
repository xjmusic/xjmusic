// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.S3UploadPolicy;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.work.WorkManager;
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
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// future test: permissions of different users to readMany vs. create vs. update or delete audios
@RunWith(MockitoJUnitRunner.class)
public class AudioIT {
  @Spy
  final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private AudioDAO testDAO;

  private static void setUpTwo() {
    // Event and Chord on Audio 1
    IntegrationTestEntity.insertAudioEvent(1, 2.5, 1.0, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioChord(1, 4, "D major");

    // Sequence, Pattern, Voice
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Macro, SequenceState.Published, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Macro, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertSequencePattern(110, 1, 1, 0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
  }

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    Injector injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
          bind(WorkManager.class).toInstance(workManager);
        }
      }));

    // audio waveform config
    System.setProperty("audio.file.bucket", "xj-audio-test");

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");

    // Sequence "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audios "Kick" and "Snare"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", 0.01, 2.123, 120.0, 440.0);
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", 0.0023, 1.05, 131.0, 702.0);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioDAO.class);
  }

  @After
  public void tearDown() {
    System.clearProperty("audio.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2L))
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    when(amazonProvider.generateKey("instrument-2-audio", "wav"))
      .thenReturn("instrument-2-audio-h2a34j5s34fd987gaw3.wav");

    Audio result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertNotNull(result.getWaveformKey());
    assertEquals(0.009, result.getStart(), 0.01);
    assertEquals(0.21, result.getLength(), 0.01);
    assertEquals(80.5, result.getTempo(), 0.01);
    assertEquals(1567.0, result.getPitch(), 0.01);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
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
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2L))
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
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2L))
      .setName("cannons fifty nine");
    when(amazonProvider.generateKey(any(), any())).thenReturn("superAwesomeKey123");

    Audio result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertEquals("cannons fifty nine", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getInstrumentId());
    assertEquals("superAwesomeKey123", result.getWaveformKey());
    assertEquals(AudioState.Published, result.getState());
    assertEquals(0.01, result.getStart(), 0.01);
    assertEquals(2.123, result.getLength(), 0.001);
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(440.0, result.getPitch(), 0.01);

    // Verify enqueued audio clone jobs
    verify(workManager).doAudioClone(eq(BigInteger.valueOf(1L)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Audio result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getInstrumentId());
    assertEquals("Snare", result.getName());
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.0023), result.getStart());
    assertEquals(Double.valueOf(1.05), result.getLength());
    assertEquals(Double.valueOf(131.0), result.getTempo());
    assertEquals(Double.valueOf(702.0), result.getPitch());
  }

  @Test
  public void uploadOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
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

    JSONObject result = testDAO.authorizeUpload(access, BigInteger.valueOf(2L));

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
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Audio> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_excludesAudiosInEraseState() throws Exception {
    IntegrationTestEntity.insertAudio(27, 1, "Erase", "shammy", "instrument-1-audio-09897f1h2j3d4f5.wav", 0, 1.0, 120.0, 440.0);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Audio> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<Audio> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
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

    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentInstrument() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(7L))
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    failure.expect(CoreException.class);
    failure.expectMessage("Instrument does not exist");

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("Snare", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getInstrumentId());
      throw e;
    }
  }


  /**
   [#162361785] Audio can be moved to a different Instrument
   */
  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = new Audio()
      .setInstrumentId(BigInteger.valueOf(2L))
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.009), result.getStart());
    assertEquals(Double.valueOf(0.21), result.getLength());
    assertEquals(Double.valueOf(80.5), result.getTempo());
    assertEquals(Double.valueOf(1567.0), result.getPitch());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void erase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.erase(access, BigInteger.valueOf(1L));

    Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals(AudioState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Audio does not exist");

    testDAO.erase(access, BigInteger.valueOf(1L));
  }

  @Test
  public void erase_SucceedsEvenWithChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertAudioEvent(1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

    try {
      testDAO.erase(access, BigInteger.valueOf(1L));

    } catch (Exception e) {
      Audio result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
      assertNotNull(result);
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.internal();

    testDAO.destroy(access, BigInteger.valueOf(1L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_afterAudioHasBeenPicked() throws Exception {
    setUpTwo(); // create picks for audio id 1
    Access access = Access.internal();

    testDAO.destroy(access, BigInteger.valueOf(1L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1L));
  }


  // future test: AudioDAO cannot delete record unless user has account access

  // future test: AudioDAO cannot write to WaveformKey value on create or update- ONLY updated by generating an upload policy

}
