//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.sub.Audio;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class AudioTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Audio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(BigInteger.valueOf(53L))
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new Audio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new Audio()
      .setLength(3.4)
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(BigInteger.valueOf(53L))
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Tempo is required");

    new Audio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(BigInteger.valueOf(53L))
      .validate();
  }

  @Test
  public void validate_failsWithoutRootPitch() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Root Pitch is required");

    new Audio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(BigInteger.valueOf(53L))
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("waveformKey", "name", "start", "length", "tempo", "pitch", "density"), new Audio().getResourceAttributeNames());
  }


  /*
FUTURE adapt these Audio unit tests (from legacy integration tests)

  private static void setUpTwo() {
    // Event and Chord on Audio 1
    insertAudioEvent(1, 2.5, 1.0, "KICK", "Eb", 0.8, 1.0);
    insertAudioChord(1, 4, "D major");

    // Sequence, Pattern, Voice
    insertSequence(1, 2, 1, ProgramType.Macro, ProgramState.Published, "epic concept", 0.342, "C#", 0.286);
    insertPattern(1, 1, PatternType.Macro, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    insertProgram(110, 1, 1, 0);
    insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
  }

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
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(newLibrary(1, 1, "palm tree",now()));

    // Sequence "leaves" has instruments "808" and "909"
    insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audios "Kick" and "Snare"
    insertAudio(1, 1, "Published", "Kick", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "kick1.wav", 0.01, 2.123, 120.0, 440.0);
    insertAudio(2, 1, "Published", "Snare", "instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", 0.0023, 1.05, 131.0, 702.0);

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
    Audio inputData = audioFactory.newAudio()
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
    Audio inputData = audioFactory.newAudio()
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
    Audio inputData = audioFactory.newAudio()
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
    Audio inputData = audioFactory.newAudio()
      .setInstrumentId(BigInteger.valueOf(2L))
      .setName("cannons fifty nine");
    when(amazonProvider.generateKey(any(), any())).thenReturn("superAwesomeKey123");

    Audio result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertEquals("cannons fifty nine", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getInstrumentId());
    assertEquals("superAwesomeKey123", result.getWaveformKey());
    assertEquals(InstrumentState.Published, result.getState());
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
      .thenReturn("https://coconuts.com");
    when(amazonProvider.getCredentialId())
      .thenReturn("MyId");
    when(amazonProvider.getAudioBucketName())
      .thenReturn("xj-audio-test");
    when(amazonProvider.getAudioUploadACL())
      .thenReturn("bucket-owner-is-awesome");

    Map<String, String> result = testDAO.authorizeUpload(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals("instrument" + File.separator + "percussion" + File.separator + "808" + File.separator + "snare.wav", result.get("waveformKey"));
    assertEquals("xj-audio-test", result.get("bucketName"));
    assertNotNull(result.get("uploadPolicySignature"));
    assertEquals("https://coconuts.com", result.get("uploadUrl"));
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
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Audio> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_excludesAudiosInEraseState() throws Exception {
    insertAudio(27, 1, "Erase", "shammy", "instrument-1-audio-09897f1h2j3d4f5.wav", 0, 1.0, 120.0, 440.0);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Audio> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<Audio> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = audioFactory.newAudio()
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
    Audio inputData = audioFactory.newAudio()
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


// FUTURE implement [#162361785] Audio can be moved to a different Instrument -- or is it just copying and pasting now??

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Audio inputData = audioFactory.newAudio()
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
    assertEquals(InstrumentState.Erase, result.getState());
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
    insertAudioEvent(1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

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

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_afterAudioHasBeenPicked() throws Exception {
    setUpTwo(); // create picks for audio id 1
    Access access = Access.internal();

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }


  // future test: AudioDAO cannot delete record unless user has account access

  // future test: AudioDAO cannot write to WaveformKey value on create or update- ONLY updated by generating an upload policy


    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(newLibrary(1, 1, "palm tree",now()));

    // Sequence "leaves" has instruments "808" and "909"
    insertInstrument(1, 1, 2, "Harmonic Performance", InstrumentType.Percussive, 0.9);

    // Instrument "808" has Audio "Chords Cm to D"
    insertAudio(1, 1, "Published", "Chords Cm to D", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440.0);

    // Audio "Drums" has events "C minor" and "D major" 2x each
    insertAudioChord(1, 4, "D major");
    insertAudioChord(1, 0, "C minor");

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioChordDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setAudioId(BigInteger.valueOf(1L));

    AudioChord result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(4.0, result.getPosition(), 0.01);
    assertEquals("G minor 7", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AudioChord result = testDAO.readOne(access, BigInteger.valueOf(1000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
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

    Collection<AudioChord> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AudioChord> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(57L))
      .setName("cannons");

    try {
      testDAO.update(access, BigInteger.valueOf(1001L), inputData);

    } catch (Exception e) {
      AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
      assertNotNull(result);
      assertEquals("C minor", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setAudioId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(1000L), inputData);

    AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  // future test: DAO cannot update audio chord to a User or Library not owned by current session

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
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));
  }




    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(newLibrary(1, 1, "palm tree",now()));

    // Sequence "leaves" has instruments "808" and "909"
    insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audio "Beat"
    insertAudio(1, 1, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);

    // Audio "Drums" has events "KICK" and "SNARE" 2x each
    insertAudioEvent(1, 2.5, 1.0, "KICK", "Eb", 0.8, 1.0);
    insertAudioEvent(1, 3.0, 1.0, "SNARE", "Ab", 0.1, 0.8);
    insertAudioEvent(1, 0, 1.0, "KICK", "C", 0.8, 1.0);
    insertAudioEvent(1, 1.0, 1.0, "SNARE", "G", 0.1, 0.8);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioEventDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.4)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutNote() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AudioEvent result = testDAO.readOne(access, BigInteger.valueOf(1003L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1003L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SNARE", result.getInflection());
    assertEquals("G", result.getNote());
    assertEquals(Double.valueOf(1.0), result.getPosition());
    assertEquals(Double.valueOf(0.1), result.getTonality());
    assertEquals(Double.valueOf(0.8), result.getVelocity());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AudioEvent> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

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
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(8L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("JAM", it.next().getInflection());
    assertEquals("PUMP", it.next().getInflection());
    assertEquals("JAM", it.next().getInflection());
    assertEquals("DUNK", it.next().getInflection());
  }

  @Test
  public void readAllOfInstrument_SeesNothingOutsideOfLibrary() throws Exception {
    insert(newAccount(6, "bananas");
    insert(newLibrary(61, 6, "palm tree",now()));
    insertInstrument(61, 61, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    insertInstrument(62, 61, 2, "909 Drums", InstrumentType.Percussive, 0.8);
    insertAudio(61, 61, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);
    insertAudioEvent(61, 2.5, 1.0, "ASS", "Eb", 0.8, 1.0);
    insertAudioEvent(61, 3.0, 1.0, "ASS", "Ab", 0.1, 0.8);
    insertAudioEvent(61, 0, 1.0, "ASS", "C", 0.8, 1.0);
    insertAudioEvent(61, 1.0, 1.0, "ASS", "G", 0.1, 0.8);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(1002L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(1001L));

    testDAO.update(access, BigInteger.valueOf(1001L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("SNARE")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(287L));

    try {
      testDAO.update(access, BigInteger.valueOf(1002L), inputData);

    } catch (Exception e) {
      AudioEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1002L));
      assertNotNull(result);
      assertEquals("KICK", result.getInflection());
      assertEquals(BigInteger.valueOf(1L), result.getAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.2)
      .setInflection("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(1000L), inputData);

    AudioEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getInflection());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.getTonality(), 0.01);
    assertEquals(0.72, result.getVelocity(), 0.01);
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

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
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));
  }

   */


}
