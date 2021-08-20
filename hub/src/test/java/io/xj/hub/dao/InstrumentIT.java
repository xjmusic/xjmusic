// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentMeme;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.User;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
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

import static io.xj.hub.tables.InstrumentAudio.INSTRUMENT_AUDIO;
import static io.xj.hub.tables.InstrumentMeme.INSTRUMENT_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT {
  private InstrumentDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("bananas")
    );
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("john")
      .email("john@email.com")
      .avatarUrl("http://pictures.com/john.gif")
    );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.ADMIN)
    );

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("jenny")
      .email("jenny@email.com")
      .avatarUrl("http://pictures.com/jenny.gif")
    );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.USER)
    );
    test.insert(new AccountUser()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .userId(fake.user3.getId())
    );

    // Library "sandwich" has instrument "jams" and instrument "buns"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("sandwich")
    );
    fake.instrument201 = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(InstrumentType.PAD)
      .density(0.6)
      .state(InstrumentState.PUBLISHED)
      .name("buns")
    );
    fake.instrument202 = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(InstrumentType.PERCUSSIVE)
      .density(0.6)
      .state(InstrumentState.PUBLISHED)
      .name("jams")
    );
    test.insert(new InstrumentMeme()
      .id(UUID.randomUUID())
      .instrumentId(fake.instrument202.getId())
      .name("smooth")
    );
    fake.audio1 = test.insert(new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(fake.instrument202.getId())
      .name("Test audio")
      .waveformKey("fake.audio5.wav")
      .start(0.0)
      .length(2.0)
      .tempo(120.0)
      .density(0.5)
    );

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
    Instrument subject = new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .name("shimmy")
      .density(0.6)
      .state(InstrumentState.PUBLISHED)
      .type(InstrumentType.PERCUSSIVE);

    Instrument result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(InstrumentType.PERCUSSIVE, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of instruments
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .name("cannons fifty nine");

    Instrument result = testDAO.clone(hubAccess, fake.instrument202.getId(), subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(InstrumentType.PERCUSSIVE, result.getType());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Instrument result = testDAO.readOne(hubAccess, fake.instrument201.getId());

    assertNotNull(result);
    assertEquals(InstrumentType.PAD, result.getType());
    assertEquals(InstrumentState.PUBLISHED, result.getState());
    assertEquals(fake.instrument201.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, fake.instrument201.getId()));
    assertEquals("Record does not exist", e.getMessage());
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    Collection<Instrument> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Instrument subject = new Instrument()
      .id(UUID.randomUUID())
      .name("shimmy")
      .libraryId(UUID.randomUUID());

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
    Instrument subject = new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .name("shimmy")
      .state(InstrumentState.PUBLISHED)
      .type(InstrumentType.PERCUSSIVE);

    testDAO.update(hubAccess, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(HubAccess.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void update_addAudio() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .name("shimmy")
      .density(0.42)
      .state(InstrumentState.PUBLISHED)
      .type(InstrumentType.PERCUSSIVE)
    );
    test.insert(new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(subject.getId())
      .name("Test audio")
      .waveformKey("fake.audio5.wav")
      .start(0.0)
      .length(2.0)
      .tempo(120.0)
      .density(0.42)
    );

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
    fake.instrument251 = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(InstrumentType.PAD)
      .density(0.6)
      .state(InstrumentState.PUBLISHED)
      .name("jub")
    );

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
    Instrument instrument = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .density(0.6)
      .type(InstrumentType.PAD)
      .state(InstrumentState.PUBLISHED)
      .name("sandwich")
    );
    test.insert(new InstrumentMeme()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name("frozen")
    );
    test.insert(new InstrumentMeme()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name("ham")
    );

    var e = assertThrows(DAOException.class, () -> testDAO.destroy(hubAccess, instrument.getId()));
    assertEquals("Found Instrument Memes", e.getMessage());

  }

  /**
   [#170299297] As long as instrument has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Instrument instrument = test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(InstrumentType.PAD)
      .density(0.6)
      .state(InstrumentState.PUBLISHED)
      .name("sandwich")
    );
    test.insert(new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name("drums")
      .waveformKey("drums.wav")
      .start(0.0)
      .length(1.0)
      .tempo(120.0)
      .density(0.6)
      .event("bing")
      .note("D")
      .volume(1.0)
    );

    testDAO.destroy(hubAccess, instrument.getId());
  }

}
