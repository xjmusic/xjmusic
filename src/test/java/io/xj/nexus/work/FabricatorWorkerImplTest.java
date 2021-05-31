// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Segment;
import io.xj.SegmentMessage;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.persistence.NexusEntityStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorWorkerImplTest {
  private FabricatorWorker subject;

  @Mock
  public FileStoreProvider mockFileStoreProvider;

  @Mock
  public FabricatorFactory mockFabricatorFactory;

  @Mock
  public SegmentDAO mockSegmentDAO;

  @Before
  public void setUp() throws AppException, NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException, EntityException {
    Config mockConfig = NexusTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9043))
      .withValue("work.bossDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.fabricatorDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.medicDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.chainDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.concurrency", ConfigValueFactory.fromAnyRef(1));

    var injector = AppConfiguration.inject(mockConfig,
      ImmutableSet.of(Modules.override(new NexusWorkModule()).with(
        new AbstractModule() {
          @Override
          public void configure() {
            bind(Config.class).toInstance(mockConfig);
            bind(FileStoreProvider.class).toInstance(mockFileStoreProvider);
            bind(SegmentDAO.class).toInstance(mockSegmentDAO);
            bind(FabricatorFactory.class).toInstance(mockFabricatorFactory);
          }
        })));
    var entityFactory = injector.getInstance(EntityFactory.class);
    NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);
    var workerFactory = injector.getInstance(WorkerFactory.class);

    // Chain "Test Print #1" is fabricating segments
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    fake.setupFixtureB1();
    Chain chainA = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chainA.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    Segment segment0 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chainA.getId())
      .setOffset(0)
      .setState(Segment.State.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120)
      .setStorageKey("chains-1-segments-9f7s89d8a7892")
      .setOutputEncoder("wav")
      .build());
    when(mockSegmentDAO.readOne(any(), eq(segment0.getId())))
      .thenReturn(segment0);

    subject = workerFactory.segment(segment0.getId());
  }

  /**
   [#175879632] Segment error message should not have NPE
   */
  @Test
  public void createSegmentErrorMessage() throws NexusException, DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    when(mockFabricatorFactory.fabricate(any(), any()))
      .thenThrow(new NexusException("Error!"));

    subject.run();

    verify(mockSegmentDAO, times(1)).create(any(), any(SegmentMessage.class));
  }

}
