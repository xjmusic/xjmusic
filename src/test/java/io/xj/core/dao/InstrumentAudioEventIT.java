// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Library;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.testing.InternalResources;
import io.xj.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentAudioEventIT {
  public WorkManager workManager;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private InstrumentAudioEventDAO testDAO;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
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

    // Library "sandwich" has instrument "jams" and instrument "buns"
    fake.library1 = test.insert(Library.create(fake.account1, "sandwich", InternalResources.now()));
    fake.instrument201 = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Percussive, InstrumentState.Published, "jams"));
    test.insert(InstrumentMeme.create(fake.instrument202, "smooth"));
    fake.audio1 = test.insert(InstrumentAudio.create(fake.instrument202, "Test audio", "fake.audio5.wav", 0, 2, 120, 300, 0.5));
    fake.audioEvent1 = test.insert(InstrumentAudioEvent.create(fake.audio1, 0, 0.5, "bing", "D", 1));

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentAudioEventDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    InstrumentAudioEvent updatedEntity = InstrumentAudioEvent.create(fake.audio1, 0, 0.5, "wham", "D", 1);

    testDAO.update(access, fake.audioEvent1.getId(), updatedEntity);

    InstrumentAudioEvent result = testDAO.readOne(Access.internal(), fake.audioEvent1.getId());
    assertNotNull(result);
    assertEquals("WHAM", result.getName());
  }

}
