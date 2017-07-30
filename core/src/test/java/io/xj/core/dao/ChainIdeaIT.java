// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_idea.ChainIdea;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.tables.records.ChainIdeaRecord;
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
import java.sql.Timestamp;

import static io.xj.core.tables.ChainIdea.CHAIN_IDEA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different libraries to readMany vs. create vs. update or delete chain libraries
public class ChainIdeaIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChainIdeaDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.52714"), Timestamp.valueOf("2015-06-09 12:17:01.0475"));

    // Account "blocks" has chain "red"
    IntegrationTestEntity.insertAccount(2, "blocks");
    IntegrationTestEntity.insertChain(3, 2, "red", ChainType.Production, ChainState.Complete, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"));

    // Stub users
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");

    // Library "palm tree" has idea "fonds" and idea "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Main, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, IdeaType.Rhythm, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has idea "helm" and idea "sail"
    IntegrationTestEntity.insertLibrary(2, 2, "boat");
    IntegrationTestEntity.insertIdea(3, 3, 2, IdeaType.Macro, "helm", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 2, 2, IdeaType.Support, "sail", 0.342, "C#", 0.286);

    // Chain "school" has idea "helm"
    IntegrationTestEntity.insertChainIdea(1, 1, 3);

    // Chain "bucket" has ideas "fonds" and "nuts"
    IntegrationTestEntity.insertChainIdea(2, 2, 1);
    IntegrationTestEntity.insertChainIdea(3, 2, 2);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainIdeaDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setChainId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(2), result.get("ideaId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setChainId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setChainId(BigInteger.valueOf(3))
      .setIdeaId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInIdeaAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setChainId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setIdeaId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutIdeaId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainIdea inputData = new ChainIdea()
      .setChainId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    ChainIdea result = new ChainIdea().setFromRecord(testDAO.readOne(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getId());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals(ULong.valueOf(3), result.getIdeaId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    ChainIdeaRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals(ULong.valueOf(1), result1.get("ideaId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(ULong.valueOf(2), result2.get("ideaId"));
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
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChainIdeaRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN_IDEA)
      .where(CHAIN_IDEA.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "idea",
      "accounts", "5"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }
}
