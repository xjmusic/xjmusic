// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.persistence.AmazonProvider;
import io.xj.service.hub.persistence.S3UploadPolicy;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.model.InstrumentState;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.User;
import io.xj.service.hub.model.UserRole;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.hub.testing.InternalResources;
import io.xj.service.hub.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.service.hub.tables.InstrumentAudioChord.INSTRUMENT_AUDIO_CHORD;
import static io.xj.service.hub.tables.InstrumentAudioEvent.INSTRUMENT_AUDIO_EVENT;
import static io.xj.service.hub.testing.Assert.assertNotExist;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentAudioIT {
  public WorkManager workManager;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public AmazonProvider amazonProvider;
  private InstrumentAudioDAO testDAO;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new IntegrationTestModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule(), new IntegrationTestModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fake.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fake.user3, UserRoleType.User));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Library "sandwich" has instrument "jams" and instrument "buns"
    fake.library1 = test.insert(Library.create(fake.account1, "sandwich", InternalResources.now()));
    fake.instrument201 = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Percussive, InstrumentState.Published, "jams"));
    test.insert(InstrumentMeme.create(fake.instrument202, "smooth"));
    fake.audio1 = test.insert(InstrumentAudio.create(fake.instrument202, "Test audio", "fake.audio5.wav", 0, 2, 120, 300, 0.5));
    fake.audioEvent1 = test.insert(InstrumentAudioEvent.create(fake.audio1, 0, 0.5, "bing", "D", 1));
    fake.audio2 = test.insert(InstrumentAudio.create(fake.instrument202, "Test audio2", "fake.audio5222.wav", 0, 2, 120, 300, 0.5));
    fake.audioEvent2 = test.insert(InstrumentAudioEvent.create(fake.audio2, 0, 0.5, "bang", "E", 1));

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentAudioDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setInstrumentId(fake.instrument201.getId())
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    when(amazonProvider.generateKey("instrument-2-audio", "wav"))
      .thenReturn("instrument-2-audio-h2a34j5s34fd987gaw3.wav");

    InstrumentAudio result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.instrument201.getId(), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertNotNull(result.getWaveformKey());
    assertEquals(0.009, result.getStart(), 0.01);
    assertEquals(0.21, result.getLength(), 0.01);
    assertEquals(80.5, result.getTempo(), 0.01);
    assertEquals(1567.0, result.getPitch(), 0.01);
  }

  @Test(expected = ValueException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
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
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setInstrumentId(fake.instrument202.getId())
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);
    when(amazonProvider.generateKey("instrument-" + fake.instrument202.getId() + "-audio", "wav"))
      .thenReturn("instrument-2-audio-h2a34j5s34fd987gaw3.wav");

    InstrumentAudio result = testDAO.create(access, inputData);

    verify(amazonProvider).generateKey("instrument-" + fake.instrument202.getId() + "-audio", "wav");
    assertEquals("instrument-2-audio-h2a34j5s34fd987gaw3.wav", result.getWaveformKey());
  }

  /**
   [#170290553] Clone sub-entities of instrument audios
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setInstrumentId(fake.instrument202.getId())
      .setName("cannons fifty nine");
    fake.audioChord1 = test.insert(InstrumentAudioChord.create(fake.audio1, 0, "D minor"));

    InstrumentAudio result = testDAO.clone(access, fake.audio1.getId(), inputData);

    assertEquals("cannons fifty nine", result.getName());
    assertEquals(fake.instrument202.getId(), result.getInstrumentId());
    assertEquals("fake.audio5.wav", result.getWaveformKey());
    assertEquals(0.01, result.getStart(), 0.01);
    assertEquals(2.0, result.getLength(), 0.01);
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(300.0, result.getPitch(), 0.01);
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    InstrumentAudio result = testDAO.readOne(access, fake.audio1.getId());

    assertNotNull(result);
    assertEquals(fake.instrument202.getId(), result.getInstrumentId());
    assertEquals("Test audio", result.getName());
    assertEquals("fake.audio5.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.0), result.getStart());
    assertEquals(Double.valueOf(2.0), result.getLength());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals(Double.valueOf(300.0), result.getPitch());
  }

  @Test
  public void uploadOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    when(amazonProvider.generateAudioUploadPolicy())
      .thenReturn(new S3UploadPolicy("MyId", "MySecret", "bucket-owner-is-awesome", "xj-audio-test", "", 5));
    when(amazonProvider.getUploadURL())
      .thenReturn("https://coconuts.com");
    when(amazonProvider.getCredentialId())
      .thenReturn("MyId");
    when(amazonProvider.getAudioBucketName())
      .thenReturn("xj-audio-test");
    when(amazonProvider.getAudioUploadACL())
      .thenReturn("bucket-owner-is-awesome");

    Map<String, String> result = testDAO.authorizeUpload(access, fake.audio2.getId());

    assertNotNull(result);
    assertEquals("fake.audio5222.wav", result.get("waveformKey"));
    assertEquals("xj-audio-test", result.get("bucketName"));
    assertNotNull(result.get("uploadPolicySignature"));
    assertEquals("https://coconuts.com", result.get("uploadUrl"));
    assertEquals("MyId", result.get("awsAccessKeyId"));
    assertNotNull(result.get("uploadPolicy"));
    assertEquals("bucket-owner-is-awesome", result.get("acl"));
  }

  @Test
  public void readMany() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    Collection<InstrumentAudio> result = testDAO.readMany(access, ImmutableList.of(fake.instrument202.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(), "Artist");

    Collection<InstrumentAudio> result = testDAO.readMany(access, ImmutableList.of(fake.instrument202.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutInstrumentID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    failure.expect(ValueException.class);
    failure.expectMessage("Instrument ID is required");

    testDAO.update(access, fake.audio1.getId(), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentInstrument() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setInstrumentId(UUID.randomUUID())
      .setName("maracas")
      .setWaveformKey("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "maracas.wav")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    failure.expect(HubException.class);
    failure.expectMessage("Instrument does not exist");

    try {
      testDAO.update(access, fake.audio2.getId(), inputData);

    } catch (Exception e) {
      InstrumentAudio result = testDAO.readOne(Access.internal(), fake.audio2.getId());
      assertNotNull(result);
      assertEquals("Test audio2", result.getName());
      assertEquals(fake.instrument202.getId(), result.getInstrumentId());
      throw e;
    }
  }


  // [#162361785] InstrumentAudio can be moved to a different Instrument
  @Test
  public void update() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    InstrumentAudio inputData = InstrumentAudio.create()
      .setInstrumentId(fake.instrument201.getId())
      .setName("maracas")
      .setStart(0.009)
      .setLength(0.21)
      .setPitch(1567.0)
      .setTempo(80.5);

    testDAO.update(access, fake.audio1.getId(), inputData);

    InstrumentAudio result = testDAO.readOne(Access.internal(), fake.audio1.getId());
    assertNotNull(result);
    assertEquals(fake.instrument201.getId(), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertEquals("fake.audio5.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.009), result.getStart());
    assertEquals(Double.valueOf(0.21), result.getLength());
    assertEquals(Double.valueOf(80.5), result.getTempo());
    assertEquals(Double.valueOf(1567.0), result.getPitch());
  }

  // FUTURE test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(), "Artist");

    failure.expect(HubException.class);
    failure.expectMessage("InstrumentAudio does not exist");

    testDAO.destroy(access, fake.audio1.getId());
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    test.insert(InstrumentAudioEvent.create(fake.audio1, 0.42, 0.41, "HEAVY", "C", 0.7));

    try {
      testDAO.destroy(access, fake.audio1.getId());

    } catch (Exception e) {
      InstrumentAudio result = testDAO.readOne(Access.internal(), fake.audio1.getId());
      assertNotNull(result);
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.internal();

    testDAO.destroy(access, fake.audio1.getId());

    assertNotExist(testDAO, fake.audio1.getId());
  }

  // future test: AudioDAO cannot delete record unless user has account access

  // future test: AudioDAO cannot write to WaveformKey value on of or update- ONLY updated by generating an upload policy


/*

TODO address deleting audio after it has been picked


  @Test
  public void destroy_afterAudioHasBeenPicked() throws Exception {
    Access access = Access.internal();
    // EventEntity and ChordEntity on InstrumentAudio 1
    test.insert(InstrumentAudioEvent.create(fake.audio1, 2.5, 1.0, "KICK", "Eb", 0.8);
    test.insert(InstrumentAudioChord.create(fake.audio1, 4, "D major");
    // Sequence, Pattern, Voice
    test.insert(Program.create(fake.user2, fake.library1, ProgramType.Macro, ProgramState.Published, "epic concept",  "C#", 0.342, 0.286);
    test.insert(ProgramSequence.create(1, 1, PatternType.Macro, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    test.insert(ProgramSequencePattern.create(110, 1, 1, 0);
    test.insert(ProgramVoice.create(8, 1, InstrumentType.Percussive, "This is a percussive voice");

    testDAO.destroy(access, fake.audio1.getId());

    assertNotExist(testDAO, fake.audio1.getId());
  }
*/







