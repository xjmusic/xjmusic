// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;


import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.lock.LockProvider;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.dub.DubAudioCacheImpl;
import io.xj.nexus.dub.DubAudioCacheItemFactory;
import io.xj.nexus.dub.DubAudioCacheItemFactoryImpl;
import io.xj.nexus.dub.DubFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ChainManagerImpl;
import io.xj.nexus.persistence.FilePathProvider;
import io.xj.nexus.persistence.FilePathProviderImpl;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class NexusWorkImplTest {
  @Mock
  public NotificationProvider notificationProvider;
  @Mock
  private MixerFactory mixerFactory;
  @Mock
  public TelemetryProvider telemetryProvider;
  @Mock
  private ChainManager chainManager;
  @Mock
  private FileStoreProvider fileStoreProvider;
  @Mock
  private HubClient hubClient;
  @Mock
  private PreviewNexusAdmin previewNexusAdmin;
  private NexusWork subject;
  @Mock
  private LockProvider lockProvider;

  @Before
  public void setUp() throws Exception {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    NexusEntityStore store = new NexusEntityStoreImpl(entityFactory);
    store.deleteAll();

    // hub entities as basis
    Account account1 = buildAccount("fish");
    buildLibrary(account1, "test");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    Template template2 = buildTemplate(account1, "Test Template 2", "test2");

    // Payload comprising Nexus entities
    store.put(buildChain(account1, "school", ChainType.PRODUCTION, ChainState.READY, template1,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    store.put(buildChain(account1, "bucket", ChainType.PRODUCTION, ChainState.FABRICATE, template2,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate dependencies
    var segmentManager = new SegmentManagerImpl(entityFactory, store);
    chainManager = new ChainManagerImpl(
      entityFactory,
      store,
      segmentManager,
      notificationProvider, 1, 1
    );
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider("");
    CraftFactory craftFactory = new CraftFactoryImpl(apiUrlProvider);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(1, 1);
    DubAudioCacheItemFactory cacheItemFactory = new DubAudioCacheItemFactoryImpl(httpClientProvider, "xj-prod-audio", "https://audio.xj.io/");
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(cacheItemFactory, "");
    FilePathProvider filePathProvider = new FilePathProviderImpl("");
    var dubFactory = new DubFactoryImpl(dubAudioCache, filePathProvider, fileStoreProvider, mixerFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    var fabricatorFactory = new FabricatorFactoryImpl(
      chainManager,
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider,
      filePathProvider);

    // Instantiate the test subject
    subject = new NexusWorkImpl(
      chainManager,
      craftFactory,
      dubFactory,
      entityFactory,
      fabricatorFactory,
      httpClientProvider,
      hubClient,
      jsonapiPayloadFactory,
      jsonProvider,
      lockProvider,
      store,
      notificationProvider,
      previewNexusAdmin,
      segmentManager,
      telemetryProvider,
      "production",
      "",
      false,
      1,
      1,
      1,
      1,
      1,
      1,
      false,
      false,
      1,
      false,
      1,
      "https://ship.xj.io/",
      "xj-prod-ship",
      1,
      "");
  }

  @Test
  public void testIsHealthy() {
    assertTrue(subject.isHealthy());
  }
}
