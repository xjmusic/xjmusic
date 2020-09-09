// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.AccountUser;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequenceBinding;
import io.xj.service.hub.entity.ProgramSequencePattern;
import io.xj.service.hub.entity.ProgramSequencePatternType;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.hub.entity.ProgramVoice;
import io.xj.service.hub.entity.ProgramVoiceTrack;
import io.xj.service.hub.entity.User;
import io.xj.service.hub.entity.UserRole;
import io.xj.service.hub.entity.UserRoleType;
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

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

import static io.xj.service.hub.tables.ProgramVoice.PROGRAM_VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramVoiceIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramVoiceDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fake.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fake.user3, UserRoleType.User));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", Instant.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.program1_sequence1 = test.insert(ProgramSequence.create(fake.program1, 4, "Ants", 0.583, "D minor", 120.0));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.program2_voice1 = test.insert(ProgramVoice.create(fake.program2, InstrumentType.Percussive, "Drums"));
    fake.program2_sequence1_pattern1 = test.insert(ProgramSequencePattern.create(fake.program1_sequence1, fake.program2_voice1, ProgramSequencePatternType.Loop, 4, "BOOMS"));
    fake.program2_voice1_track0 = test.insert(ProgramVoiceTrack.create(fake.program2_voice1, "KICK"));
    fake.program2_voice1_track1 = test.insert(ProgramVoiceTrack.create(fake.program2_voice1, "SNARE"));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", Instant.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fake.program3_sequence1 = test.insert(ProgramSequence.create(fake.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fake.program3_sequence1, 0));
    fake.program4 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramVoiceDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    ProgramVoice subject = ProgramVoice.create()
      .setProgramId(fake.program3.getId())
      .setTypeEnum(InstrumentType.Harmonic)
      .setName("Jams");

    ProgramVoice result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programVoice without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    ProgramVoice inputData = ProgramVoice.create()
      .setProgramId(fake.program3.getId())
      .setTypeEnum(InstrumentType.Harmonic)
      .setName("Jams");

    ProgramVoice result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    ProgramVoice result = testDAO.readOne(hubAccess, fake.program2_voice1.getId());

    assertNotNull(result);
    assertEquals(fake.program2_voice1.getId(), result.getId());
    assertEquals(fake.program2.getId(), result.getProgramId());
    assertEquals("Drums", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program2_voice1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    Collection<ProgramVoice> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramVoice> resultIt = result.iterator();
    assertEquals("Drums", resultIt.next().getName());
  }

  @Test
  public void readMany_seesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramVoice> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    failure.expect(DAOException.class);
    failure.expectMessage("Found Pattern in Voice");

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(fake.program2_voice1.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(fake.program2_voice1.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Voice in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());
  }

}

