// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.work;

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
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.HubApp;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.persistence.NexusEntityStoreException;
import io.xj.service.nexus.testing.NexusTestConfiguration;
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
  private static final int NUMBER_OF_SEGMENTS = 1000;
  private static final int SEGMENT_LENGTH_SECONDS = 30;
  private static final int PAST_SECONDS = 10000;
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  long startTime = System.currentTimeMillis();
  private Segment segment0;
  private Chain chainA;
  private NexusIntegrationTestingFixtures fake;
  private NexusEntityStore store;

  @Mock
  public FileStoreProvider mockFileStoreProvider;

  @Mock
  public TelemetryProvider mockTelemetryProvider;

  @Mock
  public FabricatorFactory mockFabricatorFactory;

  @Mock
  public SegmentDAO mockSegmentDAO;

  @Before
  public void setUp() throws AppException, NexusEntityStoreException, DAOPrivilegeException, DAOFatalException, DAOExistenceException, EntityException {
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
                        bind(TelemetryProvider.class).toInstance(mockTelemetryProvider);
                        bind(FileStoreProvider.class).toInstance(mockFileStoreProvider);
                        bind(SegmentDAO.class).toInstance(mockSegmentDAO);
                        bind(FabricatorFactory.class).toInstance(mockFabricatorFactory);
                      }
                    })));
    var entityFactory = injector.getInstance(EntityFactory.class);
    store = injector.getInstance(NexusEntityStore.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);
    var workerFactory = injector.getInstance(WorkerFactory.class);

    // Chain "Test Print #1" is fabricating segments
    fake = new NexusIntegrationTestingFixtures();
    fake.setupFixtureB1();
    chainA = store.put(Chain.newBuilder()
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
    segment0 = store.put(Segment.newBuilder()
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
  public void createSegmentErrorMessage() throws FabricationException, DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    when(mockFabricatorFactory.fabricate(any(), any()))
            .thenThrow(new FabricationException("Error!"));

    subject.run();

    verify(mockSegmentDAO, times(1)).create(any(), any(SegmentMessage.class));
  }

}
