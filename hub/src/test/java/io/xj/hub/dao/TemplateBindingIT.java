// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.ContentBindingType;
import io.xj.api.Library;
import io.xj.api.Template;
import io.xj.api.TemplateBinding;
import io.xj.api.User;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;


import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserRole;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete templateBindings

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class TemplateBindingIT {
  private TemplateBindingDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private TemplateBinding templateBinding201;
  private Library targetLibrary;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(buildUserRole(fake.user2,UserRoleType.ADMIN));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(buildUserRole(fake.user3,UserRoleType.USER));
    test.insert(buildAccountUser(fake.account1,fake.user3));

    // Template "sandwich" has templateBinding "jams" and templateBinding "buns"
    fake.template1 = test.insert(new Template()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("sandwich"));
    targetLibrary = test.insert(buildLibrary(fake.account1, "Test Library"));
    templateBinding201 = test.insert(buildTemplateBinding(fake.template1, targetLibrary));

    // Instantiate the test subject
    testDAO = injector.getInstance(TemplateBindingDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var otherLibrary = buildLibrary(fake.account1, "Another");
    TemplateBinding subject = buildTemplateBinding(fake.template1, otherLibrary);

    TemplateBinding result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(otherLibrary.getId(), result.getTargetId());
    assertEquals(ContentBindingType.LIBRARY, result.getType());
  }

  @Test
  public void create_cantBindSameContentTwice() throws Exception {
    test.insert(buildTemplateBinding(fake.template1, targetLibrary));
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    TemplateBinding subject = buildTemplateBinding(fake.template1, targetLibrary);

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, subject));
    assertEquals("Found same content already bound to template", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    TemplateBinding result = testDAO.readOne(hubAccess, templateBinding201.getId());

    assertNotNull(result);
    assertEquals(ContentBindingType.LIBRARY, result.getType());
    assertEquals(templateBinding201.getId(), result.getId());
    assertEquals(fake.template1.getId(), result.getTemplateId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, templateBinding201.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<TemplateBinding> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    Collection<TemplateBinding> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    TemplateBinding subject = test.insert(buildTemplateBinding(fake.template1, targetLibrary));

    assertThrows(DAOException.class, () -> testDAO.update(hubAccess, subject.getId(), subject));
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    TemplateBinding templateBinding251 = buildTemplateBinding(fake.template1, targetLibrary);

    testDAO.destroy(hubAccess, templateBinding251.getId());

    try {
      testDAO.readOne(HubAccess.internal(), templateBinding251.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

}
