// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.external.AuthType;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.idea_meme.IdeaMeme;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.IdeaMemeRecord;
import io.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.xj.core.tables.IdeaMeme.IDEA_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to readMany vs. create vs. update or delete idea memes
public class IdeaMemeIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private IdeaMemeDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.USER);
    IntegrationTestEntity.insertUserRole(2, 2, Role.ADMIN);
    IntegrationTestEntity.insertAccountUser(3, 1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, AuthType.GOOGLE, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(4, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(5, 1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(6, 4, Role.USER);

    // Library "palm tree" has idea "leaves", idea "coconuts" and idea "bananas"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Main, "leaves", 0.342, "C#", 120.4);
    IntegrationTestEntity.insertIdea(2, 2, 1, IdeaType.Main, "coconuts", 0.25, "F#", 110.3);
    IntegrationTestEntity.insertIdea(3, 2, 1, IdeaType.Main, "bananas", 0.27, "Gb", 100.6);

    // Idea "leaves" has memes "ants" and "mold"
    IntegrationTestEntity.insertIdeaMeme(1, 1, "Ants");
    IntegrationTestEntity.insertIdeaMeme(2, 1, "Mold");

    // Idea "bananas" has meme "peel"
    IntegrationTestEntity.insertIdeaMeme(3, 3, "Peel");

    // Instantiate the test subject
    testDAO = injector.getInstance(IdeaMemeDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMeme inputData = new IdeaMeme()
      .setIdeaId(BigInteger.valueOf(1))
      .setName("  !!2gnarLY    ");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("ideaId"));
    assertEquals("Gnarly", result.get("name"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutIdeaID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMeme inputData = new IdeaMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMeme inputData = new IdeaMeme()
      .setIdeaId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    IdeaMeme result = new IdeaMeme().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getIdeaId());
    assertEquals("Mold", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    IdeaMemeRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("Ants", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("Mold", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    testDAO.delete(access, ULong.valueOf(1));

    IdeaMemeRecord result = IntegrationTestService.getDb()
      .selectFrom(IDEA_MEME)
      .where(IDEA_MEME.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }
}
