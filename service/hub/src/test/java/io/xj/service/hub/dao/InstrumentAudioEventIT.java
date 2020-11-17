// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.Library;
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

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or delete instruments

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class InstrumentAudioEventIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private InstrumentAudioEventDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
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

    // Library "sandwich" has instrument "jams" and instrument "buns"
    fake.library1 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("sandwich")
      .build());
    fake.instrument201 = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Harmonic)
      .setState(Instrument.State.Published)
      .setName("buns")
      .build());
    fake.instrument202 = test.insert(Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Instrument.Type.Percussive)
      .setState(Instrument.State.Published)
      .setName("jams")
      .build());
    test.insert(InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.instrument202.getId())
      .setName("smooth")
      .build());
    fake.audio1 = test.insert(InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.instrument202.getId())
      .setName("Test audio")
      .setWaveformKey("fake.audio5.wav")
      .setStart(0)
      .setLength(2)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.5)
      .build());
    fake.audioEvent1 = test.insert(InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.audio1.getInstrumentId())
      .setInstrumentAudioId(fake.audio1.getId())
      .setPosition(0)
      .setDuration(0.5)
      .setName("bing")
      .setNote("D")
      .setVelocity(1)
      .build());
    fake.audio2 = test.insert(InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.instrument202.getId())
      .setName("Test audio2")
      .setWaveformKey("fake.audio5222.wav")
      .setStart(0)
      .setLength(2)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.5)
      .build());
    fake.audioEvent2 = test.insert(InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.audio2.getInstrumentId())
      .setInstrumentAudioId(fake.audio2.getId())
      .setPosition(0)
      .setDuration(0.5)
      .setName("bang")
      .setNote("E")
      .setVelocity(1)
      .build());

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentAudioEventDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    InstrumentAudioEvent updatedEntity = InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(fake.audio1.getInstrumentId())
      .setInstrumentAudioId(fake.audio1.getId())
      .setPosition(0)
      .setDuration(0.5)
      .setName("wham")
      .setNote("D")
      .setVelocity(1)
      .build();

    testDAO.update(hubAccess, fake.audioEvent1.getId(), updatedEntity);

    InstrumentAudioEvent result = testDAO.readOne(HubAccess.internal(), fake.audioEvent1.getId());
    assertNotNull(result);
    assertEquals("WHAM", result.getName());
  }

}
