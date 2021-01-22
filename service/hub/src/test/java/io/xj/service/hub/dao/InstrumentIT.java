// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.Library;
import io.xj.User;
import io.xj.UserRole;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.tables.InstrumentAudio.INSTRUMENT_AUDIO;
import static io.xj.service.hub.tables.InstrumentAudioChord.INSTRUMENT_AUDIO_CHORD;
import static io.xj.service.hub.tables.InstrumentAudioEvent.INSTRUMENT_AUDIO_EVENT;
import static io.xj.service.hub.tables.InstrumentMeme.INSTRUMENT_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private InstrumentDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

    // Library "sandwich" has instrument "jams" and instrument "buns"
    fake.library1 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("sandwich")
      .build());
    fake.instrument201 = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Pad)
      .setState(Instrument.State.Published)
      .setName("buns")
      .build());
    fake.instrument202 = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Percussive)
      .setState(Instrument.State.Published)
      .setName("jams")
      .build());
    test.insert(InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.instrument202.getId())
      .setName("smooth")
      .build());
    fake.audio1 = test.insert(InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.instrument202.getId())
      .setName("Test audio")
      .setWaveformKey("fake.audio5.wav")
      .setStart(0)
      .setLength(2)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.5)
      .build());

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setState(Instrument.State.Published)
      .setType(Instrument.Type.Percussive)
      .build();

    Instrument result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(Instrument.Type.Percussive, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of instruments
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setName("cannons fifty nine")
      .build();
    test.insert(InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.audio1.getInstrumentId())
      .setInstrumentAudioId(fake.audio1.getId())
      .setPosition(0)
      .setDuration(1)
      .setName("bing")
      .setNote("C")
      .setVelocity(1)
      .build());
    test.insert(InstrumentAudioChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.audio1.getInstrumentId())
      .setInstrumentAudioId(fake.audio1.getId())
      .setPosition(0)
      .setName("G minor")
      .build());

    Instrument result = testDAO.clone(hubAccess, fake.instrument202.getId(), subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(Instrument.Type.Percussive, result.getType());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Instrument result = testDAO.readOne(hubAccess, fake.instrument201.getId());

    assertNotNull(result);
    assertEquals(Instrument.Type.Pad, result.getType());
    assertEquals(Instrument.State.Published, result.getState());
    assertEquals(fake.instrument201.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.instrument201.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Instrument> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User");

    Collection<Instrument> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Instrument subject = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("shimmy")
      .setLibraryId(UUID.randomUUID().toString())
      .build();

    try {
      testDAO.update(hubAccess, fake.instrument201.getId(), subject);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(HubAccess.internal(), fake.instrument201.getId());
      assertNotNull(result);
      assertEquals("buns", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(DAOException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setState(Instrument.State.Published)
      .setType(Instrument.Type.Percussive)
      .build();

    testDAO.update(hubAccess, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void update_addAudio() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setDensity(0.42)
      .setState(Instrument.State.Published)
      .setType(Instrument.Type.Percussive)
      .build());
    test.insert(InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(subject.getId())
      .setName("Test audio")
      .setWaveformKey("fake.audio5.wav")
      .setStart(0)
      .setLength(2)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.42)
      .build());

    testDAO.update(hubAccess, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    fake.instrument251 = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Pad)
      .setState(Instrument.State.Published)
      .setName("jub")
      .build());

    testDAO.destroy(hubAccess, fake.instrument251.getId());

    try {
      testDAO.readOne(HubAccess.internal(), fake.instrument251.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  /**
   [#170299297] Cannot delete Instruments that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Instrument instrument = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Pad)
      .setState(Instrument.State.Published)
      .setName("sandwich")
      .build());
    test.insert(InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName("frozen")
      .build());
    test.insert(InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName("ham")
      .build());

    failure.expect(DAOException.class);
    failure.expectMessage("Found Instrument Meme");

    testDAO.destroy(hubAccess, instrument.getId());
  }

  /**
   [#170299297] As long as instrument has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Instrument instrument = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Pad)
      .setState(Instrument.State.Published)
      .setName("sandwich")
      .build());
    var audio = test.insert(InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName("drums")
      .setWaveformKey("drums.wav")
      .setStart(0)
      .setLength(1)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.6)
      .build());
    test.insert(InstrumentAudioChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(audio.getInstrumentId())
      .setInstrumentAudioId(audio.getId())
      .setPosition(0)
      .setName("D minor")
      .build());
    test.insert(InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(audio.getInstrumentId())
      .setInstrumentAudioId(audio.getId())
      .setPosition(0)
      .setDuration(0.5)
      .setName("bing")
      .setNote("D")
      .setVelocity(1)
      .build());

    testDAO.destroy(hubAccess, instrument.getId());
  }

}
