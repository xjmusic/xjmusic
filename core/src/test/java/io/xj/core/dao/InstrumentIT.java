// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.work.WorkManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. create vs. update or delete instruments

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
    insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newUserRole(3, UserRoleType.User));
    insert(newAccountUser(1, 3));

    // Library "sandwich" has instrument "jams" and instrument "buns"
    insert(newLibrary(1, 1, "sandwich", now()));
    insert(newInstrument(2, 3, 1, InstrumentType.Harmonic, InstrumentState.Published, "buns", now()));
    Instrument instrument1 = newInstrument(1, 3, 1, InstrumentType.Percussive, InstrumentState.Published, "jams", now());
    instrument1.add(newInstrumentMeme("smooth"));
    instrument1.add(newAudio("Test audio", "audio5.wav", 0, 2, 120, 300, 0.5));
    insert(instrument1);

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("shimmy")
      .setState("Published")
      .setType("Percussive");

    Instrument result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("shimmy", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("cannons fifty nine");

    Instrument result = testDAO.clone(access, BigInteger.valueOf(1L), subject);

    assertNotNull(result);
    assertEquals(0.5, result.getDensity(), 0.01);
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Instrument result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.getType());
    assertEquals(InstrumentState.Published, result.getState());
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("buns", result.getDescription());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Instrument> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument()
      .setDescription("shimmy")
      .setLibraryId(BigInteger.valueOf(387L));

    try {
      testDAO.update(access, BigInteger.valueOf(2L), subject);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("buns", result.getDescription());
      assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, BigInteger.valueOf(2L), subject);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("shimmy", result.getDescription());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  @Test
  public void update_addAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument(BigInteger.valueOf(2L))
      .setUserId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("shimmy")
      .setState("Published")
      .setType("Percussive");
    subject.add(newAudio("Test audio", "audio5.wav", 0, 2, 120, 300, 0.42));

    testDAO.update(access, BigInteger.valueOf(2L), subject);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.1);
    assertEquals("shimmy", result.getDescription());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was created, even after being updated by another user.
   DEPRECATED for now, awaiting: [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    insert(newInstrument(3, 3, 1, InstrumentType.Melodic, InstrumentState.Published, "jenny's jams", now()));
    Access access = new Access(ImmutableMap.of(
      "userId", "2", // John will update an instrument belonging to Jenny
      "roles", "User",
      "accounts", "1"
    ));
    Instrument subject = instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("shimmy")
      .setState("Published")
      .setType("Percussive");

    testDAO.update(access, BigInteger.valueOf(3L), subject);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newInstrument(86, 3, 1, InstrumentType.Harmonic, InstrumentState.Published, "jub", now()));

    testDAO.destroy(access, BigInteger.valueOf(86L));

    assertNotExist(testDAO, BigInteger.valueOf(86L));
  }

  @Test
  public void delete_SucceedsIfInstrumentHasChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Instrument instrument = newInstrument(86, 3, 1, InstrumentType.Harmonic, InstrumentState.Published, "sandwich", now());
    instrument.add(newInstrumentMeme("frozen"));
    instrument.add(newInstrumentMeme("ham"));
    insert(instrument);

    testDAO.destroy(access, BigInteger.valueOf(86L));
  }

}
