// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.ship.broadcast.BroadcastFactory;
import io.xj.ship.broadcast.PlaylistPublisher;
import io.xj.ship.source.SourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShipWorkImplTest {
  @Mock
  BroadcastFactory broadcast;
  @Mock
  ChainManager chains;
  @Mock
  Janitor janitor;
  @Mock
  NotificationProvider notification;
  @Mock
  SourceFactory source;
  @Mock
  PlaylistPublisher publisher;

  private ShipWork subject;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.from(ImmutableMap.of("SHIP_KEY", "coolair"));
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(BroadcastFactory.class).toInstance(broadcast);
        bind(ChainManager.class).toInstance(chains);
        bind(Janitor.class).toInstance(janitor);
        bind(NotificationProvider.class).toInstance(notification);
        bind(SourceFactory.class).toInstance(source);
      }
    }));
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    when(broadcast.publisher(eq("coolair"))).thenReturn(publisher);

    // Instantiate the test subject
    subject = injector.getInstance(ShipWork.class);
  }

/*
FUTURE: bring this back, but it's now necessary to get the stream process running before the health check is OK, by actually running the work!

  @Test
  public void isHealthy() {
    assertTrue(subject.isHealthy());
  }
*/

  @Test
  public void isHealthy_neverWithoutShipKey() {
    subject = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(Environment.getDefault());
      }
    })).getInstance(ShipWork.class);

    assertFalse(subject.isHealthy());
  }

}
