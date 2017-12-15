// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LibraryIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private LibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

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

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1));

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals("manuts", result.getName());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("manuts");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Library result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Library result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("leaves", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("coconuts", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setAccountId(BigInteger.valueOf(3));

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(3978));

    try {
      testDAO.update(access, BigInteger.valueOf(3), inputData);

    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(BigInteger.valueOf(2), result.getAccountId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Library inputData = new Library()
      .setName("trunk")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.delete(access, BigInteger.valueOf(1));

    Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfLibraryHasChilds() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertPattern(301, 101, 2, PatternType.Main, "brilliant", 0.342, "C#", 0.286);

    try {
      testDAO.delete(access, BigInteger.valueOf(2));
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      throw e;
    }
  }
}
