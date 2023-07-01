// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.tables.pojos.TemplatePlayback;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplatePlayback;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// future test: permissions of different users to readMany vs. of vs. update or delete templatePlaybacks

// FUTURE: any test that

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TemplatePlaybackManagerDbTest {
  private TemplatePlaybackManager testManager;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;
  private TemplatePlayback templatePlayback201;

  @MockBean
  private PreviewNexusAdmin previewNexusAdmin;

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
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "User, Artist"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Template "sandwich" has templatePlayback "jams" and templatePlayback "buns"
    fake.template1 = test.insert(buildTemplate(fake.account1, TemplateType.Preview, "sandwich", "sandwich55"));

    test.insert(buildTemplate(fake.account1, TemplateType.Preview, "Test Template", UUID.randomUUID().toString()));
    templatePlayback201 = test.insert(buildTemplatePlayback(fake.template1, fake.user2));

    // Instantiate the test subject
    testManager = new TemplatePlaybackManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider(), previewNexusAdmin, 14400);
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create_alwaysTakesUserFromHubAccess() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplatePlayback subject = buildTemplatePlayback(fake.template1, fake.user3); // user will be overridden by hub access user id

    TemplatePlayback result = testManager.create(access, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
    verify(previewNexusAdmin, times(1)).startPreviewNexus(any(), any());
  }

  @Test
  public void create_withoutSpecifyingUser() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplatePlayback subject = new TemplatePlayback();
    subject.setId(UUID.randomUUID());
    subject.setTemplateId(fake.template1.getId());

    TemplatePlayback result = testManager.create(access, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
    verify(previewNexusAdmin, times(1)).startPreviewNexus(any(), any());
  }

  @Test
  public void create_cannotPlaybackProductionChain() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var template5 = test.insert(buildTemplate(fake.account1, TemplateType.Production, "test", UUID.randomUUID().toString()));

    TemplatePlayback subject = buildTemplatePlayback(template5, fake.user3); // user will be overridden by hub access user id

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, subject));
    assertEquals("Preview-type Template is required", e.getMessage());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplatePlayback subject = test.insert(buildTemplatePlayback(fake.template1, fake.user2));

    assertThrows(ManagerException.class, () -> testManager.update(access, subject.getId(), subject));
  }

  @Test
  public void create_archivesExistingForUser() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var priorTemplate = test.insert(buildTemplate(fake.account1, TemplateType.Preview, "Prior", UUID.randomUUID().toString()));

    var priorPlayback = test.insert(buildTemplatePlayback(priorTemplate, fake.user2));
    var subject = buildTemplatePlayback(fake.template1, fake.user2);

    testManager.create(access, subject);

    assertNull(testManager.readOne(access, priorPlayback.getId()));
    verify(previewNexusAdmin, times(1)).startPreviewNexus(any(), any());
    assertNull(testManager.readOne(HubAccess.internal(), priorPlayback.getId()));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    TemplatePlayback result = testManager.readOne(access, templatePlayback201.getId());

    assertNotNull(result);
    assertEquals(templatePlayback201.getId(), result.getId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }


  /**
   * Preview Nexus attaches to template playback id specifically,
   * to ensure that once the template playback is unavailable, fabrication gracefully terminates
   * https://www.pivotaltracker.com/story/show/185115143
   */
  @Test
  public void readOne_notIfOlderThanThreshold() throws Exception {
    HubAccess userAccess = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");
    HubAccess adminAccess = HubAccess.internal();
    var olderPlayback = buildTemplatePlayback(fake.template1, fake.user3);
    olderPlayback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    var input = test.insert(olderPlayback);

    var adminResult = testManager.readOne(adminAccess, input.getId());
    var userResult = testManager.readOne(userAccess, input.getId());

    assertNull(adminResult);
    assertNull(userResult);
  }

  @Test
  public void readOneForUser() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));

    var result = testManager.readOneForUser(access, fake.user2.getId());

    assertTrue(result.isPresent());
    assertEquals(templatePlayback201.getId(), result.get().getId());
    assertEquals(fake.user2.getId(), result.get().getUserId());
  }

  @Test
  public void readOneForUser_justCreated() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    test.insert(buildTemplatePlayback(fake.template1, fake.user3));

    var result = testManager.readOneForUser(access, fake.user3.getId());

    assertTrue(result.isPresent());
    assertEquals(fake.user3.getId(), result.get().getUserId());
  }

  @Test
  public void readOneForUser_notIfOlderThanThreshold() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var olderPlayback = buildTemplatePlayback(fake.template1, fake.user3);
    olderPlayback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(olderPlayback);

    var result = testManager.readOneForUser(access, fake.user3.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() throws ManagerException {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")
    ), "User");

    assertNull(testManager.readOne(access, templatePlayback201.getId()));
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<TemplatePlayback> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_seesAdditional() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(fake.template1, fake.user3));

    Collection<TemplatePlayback> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_seesNoneOlderThanThreshold() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    var olderPlayback = buildTemplatePlayback(fake.template1, fake.user3);
    olderPlayback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(olderPlayback);

    Collection<TemplatePlayback> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfTemplate() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    Collection<TemplatePlayback> result = testManager.readMany(access, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    TemplatePlayback templatePlayback251 = test.insert(buildTemplatePlayback(fake.template1, fake.user2));

    testManager.destroy(access, templatePlayback251.getId());

    assertNull(testManager.readOne(HubAccess.internal(), templatePlayback251.getId()));
    verify(previewNexusAdmin, times(1)).stopPreviewNexus(any());
  }

}
