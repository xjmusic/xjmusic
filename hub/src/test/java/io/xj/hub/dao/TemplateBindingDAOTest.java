// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or delete templateBindings

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class TemplateBindingDAOTest {
  private TemplateBindingDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private TemplateBinding templateBinding201;
  private Library targetLibrary;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Template "sandwich" has templateBinding "jams" and templateBinding "buns"
    fake.template1 = test.insert(buildTemplate(fake.account1, "sandwich"));
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var otherLibrary = buildLibrary(fake.account1, "Another");
    TemplateBinding subject = buildTemplateBinding(fake.template1, otherLibrary);

    TemplateBinding result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(otherLibrary.getId(), result.getTargetId());
    assertEquals(ContentBindingType.Library, result.getType());
  }

  @Test
  public void create_cantBindSameContentTwice() throws Exception {
    test.insert(buildTemplateBinding(fake.template1, targetLibrary));
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplateBinding subject = buildTemplateBinding(fake.template1, targetLibrary);

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, subject));
    assertEquals("Found same content already bound to template", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    TemplateBinding result = testDAO.readOne(hubAccess, templateBinding201.getId());

    assertNotNull(result);
    assertEquals(ContentBindingType.Library, result.getType());
    assertEquals(templateBinding201.getId(), result.getId());
    assertEquals(fake.template1.getId(), result.getTemplateId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    Collection<TemplateBinding> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
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
