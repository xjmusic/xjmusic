// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.instrument;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.Library;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.work.WorkManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT extends FixtureIT {
  @Spy
  final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private InstrumentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    // inject mocks
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));

    // Account "bananas"
    account1 = insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    insert(UserRole.create(user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(UserRole.create(user3, UserRoleType.User));
    insert(AccountUser.create(account1, user3));

    // Library "sandwich" has instrument "jams" and instrument "buns"
    library1 = insert(Library.create(account1, "sandwich", now()));
    instrument201 = insert(Instrument.create(user3, library1, InstrumentType.Harmonic, InstrumentState.Published, "buns"));
    instrument202 = insert(Instrument.create(user3, library1, InstrumentType.Percussive, InstrumentState.Published, "jams"));
    insert(InstrumentMeme.create(instrument202, "smooth"));
    insert(InstrumentAudio.create(instrument202, "Test audio", "audio5.wav", 0, 2, 120, 300, 0.5));

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(user3.getId())
      .setLibraryId(library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    Instrument result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(library1.getId(), result.getLibraryId());
    assertEquals("shimmy", result.getName());
    assertEquals(InstrumentType.Percussive, result.getType());
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(user3.getId())
      .setLibraryId(library1.getId())
      .setName("cannons fifty nine");

    Instrument result = testDAO.clone(access, instrument202.getId(), subject);

    // TODO assert that cloning includes all audio, event, and meme

    assertNotNull(result);
    assertEquals(library1.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(InstrumentType.Percussive, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    Instrument result = testDAO.readOne(access, instrument201.getId());

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.getType());
    assertEquals(InstrumentState.Published, result.getState());
    assertEquals(instrument201.getId(), result.getId());
    assertEquals(library1.getId(), result.getLibraryId());
    assertEquals("buns", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, instrument201.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Admin");

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(library1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");
    Instrument subject = Instrument.create()
      .setName("shimmy")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(access, instrument201.getId(), subject);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), instrument201.getId());
      assertNotNull(result);
      assertEquals("buns", result.getName());
      assertEquals(library1.getId(), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(user3.getId())
      .setLibraryId(library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), instrument201.getId());
    assertNotNull(result);
    assertEquals("shimmy", result.getName());
    assertEquals(library1.getId(), result.getLibraryId());
  }

  @Test
  public void update_addAudio() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Instrument subject = insert(Instrument.create()
      .setUserId(user3.getId())
      .setLibraryId(library1.getId())
      .setName("shimmy")
      .setDensity(0.42)
      .setState("Published")
      .setType("Percussive"));
    insert(InstrumentAudio.create(subject, "Test audio", "audio5.wav", 0, 2, 120, 300, 0.42));

    testDAO.update(access, instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), instrument201.getId());
    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getName());
    assertEquals(library1.getId(), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED for now, awaiting: [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    insert(Instrument.create(user3, library1, InstrumentType.Melodic, InstrumentState.Published, "jenny's jams"));
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Instrument subject = Instrument.create()
      .setUserId(user3.getId())
      .setLibraryId(library1.getId())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, instrument201.getId(), subject);

    Instrument result = testDAO.readOne(Access.internal(), instrument201.getId());
    assertNotNull(result);
    assertEquals(user3.getId(), result.getUserId());
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");
    instrument251 = insert(Instrument.create(user3, library1, InstrumentType.Harmonic, InstrumentState.Published, "jub"));

    testDAO.destroy(access, instrument251.getId());

    assertNotExist(testDAO, instrument251.getId());
  }

  @Test
  public void destroy_FailsIfInstrumentHasMemes() throws Exception {
    Access access = Access.create("Admin");
    Instrument instrument = insert(Instrument.create(user3, library1, InstrumentType.Harmonic, InstrumentState.Published, "sandwich"));
    insert(InstrumentMeme.create(instrument, "frozen"));
    insert(InstrumentMeme.create(instrument, "ham"));

    failure.expect(CoreException.class);
    failure.expectMessage("Found MemeEntity");

    testDAO.destroy(access, instrument.getId());
  }

}
