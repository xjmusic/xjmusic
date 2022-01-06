// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.nexus.persistence.ChainManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ChunkTest {
  private static final String SHIP_KEY = "test63";
  private Chunk subject;

  @Mock
  private ChainManager chainManager;

  @Before
  public void setUp() {
    Environment env = Environment.from(ImmutableMap.of(
      "SHIP_CHUNK_TARGET_DURATION", "10",
      "SHIP_KEY", "coolair"
    ));
    var injector = Guice.createInjector(Modules.override(new BroadcastModule()).with(new AbstractModule() {

      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(ChainManager.class).toInstance(chainManager);
      }
    }));

    var broadcast = injector.getInstance(BroadcastFactory.class);
    subject = broadcast.chunk(SHIP_KEY, 151304042L, "mp3", null);
  }

  @Test
  public void getFromSecondsUTC() {
    assertEquals(1513040420L, (long) subject.getFromSecondsUTC());
  }

  @Test
  public void getShipKey() {
    assertEquals(SHIP_KEY, subject.getShipKey());
  }

  @Test
  public void getKey() {
    assertEquals("test63-128k-151304042", subject.getKey(128000));
  }

  // Used in ffmpeg parameter for generating an HLS stream
  @Test
  public void getKeyTemplate() {
    assertEquals("test63-128k-%d", subject.getKeyTemplate(128000));
  }

}
