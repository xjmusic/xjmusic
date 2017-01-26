// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao.jooq;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.AccountDAO;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.tables.records.AccountRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.ACCOUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AccountDAOImplTest extends Mockito {
  @Mock private SQLDatabaseProvider sqlDatabaseProvider;
  private Injector injector;
  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
    createInjector();
    testDAO = injector.getInstance(AccountDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Connection connection = new MockConnection(c -> {
      if (c.sql().toLowerCase().startsWith("insert")) {
        assertEquals("manuts", c.bindings()[0]);
      } else if (c.sql().toLowerCase().startsWith("select last_insert_id()")){
        Field<BigInteger> id = DSL.field("last_insert_id()", BigInteger.class);
        Record record = DSL.using(SQLDialect.MYSQL).newRecord(id);
        record.setValue(id, BigInteger.valueOf(5));
        return new MockResult[] { new MockResult(record) };
      }
      return new MockResult[]{};
    });
    DSLContext db = DSL.using(connection, SQLDialect.MYSQL);
    when(sqlDatabaseProvider.getConnectionTransaction())
      .thenReturn(connection);
    when(sqlDatabaseProvider.getContext(connection))
      .thenReturn(db);

    Account inputData = new Account();
    inputData.setName("manuts");
    AccountWrapper inputDataWrapper = new AccountWrapper();
    inputDataWrapper.setAccount(inputData);

    JSONObject actualResult = testDAO.create(inputDataWrapper);

    verify(sqlDatabaseProvider).commitAndClose(connection);
    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(5), actualResult.get("id"));
    assertEquals("manuts", actualResult.get("name"));
  }

  @Test
  public void readOneAble() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles","user",
      "accounts","1"
    ));
    Connection connection = new MockConnection(c -> {
      AccountRecord record= DSL.using(SQLDialect.MYSQL).newRecord(ACCOUNT);
      record.setId(ULong.valueOf(5));
      record.setName("manuts");
      return new MockResult[] { new MockResult(record) };
    });
    DSLContext mockContext = DSL.using(connection, SQLDialect.MYSQL);
    when(sqlDatabaseProvider.getConnection())
      .thenReturn(connection);
    when(sqlDatabaseProvider.getContext(connection))
      .thenReturn(mockContext);

    JSONObject actualResult = testDAO.readOneAble(access, ULong.valueOf(5));

    verify(sqlDatabaseProvider).close(connection);
    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(5), actualResult.get("id"));
    assertEquals("manuts", actualResult.get("name"));
  }

  @Test(expected = DatabaseException.class)
  public void readOneAble_FailureConnectionToDatabase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles","user",
      "accounts","1"
    ));
    Connection connection = new MockConnection(c -> {
      throw new SQLException("network failure");
    });
    DSLContext mockContext = DSL.using(connection, SQLDialect.MYSQL);
    when(sqlDatabaseProvider.getConnection())
      .thenReturn(connection);
    when(sqlDatabaseProvider.getContext(connection))
      .thenReturn(mockContext);

    try {
      testDAO.readOneAble(access, ULong.valueOf(5));
    } catch (Exception e) {
      // Even if SQL statement execution throws exception, connection ought to be closed afterwards.
      verify(sqlDatabaseProvider).close(connection);
      throw e;
    }
  }

  @Test
  public void readOneVisible() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
  }

  @Test
  public void readAll() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
  }

  @Test
  public void readAllVisible() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
  }

  @Test
  public void update() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
  }

  @Test
  public void delete() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AccountDAO.class).to(AccountDAOImpl.class);
          bind(SQLDatabaseProvider.class).toInstance(sqlDatabaseProvider);
        }
      }));
  }
}
