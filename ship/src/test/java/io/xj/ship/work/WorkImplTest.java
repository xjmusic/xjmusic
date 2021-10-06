// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.persistence.NexusEntityStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class WorkImplTest {
  private Work subject;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.from(ImmutableMap.of(
      "BOOTSTRAP_SHIP_KEY", "coolair"
    ));
    var injector = Guice.createInjector(Modules.override(new WorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Environment.class).toInstance(env);
        }
      }));
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // Instantiate the test subject
    subject = injector.getInstance(Work.class);
  }

  @Test
  public void isHealthy() {
    assertTrue(subject.isHealthy());
  }

  @Test
  public void isHealthy_neverWithoutShipKey() {
    subject = Guice.createInjector(Modules.override(new WorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Environment.class).toInstance(Environment.getDefault());
        }
      })).getInstance(Work.class);

    assertFalse(subject.isHealthy());
  }

}