/*

  FUTURE tests for events and chords below



  @Before
  public void setUp() throws Exception {
    reset();

    Injector injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
    audioFactory = injector.getInstance(AudioFactory.class);


    // audio waveform config
    System.setProperty("audio.file.bucket", "xj-audio-test");

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(of(1, 1, "palm tree",now()));

    // Sequence "leaves" has instruments "808" and "909"
    insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audios "Kick" and "Snare"
    insertAudio(1, 1, "Published", "Kick", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", 0.01, 2.123, 120.0, 440.0);
    insertAudio(2, 1, "Published", "Snare", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", 0.0023, 1.05, 131.0, 702.0);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioDAO.class);
  }




  @Test
  public void create() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setInstrumentAudioId(UUID.randomUUID());

    AudioChord result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(4.0, result.getPosition(), 0.01);
    assertEquals("G minor 7", result.getName());
    assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    AudioChord result = testDAO.readOne(access, UUID.fromString(1000L));

    assertNotNull(result);
    assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, UUID.fromString(1000L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    Collection<AudioChord> result = testDAO.readMany(access, ImmutableList.of(fake.audio1.getId()));

    assertEquals(2, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<AudioChord> result = testDAO.readMany(access, ImmutableList.of(fake.audio1.getId()));

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, UUID.fromString(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.update(access, fake.audio2.getId(), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("cannons");

    try {
      testDAO.update(access, UUID.fromString(1001L), inputData);

    } catch (Exception e) {
      AudioChord result = testDAO.readOne(Access.internal(), UUID.fromString(1001L));
      assertNotNull(result);
      assertEquals("C minor", result.getName());
      assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioChord inputData = new AudioChord()
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, UUID.fromString(1000L), inputData);

    AudioChord result = testDAO.readOne(Access.internal(), UUID.fromString(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
  }

  // future test: DAO cannot update audio chord to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    testDAO.destroy(access, UUID.fromString(1000L));

    assertNotExist(testDAO, UUID.fromString(1000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, UUID.fromString(1000L));
  }


    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(of(1, 1, "palm tree",now()));

    // Sequence "leaves" has instruments "808" and "909"
    insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has InstrumentAudio "Beat"
    insertAudio(1, 1, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);

    // InstrumentAudio "Drums" has events "KICK" and "SNARE" 2x each
    insertAudioEvent(1, 2.5, 1.0, "KICK", "Eb", 0.8, 1.0);
    insertAudioEvent(1, 3.0, 1.0, "SNARE", "Ab", 0.1, 0.8);
    insertAudioEvent(1, 0, 1.0, "KICK", "C", 0.8, 1.0);
    insertAudioEvent(1, 1.0, 1.0, "SNARE", "G", 0.1, 0.8);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioEventDAO.class);
  }


  @Test
  public void create() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.4)
      .setName("KICK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setName("KICK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutNote() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setName("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    AudioEvent result = testDAO.readOne(access, UUID.fromString(1003L));

    assertNotNull(result);
    assertEquals(UUID.fromString(1003L), result.getId());
    assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SNARE", result.getName());
    assertEquals("G", result.getNote());
    assertEquals(Double.valueOf(1.0), result.getPosition());
    assertEquals(Double.valueOf(0.1), result.getTonality());
    assertEquals(Double.valueOf(0.8), result.getVelocity());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, UUID.fromString(1003L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    Collection<AudioEvent> result = testDAO.readMany(access, ImmutableList.of(fake.audio1.getId()));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<AudioEvent> result = testDAO.readMany(access, ImmutableList.of(fake.audio1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }


  @Test
  public void readAllOfInstrument() throws Exception {
    insertAudio(51, 1, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);
    insertAudioEvent(51, 12.5, 1.0, "JAM", "Eb", 0.8, 1.0);
    insertAudioEvent(51, 14.0, 1.0, "PUMP", "Ab", 0.1, 0.8);
    insertAudioEvent(51, 18, 1.0, "JAM", "C", 0.8, 1.0);
    insertAudioEvent(51, 20.0, 1.0, "DUNK", "G", 0.1, 0.8);
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(fake.audio1.getId()));

    assertNotNull(result);
    assertEquals(8L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
    assertEquals("JAM", it.next().getName());
    assertEquals("PUMP", it.next().getName());
    assertEquals("JAM", it.next().getName());
    assertEquals("DUNK", it.next().getName());
  }

  @Test
  public void readAllOfInstrument_SeesNothingOutsideOfLibrary() throws Exception {
    insert(of(6, "bananas");
    insert(of(61, 6, "palm tree", now()));
    insertInstrument(61, 61, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(62, 61, 2, "909 Drums", InstrumentType.Percussive, 0.8);
    insertAudio(61, 61, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);
    insertAudioEvent(61, 2.5, 1.0, "ASS", "Eb", 0.8, 1.0);
    insertAudioEvent(61, 3.0, 1.0, "ASS", "Ab", 0.1, 0.8);
    insertAudioEvent(61, 0, 1.0, "ASS", "C", 0.8, 1.0);
    insertAudioEvent(61, 1.0, 1.0, "ASS", "G", 0.1, 0.8);
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(fake.audio1.getId()));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
    assertEquals("KICK", it.next().getName());
    assertEquals("SNARE", it.next().getName());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setName("KICK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.update(access, UUID.fromString(1002L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setName("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.update(access, UUID.fromString(1001L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setName("SNARE")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setInstrumentAudioId(UUID.randomUUID());

    try {
      testDAO.update(access, UUID.fromString(1002L), inputData);

    } catch (Exception e) {
      AudioEvent result = testDAO.readOne(Access.internal(), UUID.fromString(1002L));
      assertNotNull(result);
      assertEquals("KICK", result.getName());
      assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.2)
      .setName("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setInstrumentAudioId(UUID.randomUUID());

    testDAO.update(access, UUID.fromString(1000L), inputData);

    AudioEvent result = testDAO.readOne(Access.internal(), UUID.fromString(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.getTonality(), 0.01);
    assertEquals(0.72, result.getVelocity(), 0.01);
    assertEquals(fake.audio1.getId(), result.getInstrumentAudioId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    testDAO.destroy(access, UUID.fromString(1000L));

    assertNotExist(testDAO, UUID.fromString(1000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, UUID.fromString(1000L));
  }

*/

}
