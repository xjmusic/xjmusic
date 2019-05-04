// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.library.Library;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LibraryIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private LibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "palm tree" has library "leaves" and library "coconuts"
    IntegrationTestEntity.insertAccount(1, "palm tree");
    IntegrationTestEntity.insertLibrary(1, 1, "leaves");
    IntegrationTestEntity.insertLibrary(2, 1, "coconuts");

    // Account "boat" has library "helm" and library "sail"
    IntegrationTestEntity.insertAccount(2, "boat");
    IntegrationTestEntity.insertLibrary(3, 2, "helm");
    IntegrationTestEntity.insertLibrary(4, 2, "sail");

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1L));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
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
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1L));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to create and update a Library.
   */
  @Test(expected = CoreException.class)
  public void create_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "12"
    ));
    Library inputData = new Library()
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts");

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

    Collection<Library> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

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

    Collection<Library> result = testDAO.readAll(access, Lists.newArrayList());

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

    Collection<Library> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setAccountId(BigInteger.valueOf(3L));

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
  @Test(expected = CoreException.class)
  public void update_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "12"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
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
      throw e;
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

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailsIfLibraryHasChilds() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertSequence(301, 101, 2, SequenceType.Main, SequenceState.Published, "brilliant", 0.342, "C#", 0.286);

    try {
      testDAO.destroy(access, BigInteger.valueOf(2L));
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      throw e;
    }
  }
}
