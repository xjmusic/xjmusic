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
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.user_role.UserRoleType;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

// future test: permissions of different users to readMany vs. create vs. update or delete instruments
@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT {
  @Spy
  final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private InstrumentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "sandwich" has instrument "jams" and instrument "buns"
    IntegrationTestEntity.insertLibrary(1, 1, "sandwich");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "buns", InstrumentType.Harmonic, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(1, "smooth");

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2L));

    Instrument result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("bimmies", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutUserID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType("Percussive")
      .setLibraryId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("cannons fifty nine");

    Instrument result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.01);
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());

    // Verify enqueued audio clone jobs
    verify(workManager).doInstrumentClone(eq(BigInteger.valueOf(1L)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Instrument result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
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

    Collection<Instrument> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Instrument> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setLibraryId(BigInteger.valueOf(3L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies")
      .setLibraryId(BigInteger.valueOf(387L));

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("buns", result.getDescription());
      assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("bimmies", result.getDescription());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was created, even after being updated by another user.
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    IntegrationTestEntity.insertInstrument(3, 1, 3, "jenny's jams", InstrumentType.Melodic, 0.6);
    Access access = new Access(ImmutableMap.of(
      "userId", "2", // John will update an instrument belonging to Jenny
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3L), result.getUserId());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "jub", InstrumentType.Harmonic, 0.4);

    testDAO.destroy(access, BigInteger.valueOf(86L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(86L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailsIfInstrumentHasChilds() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "hamsicle", InstrumentType.Harmonic, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(86, "frozen");
    IntegrationTestEntity.insertInstrumentMeme(86, "ham");

    try {
      testDAO.destroy(access, BigInteger.valueOf(86L));

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(86L));
      assertNotNull(result);
      throw e;
    }

  }

}
