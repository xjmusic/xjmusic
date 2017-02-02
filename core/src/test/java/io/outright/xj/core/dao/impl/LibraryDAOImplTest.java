// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LibraryDAOImplTest extends Mockito {
//  @Mock private SQLDatabaseProvider sqlDatabaseProvider;
//  private Injector injector;
//  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
//    createInjector();
//    testDAO = injector.getInstance(AccountDAO.class);
  }

  @After
  public void tearDown() throws Exception {
//    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    // ABORT.
    // See [#56] Developer wants functional testing for DAOs because unit testing will create too much dev-time drag
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

//  private void createInjector() {
//    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
//      new AbstractModule() {
//        @Override
//        public void configure() {
//          bind(AccountDAO.class).to(AccountDAOImpl.class);
//          bind(SQLDatabaseProvider.class).toInstance(sqlDatabaseProvider);
//        }
//      }));
//  }
}
