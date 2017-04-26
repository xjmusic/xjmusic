// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.external.AuthType;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.idea_meme.IdeaMeme;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.IdeaMemeRecord;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.outright.xj.core.tables.IdeaMeme.IDEA_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete idea memes
public class IdeaMemeIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private IdeaMemeDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1,2, Role.USER);
    IntegrationTestEntity.insertUserRole(2,2, Role.ADMIN);
    IntegrationTestEntity.insertAccountUser(3,1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, AuthType.GOOGLE, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(4,3, Role.USER);
    IntegrationTestEntity.insertAccountUser(5,1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(6,4, Role.USER);

    // Library "palm tree" has idea "leaves", idea "coconuts" and idea "bananas"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 120.4);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.MAIN, "coconuts", 0.25, "F#", 110.3);
    IntegrationTestEntity.insertIdea(3, 2, 1, Idea.MAIN, "bananas", 0.27, "Gb", 100.6);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMemeWrapper inputDataWrapper = new IdeaMemeWrapper()
      .setIdeaMeme(new IdeaMeme()
        .setIdeaId(BigInteger.valueOf(1))
        .setName("  !!2gnarLY    ")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.get("ideaId"));
    assertEquals("Gnarly", result.get("name"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutIdeaID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMemeWrapper inputDataWrapper = new IdeaMemeWrapper()
      .setIdeaMeme(new IdeaMeme()
        .setName("gnarly")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    IdeaMemeWrapper inputDataWrapper = new IdeaMemeWrapper()
      .setIdeaMeme(new IdeaMeme()
        .setIdeaId(BigInteger.valueOf(1))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("ideaId"));
    assertEquals("Mold", result.get("name"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("Ants", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("Mold", actualResult2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    testDAO.delete(access, ULong.valueOf(1));

    IdeaMemeRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA_MEME)
      .where(IDEA_MEME.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }
}
