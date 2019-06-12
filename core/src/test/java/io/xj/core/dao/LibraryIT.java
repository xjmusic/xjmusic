// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.library.Library;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class LibraryIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private LibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "palm tree" has library "leaves" and library "coconuts"
    insert(newAccount(1, "palm tree"));
    insert(newLibrary(1, 1, "leaves", now()));
    insert(newLibrary(2, 1, "coconuts", now()));

    // Account "boat" has library "helm" and library "sail"
    insert(newAccount(2, "boat"));
    insert(newLibrary(3, 2, "helm", now()));
    insert(newLibrary(4, 2, "sail", now()));

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(BigInteger.valueOf(1L));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to create and update a Library.
   */
  @Test
  public void create_asEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "1"
    ));
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(BigInteger.valueOf(1L));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to create and update a Library.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "12"
    ));
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(BigInteger.valueOf(1L));

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("coconuts");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Library result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Library> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Library> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readAll_fromAllAccounts() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1,2"
    ));

    Collection<Library> result = testDAO.readMany(access, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Library> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Library> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setAccountId(BigInteger.valueOf(3L));

    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to create and update a Library.
   */
  @Test
  public void update_asEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "1"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to create and update a Library.
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "12"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1L));

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(3978L));

    try {
      testDAO.update(access, BigInteger.valueOf(3L), inputData);

    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(BigInteger.valueOf(2L), result.getAccountId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("trunk")
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_FailsIfLibraryHasProgram() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif"));
    insert(newProgram(301, 101, 2, ProgramType.Main, ProgramState.Published, "brilliant", "C#", 120.0, now()));

    try {
      testDAO.destroy(access, BigInteger.valueOf(2L));
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertSame(CoreException.class, e.getClass());
      assertEquals("Found Program in Library", e.getMessage());
    }
  }

  @Test
  public void delete_FailsIfLibraryHasInstrument() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif"));
    insert(newInstrument(301, 101, 2, InstrumentType.Percussive, InstrumentState.Published, "brilliant", now()));

    try {
      testDAO.destroy(access, BigInteger.valueOf(2L));
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertSame(CoreException.class, e.getClass());
      assertEquals("Found Instrument in Library", e.getMessage());
    }
  }
}
