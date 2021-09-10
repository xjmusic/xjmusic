// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserRole;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequencePatternIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequencePatternDAO subjectDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private Injector injector;
  private ProgramVoice programVoice3;
  private ProgramVoice programVoice1;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
     injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
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

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("palm tree"));
    fake.program1 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    fake.program1_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .total(4)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0));
    programVoice1 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .type(InstrumentType.DRUM)
      .name("Drums"));
    var programVoiceTrack1 = test.insert(new ProgramVoiceTrack()
      .programId(programVoice1.getProgramId())
      .id(UUID.randomUUID())
      .programVoiceId(programVoice1.getId())
      .name("KICK"));
    fake.program2_sequence1_pattern1 = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .programVoiceId(programVoice1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .name("Beat"));
    fake.program2_sequence1_pattern1_event0 = test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(fake.program2_sequence1_pattern1.getProgramId())
      .programSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .programVoiceTrackId(programVoiceTrack1.getId())
      .position(0.0)
      .duration(1.0)
      .note("X")
      .velocity(1.0));
    fake.program2_sequence1_pattern1_event1 = test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(fake.program2_sequence1_pattern1.getProgramId())
      .programSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .programVoiceTrackId(programVoiceTrack1.getId())
      .position(1.0)
      .duration(1.0)
      .note("X")
      .velocity(1.0));
    fake.program2 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.RHYTHM)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.DRUM)
      .name("Drums"));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("boat"));
    fake.program3 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MACRO)
      .state(ProgramState.PUBLISHED)
      .name("helm")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    fake.program3_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .total(16)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0));
    programVoice3 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .type(InstrumentType.DRUM)
      .name("Drums"));
    test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(programVoice3.getProgramId())
      .programVoiceId(programVoice3.getId())
      .name("KICK"));
    test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program3_sequence1.getProgramId())
      .programSequenceId(fake.program3_sequence1.getId())
      .programVoiceId(programVoice3.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .name("Beat"));
    fake.program4 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.DETAIL)
      .state(ProgramState.PUBLISHED)
      .name("sail")
      .key("C#")
      .tempo(120.0)
      .density(0.6));

    // Instantiate the test subject
    subjectDAO = injector.getInstance(ProgramSequencePatternDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .programId(fake.program3.getId())
      .programVoiceId(programVoice3.getId())
      .programSequenceId(fake.program3_sequence1.getId())
      .name("Beat")
      ;

    var result = subjectDAO.create(
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
    var input = new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .programId(fake.program3.getId())
      .programVoiceId(programVoice3.getId())
      .programSequenceId(fake.program3_sequence1.getId())
      .name("Beat")
      ;

    DAOCloner<ProgramSequencePattern> result = subjectDAO.clone(hubAccess, fake.program2_sequence1_pattern1.getId(), input);

    assertNotNull(result);
    assertEquals(ProgramSequencePatternType.LOOP, result.getClone().getType());
    assertEquals(2, result.getChildClones().size());
    assertEquals(2, injector.getInstance(ProgramSequencePatternEventDAO.class)
      .readMany(HubAccess.internal(), ImmutableSet.of(result.getClone().getId()))
      .size());
  }

  /**
   FIX [#176352798] Clone API for Artist editing a Program can clone a pattern including its events
   due to constraints of serializing and deserializing the empty JSON payload for cloning an object
   without setting values (we will do this better in the future)--
   when cloning a pattern, `type` and `total` will always be set from the source pattern, and cannot be overridden.
   */
  @Test
  public void cloneExisting_allModifications() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var input = new ProgramSequencePattern()
      .programId(fake.program3.getId())
      .programVoiceId(programVoice3.getId())
      .programSequenceId(fake.program3_sequence1.getId())
      .type(ProgramSequencePatternType.INTRO) // cannot be modified while cloning
      .total(16) // cannot be modified while cloning
      .name("Jamming")
      ;

    DAOCloner<ProgramSequencePattern> result = subjectDAO.clone(hubAccess, fake.program2_sequence1_pattern1.getId(), input);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getClone().getProgramId());
    assertEquals(programVoice3.getId(), result.getClone().getProgramVoiceId());
    assertEquals(fake.program3_sequence1.getId(), result.getClone().getProgramSequenceId());
    assertEquals("Jamming", result.getClone().getName());
    assertEquals(ProgramSequencePatternType.LOOP, result.getClone().getType()); // cannot be modified while cloning
    assertEquals(Integer.valueOf(4), result.getClone().getTotal()); // cannot be modified while cloning
  }

  /**
   [#176352798] Clone API for Artist editing a Program can clone a pattern including its events
   */
  @Test
  public void cloneExisting_noModifications() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var input = new ProgramSequencePattern()
      ;

    DAOCloner<ProgramSequencePattern> result = subjectDAO.clone(hubAccess, fake.program2_sequence1_pattern1.getId(), input);

    assertNotNull(result);
    assertEquals(fake.program1_sequence1.getProgramId(), result.getClone().getProgramId());
    assertEquals(fake.program1_sequence1.getId(), result.getClone().getProgramSequenceId());
    assertEquals(programVoice1.getId(), result.getClone().getProgramVoiceId());
    assertEquals(ProgramSequencePatternType.LOOP, result.getClone().getType());
    assertEquals(Integer.valueOf(4), result.getClone().getTotal());
    assertEquals("Beat", result.getClone().getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequencePattern without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .programId(fake.program3.getId())
      .programVoiceId(programVoice3.getId())
      .programSequenceId(fake.program3_sequence1.getId())
      .name("Beat")
      ;

    var result = subjectDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_sequence1.getId(), result.getProgramSequenceId());
    assertEquals("Beat", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = subjectDAO.readOne(hubAccess, fake.program2_sequence1_pattern1.getId());

    assertNotNull(result);
    assertEquals(fake.program2_sequence1_pattern1.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals("Beat", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    subjectDAO.readOne(hubAccess, fake.program2_sequence1_pattern1.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequencePattern> result = subjectDAO.readMany(hubAccess, ImmutableList.of(fake.program1_sequence1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequencePattern> resultIt = result.iterator();
    assertEquals("Beat", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");

    Collection<ProgramSequencePattern> result = subjectDAO.readMany(hubAccess, ImmutableList.of(fake.program3_sequence1.getId()));

    assertEquals(0L, result.size());
  }

  /**
   [#171173394] Delete pattern with events in it
   */
  @Test
  public void destroy_okWithChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    subjectDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());
  }


  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event1.getId());

    subjectDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN)
      .where(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN.ID.eq(fake.program2_sequence1_pattern1.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = new Account()
      .id(UUID.randomUUID())
      ;
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1_event1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Sequence Pattern in Program in Account you have hubAccess to does not exist");

    subjectDAO.destroy(hubAccess, fake.program2_sequence1_pattern1.getId());
  }

}

