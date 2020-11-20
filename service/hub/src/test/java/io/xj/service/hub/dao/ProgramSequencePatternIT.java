// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.User;
import io.xj.UserRole;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.service.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequencePatternIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequencePatternDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private Injector injector;
  private ProgramVoice programVoice3;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());
// John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("palm tree")
      .build());
    fake.program1 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("ANTS")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.program1_sequence1 = test.insert(ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setTotal(4)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0)
      .build());
    var programVoice1 = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setType(Instrument.Type.Percussive)
      .setName("Drums")
      .build());
    var programVoiceTrack1 = test.insert(ProgramVoiceTrack.newBuilder()
      .setProgramId(programVoice1.getProgramId())
      .setId(UUID.randomUUID().toString())
      .setProgramVoiceId(programVoice1.getId())
      .setName("KICK")
      .build());
    fake.program2_sequence1_pattern1 = test.insert(ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setProgramVoiceId(programVoice1.getId())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(4)
      .setName("Beat")
      .build());
    fake.program2_sequence1_pattern1_event0 = test.insert(ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2_sequence1_pattern1.getProgramId())
      .setProgramSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .setProgramVoiceTrackId(programVoiceTrack1.getId())
      .setPosition(0)
      .setDuration(1)
      .setNote("X")
      .setVelocity(1)
      .build());
    fake.program2_sequence1_pattern1_event1 = test.insert(ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2_sequence1_pattern1.getProgramId())
      .setProgramSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .setProgramVoiceTrackId(programVoiceTrack1.getId())
      .setPosition(1)
      .setDuration(1)
      .setNote("X")
      .setVelocity(1)
      .build());
    fake.program2 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Rhythm)
      .setState(Program.State.Published)
      .setName("ANTS")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2.getId())
      .setType(Instrument.Type.Percussive)
      .setName("Drums")
      .build());

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("boat")
      .build());
    fake.program3 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Macro)
      .setState(Program.State.Published)
      .setName("helm")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.program3_sequence1 = test.insert(ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0)
      .build());
    programVoice3 = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setType(Instrument.Type.Percussive)
      .setName("Drums")
      .build());
    test.insert(ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programVoice3.getProgramId())
      .setProgramVoiceId(programVoice3.getId())
      .setName("KICK")
      .build());
    test.insert(ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3_sequence1.getProgramId())
      .setProgramSequenceId(fake.program3_sequence1.getId())
      .setProgramVoiceId(programVoice3.getId())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(4)
      .setName("Beat")
      .build());
    fake.program4 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Detail)
      .setState(Program.State.Published)
      .setName("sail")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequencePatternDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(4)
      .setProgramId(fake.program3.getId())
      .setProgramVoiceId(programVoice3.getId())
      .setProgramSequenceId(fake.program3_sequence1.getId())
      .setName("Beat")
      .build();

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_sequence1.getId(), result.getProgramSequenceId());
    assertEquals("Beat", result.getName());
  }

  /**
   [#171617769] Artist editing Program clones a pattern
   [#173912361] Hub API create pattern cloning existing pattern
   */
  @Test
  public void cloneExisting() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(4)
      .setProgramId(fake.program3.getId())
      .setProgramVoiceId(programVoice3.getId())
      .setProgramSequenceId(fake.program3_sequence1.getId())
      .setName("Beat")
      .build();

    DAOCloner<ProgramSequencePattern> result = testDAO.clone(hubAccess, fake.program2_sequence1_pattern1.getId(), subject);

    assertNotNull(result);
    assertEquals(ProgramSequencePattern.Type.Loop, result.getClone().getType());
    assertEquals(2, result.getChildClones().size());
    assertEquals(2, injector.getInstance(ProgramSequencePatternEventDAO.class)
      .readMany(HubAccess.internal(), ImmutableSet.of(result.getClone().getId()))
      .size());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequencePattern without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(4)
      .setProgramId(fake.program3.getId())
      .setProgramVoiceId(programVoice3.getId())
      .setProgramSequenceId(fake.program3_sequence1.getId())
      .setName("Beat")
      .build();

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_sequence1.getId(), result.getProgramSequenceId());
    assertEquals("Beat", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.program2_sequence1_pattern1.getId());

    assertNotNull(result);
    assertEquals(fake.program2_sequence1_pattern1.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals("Beat", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program2_sequence1_pattern1.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequencePattern> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1_sequence1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequencePattern> resultIt = result.iterator();
    assertEquals("Beat", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User, Artist");

    Collection<ProgramSequencePattern> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3_sequence1.getId()));

    assertEquals(0L, result.size());
  }

  /**
   [#171173394] Delete pattern with events in it
   */
  @Test
  public void destroy_okWithChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());
  }


  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event1.getId());

    testDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.ID.eq(UUID.fromString(fake.program2_sequence1_pattern1.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Sequence Pattern in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());
  }

}

