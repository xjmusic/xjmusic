// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete templateBindings

// FUTURE: any test that

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TemplateBindingManagerDbTest {
  TemplateBindingManager testManager;
  HubIntegrationTest test;
  IntegrationTestingFixtures fake;
  TemplateBinding templateBinding201;
  Library targetLibrary;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
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
    testManager = new TemplateBindingManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var otherLibrary = buildLibrary(fake.account1, "Another");
    TemplateBinding subject = buildTemplateBinding(fake.template1, otherLibrary);

    TemplateBinding result = testManager.create(access, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(otherLibrary.getId(), result.getTargetId());
    assertEquals(ContentBindingType.Library, result.getType());
  }

  @Test
  public void create_cantBindSameContentTwice() throws Exception {
    test.insert(buildTemplateBinding(fake.template1, targetLibrary));
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplateBinding subject = buildTemplateBinding(fake.template1, targetLibrary);

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, subject));
    assertEquals("Found same content already bound to template", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    TemplateBinding result = testManager.readOne(access, templateBinding201.getId());

    assertNotNull(result);
    assertEquals(ContentBindingType.Library, result.getType());
    assertEquals(templateBinding201.getId(), result.getId());
    assertEquals(fake.template1.getId(), result.getTemplateId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")
    ), "User");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, templateBinding201.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<TemplateBinding> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfTemplate() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    Collection<TemplateBinding> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplateBinding subject = test.insert(buildTemplateBinding(fake.template1, targetLibrary));

    assertThrows(ManagerException.class, () -> testManager.update(access, subject.getId(), subject));
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    TemplateBinding templateBinding251 = buildTemplateBinding(fake.template1, targetLibrary);

    testManager.destroy(access, templateBinding251.getId());

    try {
      testManager.readOne(HubAccess.internal(), templateBinding251.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
    }
  }

}
