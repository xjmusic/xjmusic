// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.ProgramMemeDAO;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.hub.ingest.HubIngestFactory;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestHashIT {
  private HubIngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private IntegrationTestingFixtures fake;
  private Injector injector;
  private HubIntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubDigestModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();


    ingestFactory = injector.getInstance(HubIngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void readHash_ofLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");
    Set<UUID> libraryIds = ImmutableSet.of(fake.library10000001.getId());

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(hubAccess, libraryIds, ImmutableSet.of(), ImmutableSet.of()));

    assertNotNull(result);
  }

  @Test
  public void readHash_ofLibrary_afterUpdateEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");
    Set<UUID> libraryIds = ImmutableSet.of(fake.library10000001.getId());
    injector.getInstance(ProgramDAO.class).update(HubAccess.internal(), fake.program703.getId(),
      new Program()
        .setUserId(fake.program703.getUserId())
        .setLibraryId(fake.program703.getLibraryId())
        .setKey("G")
        .setDensity(1.0)
        .setStateEnum(ProgramState.Published)
        .setTypeEnum(ProgramType.Rhythm)
        .setName("new name")
        .setTempo(150.0));

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(hubAccess, libraryIds, ImmutableSet.of(), ImmutableSet.of()));

    assertNotNull(result);
    Program updatedProgram = injector.getInstance(ProgramDAO.class).readOne(HubAccess.internal(), fake.program703.getId());
    assertNotNull(updatedProgram);
  }

  @Test
  public void readHash_ofLibrary_afterDestroyEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");
    Set<UUID> libraryIds = ImmutableSet.of(fake.library10000001.getId());
    injector.getInstance(ProgramMemeDAO.class).destroy(HubAccess.internal(), fake.program701_meme0.getId());

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(hubAccess, libraryIds, ImmutableSet.of(), ImmutableSet.of()));
  }

}
