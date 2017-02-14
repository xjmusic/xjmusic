// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.integration.IntegrationTestEntity;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    // TODO: test AccountDAOImpl create()
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles","user",
      "accounts","1"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("id"));
    assertEquals("bananas", actualResult.get("name"));
  }

  @Test
  public void readAll() throws Exception {
    // TODO: test AccountDAOImpl readAll()
  }

  @Test
  public void update() throws Exception {
    // TODO: test AccountDAOImpl update()
  }

  @Test
  public void delete() throws Exception {
    // TODO: test AccountDAOImpl delete()
    // TODO: test AccountDAOImpl delete() fails if account has child records
  }

}
