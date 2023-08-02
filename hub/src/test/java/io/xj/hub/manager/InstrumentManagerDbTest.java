// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrument;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentAudio;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.tables.InstrumentAudio.INSTRUMENT_AUDIO;
import static io.xj.hub.tables.InstrumentMeme.INSTRUMENT_MEME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class InstrumentManagerDbTest {
  InstrumentManager subject;
  HubIntegrationTest test;
  IntegrationTestingFixtures fake;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Accounts
    fake.account1 = test.insert(buildAccount("bananas"));
    fake.account2 = test.insert(buildAccount("apples"));

    // User
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "sandwich" has instruments "jams" and instrument "buns"
    fake.library1 = test.insert(buildLibrary(fake.account1, "sandwich"));
    fake.instrument201 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "jams"));
    fake.audio1 = test.insert(buildInstrumentAudio(fake.instrument202, "Test audio", "fake.audio5.wav", 0.0f, 2.0f, 120.0f));

    // Library in different account
    fake.library3 = test.insert(buildLibrary(fake.account2, "car"));

    // Instantiate the test subject
    subject = new InstrumentManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    Instrument input = new Instrument();
    input.setId(UUID.randomUUID());
    input.setLibraryId(fake.library1.getId());
    input.setName("shimmy");
    input.setVolume(0.54f);
    input.setDensity(0.6f);
    input.setState(InstrumentState.Published);
    input.setMode(InstrumentMode.Event);
    input.setType(InstrumentType.Drum);

    Instrument result = subject.create(
      access, input);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(0.54f, result.getVolume(), 0.01);
    assertEquals(InstrumentType.Drum, result.getType());
    assertEquals(InstrumentMode.Event, result.getMode());
  }

  /**
   * Overall volume parameter defaults to 1.0 https://www.pivotaltracker.com/story/show/179215413
   */
  @Test
  public void create_defaultVolume() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    Instrument input = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "shimmy");

    Instrument result = subject.create(
      access, input);

    assertEquals(1.0f, result.getVolume(), 0.01);
  }

  /**
   * Instruments/Instruments can be cloned/moved between accounts https://www.pivotaltracker.com/story/show/181878883
   */
  @Test
  public void clone_toLibraryInDifferentAccount() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1, fake.account2));
    Instrument input = new Instrument();
    input.setLibraryId(fake.library3.getId());
    input.setName("porcupines");

    ManagerCloner<Instrument> resultCloner = subject.clone(access, fake.instrument202.getId(), input);

    Instrument result = subject.readOne(HubAccess.internal(), resultCloner.getClone().getId());
    assertEquals(fake.library3.getId(), result.getLibraryId());
    assertEquals("porcupines", result.getName());
  }

  /**
   * Clone sub-entities of instruments https://www.pivotaltracker.com/story/show/170290553
   * Cloning an Instrument should not reset its Parameters https://www.pivotaltracker.com/story/show/180764355
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    test.insert(buildInstrumentMeme(fake.instrument202, "chunk"));
    Instrument input = new Instrument();
    input.setId(UUID.randomUUID());
    input.setLibraryId(fake.library1.getId());
    input.setConfig(
      """
        isMultiphonic = true
        isOneShot = false
        isTonal = false
        oneShotObserveLengthOfEvents = [TEST]
          """
    );
    input.setName("cannons fifty nine");

    ManagerCloner<Instrument> result = subject.clone(access, fake.instrument202.getId(), input);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getClone().getLibraryId());
    assertEquals("cannons fifty nine", result.getClone().getName());
    assertEquals(InstrumentType.Drum, result.getClone().getType());
    assertArrayEquals(
      new String[]{
        "attackMillis = 1",
        "isAudioSelectionPersistent = true",
        "isMultiphonic = true",
        "isOneShot = false",
        "isOneShotCutoffEnabled = true",
        "isTonal = false",
        "oneShotObserveLengthOfEvents = [TEST]",
        "releaseMillis = 5",
      },
      result.getClone().getConfig().split(System.lineSeparator())
    );
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(INSTRUMENT_MEME)
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(result.getClone().getId()))
        .fetchOne(0, int.class));
    }
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(result.getClone().getId()))
        .fetchOne(0, int.class));
    }
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    Instrument result = subject.readOne(access, fake.instrument201.getId());

    assertNotNull(result);
    assertEquals(InstrumentType.Pad, result.getType());
    assertEquals(InstrumentState.Published, result.getState());
    assertEquals(fake.instrument201.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(buildAccount("Testing")
    ), "User");

    var e = assertThrows(ManagerException.class, () -> subject.readOne(access, fake.instrument201.getId()));
    assertEquals("Instrument does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");

    Collection<Instrument> result = subject.readMany(access, List.of(fake.library1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(buildAccount("Testing")), "User");

    Collection<Instrument> result = subject.readMany(access, List.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");
    Instrument input = new Instrument();
    input.setId(UUID.randomUUID());
    input.setName("shimmy");
    input.setLibraryId(UUID.randomUUID());

    try {
      subject.update(access, fake.instrument201.getId(), input);

    } catch (Exception e) {
      Instrument result = subject.readOne(HubAccess.internal(), fake.instrument201.getId());
      assertNotNull(result);
      assertEquals("buns", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(ManagerException.class, e.getClass());
    }
  }

  /**
   * change volume parameter https://www.pivotaltracker.com/story/show/179215413
   */
  @Test
  public void update_volume() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    fake.instrument201.setVolume(0.74f);

    subject.update(access, fake.instrument201.getId(), fake.instrument201);

    Instrument result = subject.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertEquals(0.74f, result.getVolume(), 0.01f);
  }

  @Test
  public void update_addAudio() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    Instrument input = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "shimmy"));
    test.insert(buildInstrumentAudio(input, "Test audio", "fake.audio5.wav", 0.0f, 20.f, 120.0f));

    subject.update(access, fake.instrument201.getId(), input);

    Instrument result = subject.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    fake.instrument251 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "jub"));

    subject.destroy(access, fake.instrument251.getId());

    try {
      subject.readOne(HubAccess.internal(), fake.instrument251.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
    }
  }

  @Test
  public void destroy_evenWithMemes() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Instrument instrument = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "sandwich"));
    test.insert(buildInstrumentMeme(instrument, "frozen"));
    test.insert(buildInstrumentMeme(instrument, "ham"));

    subject.destroy(access, instrument.getId());
  }

  /**
   * As long as instrument has no meme, destroy all other inner entities https://www.pivotaltracker.com/story/show/170299297
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Instrument instrument = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "sandwich"));
    test.insert(buildInstrumentAudio(instrument, "drums", "drums.wav", 0.0f, 1.0f, 120.0f, 0.6f, "bing", "D", 1.0f));

    subject.destroy(access, instrument.getId());
  }

}
