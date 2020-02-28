// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.Instrument;
import io.xj.lib.core.model.InstrumentAudio;
import io.xj.lib.core.model.InstrumentAudioChord;
import io.xj.lib.core.model.InstrumentAudioEvent;
import io.xj.lib.core.model.InstrumentMeme;
import io.xj.lib.core.model.InstrumentState;
import io.xj.lib.core.model.InstrumentType;
import io.xj.lib.core.model.Library;
import io.xj.lib.core.model.User;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.Assert;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.core.testing.InternalResources;
import io.xj.lib.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO;
import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.lib.core.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.lib.core.Tables.INSTRUMENT_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT {
  public WorkManager workManager;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private InstrumentDAO testDAO;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
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

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    Instrument result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(InstrumentType.Percussive, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of instruments
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("cannons fifty nine");
    test.insert(InstrumentAudioEvent.create(fake.audio1, 0, 1, "bing", "C", 1));
    test.insert(InstrumentAudioChord.create(fake.audio1, 0, "G minor"));

    Instrument result = testDAO.clone(access, fake.instrument202.getId(), subject);

    assertNotNull(result);
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Instrument result = testDAO.readOne(access, fake.instrument201.getId());

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.getType());
    assertEquals(InstrumentState.Published, result.getState());
    assertEquals(fake.instrument201.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.instrument201.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");
    Instrument subject = Instrument.create()
      .setName("shimmy")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(access, fake.instrument201.getId(), subject);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), fake.instrument201.getId());
      assertNotNull(result);
      assertEquals("buns", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  @Test
  public void update_addAudio() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = test.insert(Instrument.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setDensity(0.42)
      .setState("Published")
      .setType("Percussive"));
    test.insert(InstrumentAudio.create(subject, "Test audio", "fake.audio5.wav", 0, 2, 120, 300, 0.42));

    testDAO.update(access, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getName());
    assertEquals(fake.library1.getId(), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED for now, awaiting: [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Melodic, InstrumentState.Published, "jenny's jams"));
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, fake.instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), fake.instrument201.getId());
    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getUserId());
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");
    fake.instrument251 = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "jub"));

    testDAO.destroy(access, fake.instrument251.getId());

    Assert.assertNotExist(testDAO, fake.instrument251.getId());
  }

  /**
   [#170299297] Cannot delete Instruments that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    Access access = Access.create("Admin");
    Instrument instrument = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "sandwich"));
    test.insert(InstrumentMeme.create(instrument, "frozen"));
    test.insert(InstrumentMeme.create(instrument, "ham"));

    failure.expect(CoreException.class);
    failure.expectMessage("Found Instrument Meme");

    testDAO.destroy(access, instrument.getId());
  }

  /**
   [#170299297] As long as instrument has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    Access access = Access.create("Admin");
    Instrument instrument = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "sandwich"));
    InstrumentAudio audio= test.insert(InstrumentAudio.create(instrument,"drums","drums.wav",0,1,120,300,0.6));
    test.insert(InstrumentAudioChord.create(audio, 0, "D minor"));
    test.insert(InstrumentAudioEvent.create(audio, 0, 0.5, "bing", "D", 1));

    testDAO.destroy(access, instrument.getId());
  }

}
