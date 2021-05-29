// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Account;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Value;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.hub.HubApp;
import io.xj.hub.client.HubClientAccess;
import io.xj.nexus.NexusApp;
import io.xj.nexus.persistence.NexusEntityStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class JanitorWorkerImplTest {
  private JanitorWorker subject;
  private static final int NUMBER_OF_SEGMENTS = 1000;
  private static final int SEGMENT_LENGTH_SECONDS = 30;
  private static final int PAST_SECONDS = 10000;
  private NexusEntityStore store;
  private SegmentDAO segmentDAO;

  @Mock
  public FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9043))
      .withValue("datadog.statsd.hostname", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("work.bossDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.janitorDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.medicDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.chainDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.concurrency", ConfigValueFactory.fromAnyRef(1));

    var injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule()).with(
        new AbstractModule() {
          @Override
          public void configure() {
            bind(Config.class).toInstance(config);
            bind(FileStoreProvider.class).toInstance(fileStoreProvider);
          }
        })));
    var entityFactory = injector.getInstance(EntityFactory.class);
    store = injector.getInstance(NexusEntityStore.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    segmentDAO = injector.getInstance(SegmentDAO.class);
    var workerFactory = injector.getInstance(WorkerFactory.class);
    subject = workerFactory.janitor();
  }

  @Test
  public void deletesSegments() throws Exception {
    var account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("palm tree")
      .build();
    var now = Instant.now();
    var chain1 = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt(Value.formatIso8601UTC(now.minusSeconds(PAST_SECONDS + SEGMENT_LENGTH_SECONDS * NUMBER_OF_SEGMENTS)))
      .build());
    var segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setBeginAt(chain1.getStartAt())
      .setEndAt(Value.formatIso8601UTC(Instant.parse(chain1.getStartAt()).plusSeconds(SEGMENT_LENGTH_SECONDS)))
      .setState(Segment.State.Dubbed)
      .build());
    for (int i = 0; i < NUMBER_OF_SEGMENTS; i++)
      segment = store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain1.getId())
        .setBeginAt(segment.getEndAt())
        .setEndAt(Value.formatIso8601UTC(Instant.parse(segment.getEndAt()).plusSeconds(SEGMENT_LENGTH_SECONDS)))
        .setState(Segment.State.Dubbed)
        .build());

    // Start app, wait for work, stop app
    subject.run();

    // Check segments actually deleted
    var segments = segmentDAO.readMany(HubClientAccess.internal(), ImmutableList.of(chain1.getId()));
    assertEquals(0, segments.size());
  }

}
