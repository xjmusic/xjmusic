// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.InstrumentAudio.INSTRUMENT_AUDIO;
import static io.xj.hub.tables.InstrumentMeme.INSTRUMENT_MEME;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentManagerDbTest {
  private InstrumentManager testManager;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "sandwich" has instruments "jams" and instrument "buns"
    fake.library1 = test.insert(buildLibrary(fake.account1, "sandwich"));
    fake.instrument201 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "jams"));
    fake.audio1 = test.insert(buildInstrumentAudio(fake.instrument202, "Test audio", "fake.audio5.wav", 0.0f, 2.0f, 120.0f));

    // Instantiate the test subject
    testManager = injector.getInstance(InstrumentManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    Instrument subject = new Instrument();
    subject.setId(UUID.randomUUID());
    subject.setLibraryId(fake.library1.getId());
    subject.setName("shimmy");
    subject.setVolume(0.54f);
    subject.setDensity(0.6f);
    subject.setState(InstrumentState.Published);
    subject.setMode(InstrumentMode.Event);
    subject.setType(InstrumentType.Drum);

    Instrument result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(0.54f, result.getVolume(), 0.01);
    assertEquals(InstrumentType.Drum, result.getType());
    assertEquals(InstrumentMode.Event, result.getMode());
  }

  /**
   Overall volume parameter defaults to 1.0 https://www.pivotaltracker.com/story/show/179215413
   */
  @Test
  public void create_defaultVolume() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    Instrument subject = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "shimmy");

    Instrument result = testManager.create(
      access, subject);

    assertEquals(1.0f, result.getVolume(), 0.01);
  }

  /**
   https://www.pivotaltracker.com/story/show/170290553 Clone sub-entities of instruments
   Cloning an Instrument should not reset its Parameters https://www.pivotaltracker.com/story/show/180764355
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    test.insert(buildInstrumentMeme(fake.instrument202, "chunk"));
    Instrument subject = new Instrument();
    subject.setId(UUID.randomUUID());
    subject.setLibraryId(fake.library1.getId());
    subject.setConfig(
      """
        isMultiphonic = true
        isOneShot = false
        isTonal = false
        oneShotObserveLengthOfEvents = [TEST]
          """
    );
    subject.setName("cannons fifty nine");

    ManagerCloner<Instrument> result = testManager.clone(access, fake.instrument202.getId(), subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getClone().getLibraryId());
    assertEquals("cannons fifty nine", result.getClone().getName());
    assertEquals(InstrumentType.Drum, result.getClone().getType());
    assertEquals(
      """
        isMultiphonic = true
        isOneShot = false
        isOneShotCutoffEnabled = true
        isTonal = false
        oneShotObserveLengthOfEvents = [TEST]
          """,
      result.getClone().getConfig()
    );
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(result.getClone().getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(result.getClone().getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Instrument result = testManager.readOne(access, fake.instrument201.getId());

    assertNotNull(result);
    assertEquals(InstrumentType.Pad, result.getType());
    assertEquals(InstrumentState.Published, result.getState());
    assertEquals(fake.instrument201.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")
    ), "User");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, fake.instrument201.getId()));
    assertEquals("Instrument does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Instrument> result = testManager.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    Collection<Instrument> result = testManager.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Instrument subject = new Instrument();
    subject.setId(UUID.randomUUID());
    subject.setName("shimmy");
    subject.setLibraryId(UUID.randomUUID());

    try {
      testManager.update(access, fake.instrument201.getId(), subject);

    } catch (Exception e) {
      Instrument result = testManager.readOne(HubAccess.internal(), fake.instrument201.getId());
      assertNotNull(result);
      assertEquals("buns", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(ManagerException.class, e.getClass());
    }
  }

  /**
   change volume parameter https://www.pivotaltracker.com/story/show/179215413
   */
  @Test
  public void update_volume() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    fake.instrument201.setVolume(0.74f);

    testManager.update(access, fake.instrument201.getId(), fake.instrument201);

    Instrument result = testManager.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertEquals(0.74f, result.getVolume(), 0.01f);
  }

  @Test
  public void update_addAudio() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    Instrument subject = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "shimmy"));
    test.insert(buildInstrumentAudio(subject, "Test audio", "fake.audio5.wav", 0.0f, 20.f, 120.0f));

    testManager.update(access, fake.instrument201.getId(), subject);

    Instrument result = testManager.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    fake.instrument251 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "jub"));

    testManager.destroy(access, fake.instrument251.getId());

    try {
      testManager.readOne(HubAccess.internal(), fake.instrument251.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_evenWithMemes() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Instrument instrument = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "sandwich"));
    test.insert(buildInstrumentMeme(instrument, "frozen"));
    test.insert(buildInstrumentMeme(instrument, "ham"));

    testManager.destroy(access, instrument.getId());
  }

  /**
   https://www.pivotaltracker.com/story/show/170299297 As long as instrument has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Instrument instrument = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "sandwich"));
    test.insert(buildInstrumentAudio(instrument, "drums", "drums.wav", 0.0f, 1.0f, 120.0f, 0.6f, "bing", "D", 1.0f));

    testManager.destroy(access, instrument.getId());
  }

}
