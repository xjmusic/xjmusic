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
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceChordVoicingIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceChordVoicingDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceChord sequenceChord1a_0;
  private ProgramSequenceChordVoicing sequenceChord1a_0_voicing0;

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
    fake.account1 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("bananas")
      );
// John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("john")
      .email("john@email.com")
      .avatarUrl("http://pictures.com/john.gif")
      );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.ADMIN)
    );

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("jenny")
      .email("jenny@email.com")
      .avatarUrl("http://pictures.com/jenny.gif")
      );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.USER)
      );
    test.insert(new AccountUser()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .userId(fake.user3.getId())
      );

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("palm tree")
      );
    fake.program1 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
      );
    fake.program1_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .total(4)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0)
      );
    sequenceChord1a_0 = test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .position(0.0)
      .name("C minor")
      );
    sequenceChord1a_0_voicing0 = test.insert(new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .type(InstrumentType.BASS)
      .programId(sequenceChord1a_0.getProgramId())
      .programSequenceChordId(sequenceChord1a_0.getId())
      .notes("C5, Eb5, G5")
      );
    test.insert(new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .type(InstrumentType.BASS)
      .programId(sequenceChord1a_0.getProgramId())
      .programSequenceChordId(sequenceChord1a_0.getId())
      .notes("G,B,Db,F")
      );
    fake.program2 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.RHYTHM)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
      );
    fake.program2_voice1 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Drums")
      );

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("boat")
      );
    fake.program3 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MACRO)
      .state(ProgramState.PUBLISHED)
      .name("helm")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
      );
    fake.program3_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .total(16)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0)
      );
    fake.program3_chord1 = test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(fake.program3_sequence1.getProgramId())
      .programSequenceId(fake.program3_sequence1.getId())
      .position(0.0)
      .name("G7 flat 6")
      );
    fake.program4 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.DETAIL)
      .state(ProgramState.PUBLISHED)
      .name("sail")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
      );

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceChordVoicingDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .programSequenceChordId(fake.program3_chord1.getId())
      .type(InstrumentType.PAD)
      .notes("C5, Eb5, G5")
      ;

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_chord1.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.PAD, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  /**
   [#176162975] Endpoint to batch update ProgramSequenceChordVoicing
   */
  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");

    testDAO.update(hubAccess, sequenceChord1a_0_voicing0.getId(),
      sequenceChord1a_0_voicing0
        .type(InstrumentType.STICKY)
        .notes("G1,G2,G3")
        );

    var result = testDAO.readOne(hubAccess, sequenceChord1a_0_voicing0.getId());
    assertNotNull(result);
    assertEquals(sequenceChord1a_0_voicing0.getId(), result.getId());
    assertEquals(InstrumentType.STICKY, result.getType());
    assertEquals("G1,G2,G3", result.getNotes());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequenceChordVoicing without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .programSequenceChordId(fake.program3_chord1.getId())
      .type(InstrumentType.BASS)
      .notes("C5, Eb5, G5")
      ;

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_chord1.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.BASS, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, sequenceChord1a_0_voicing0.getId());

    assertNotNull(result);
    assertEquals(sequenceChord1a_0_voicing0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(sequenceChord1a_0.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.BASS, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, sequenceChord1a_0_voicing0.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceChordVoicing> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(2L, result.size());
    Iterator<ProgramSequenceChordVoicing> resultIt = result.iterator();
    assertEquals("C5, Eb5, G5", resultIt.next().getNotes());
    assertEquals("G,B,Db,F", resultIt.next().getNotes());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");

    Collection<ProgramSequenceChordVoicing> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = new Account()
      .id(UUID.randomUUID())
      ;
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Voicing belongs to Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, sequenceChord1a_0_voicing0.getId());
  }

}

