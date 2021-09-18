// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.hub.HubTopology;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.ship.ShipTestConfiguration;
import io.xj.ship.ShipTopology;
import io.xj.ship.persistence.ShipEntityStore;
import io.xj.ship.persistence.ShipEntityStoreModule;
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

@SuppressWarnings("HttpUrlsUsage")
@RunWith(MockitoJUnitRunner.class)
public class ShipWorkChainManagerImplTest {
  private ShipEntityStore test;
  private ShipWorkChainManager subject;
  private Account account1;
  private Chain chain1;
  private Chain chain2;
  private Template template1;
  private Template template2;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws Exception {
    Config config = ShipTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule(), new JsonModule(), new JsonapiModule(), new EntityModule(), new ShipEntityStoreModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
          bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        }
      }));
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    ShipTopology.buildShipApiTopology(entityFactory);

    // Manipulate the underlying entity store
    test = injector.getInstance(ShipEntityStore.class);
    test.deleteAll();

    // hub entities as basis
    account1 = buildAccount("fish");
    buildLibrary(account1, "test");
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    template2 = buildTemplate(account1, "Test Template 2", "test2");

    // Payload comprising Ship entities
    chain1 = test.put(buildChain(account1, "school", ChainType.PRODUCTION, ChainState.READY, template1,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    chain2 = test.put(buildChain(account1, "bucket", ChainType.PRODUCTION, ChainState.FABRICATE, template2,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate the test subject
    subject = injector.getInstance(ShipWorkChainManager.class);
  }

  @Test
  public void testPoll() {
    subject.poll();
  }

  @Test
  public void testIsHealthy() {
    assertTrue(subject.isHealthy());
  }
}
