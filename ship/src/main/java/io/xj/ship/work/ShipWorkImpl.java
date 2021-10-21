// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.Text;
import io.xj.ship.broadcast.BroadcastFactory;
import io.xj.ship.broadcast.ChunkManager;
import io.xj.ship.source.SourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 Ship Work Service Implementation
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 SEE: https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 SEE: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
 <p>
 Work is performed by runnable executables in a cached thread pool, with callback functions for success and failure.
 */
@Singleton
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private final AtomicReference<State> state;
  private final BroadcastFactory broadcast;
  private final ChunkManager chunkManager;
  private final Janitor janitor;
  private final NotificationProvider notification;
  private final SourceFactory sources;
  private final boolean janitorEnabled;
  private final int cycleMillis;
  private final int janitorCycleSeconds;
  private final int publishCycleSeconds;
  private final int shipReloadSeconds;
  private final long healthCycleStalenessThresholdMillis;
  @Nullable
  private final String shipKey;
  @Nullable
  private final String shipTitle;
  @Nullable
  private final String shipSource;
  private boolean alive = true;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextPublishMillis = 0;
  private long nextShipReloadMillis = 0;

  @Inject
  public ShipWorkImpl(
    ChunkManager chunkManager,
    Environment env,
    Janitor janitor,
    NotificationProvider notification,
    SourceFactory sources,
    BroadcastFactory broadcast
  ) {
    this.janitor = janitor;
    this.sources = sources;
    this.chunkManager = chunkManager;
    this.notification = notification;

    shipKey = env.getBootstrapShipKey();
    shipTitle = env.getBootstrapShipTitle();
    shipSource = env.getBootstrapShipSource();
    this.broadcast = broadcast;
    state = new AtomicReference<>(State.Active);
    shipReloadSeconds = env.getShipReloadSeconds();

    cycleMillis = env.getWorkCycleMillis();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.getWorkJanitorEnabled();
    publishCycleSeconds = env.getWorkPublishCycleSeconds();

    if (Strings.isNullOrEmpty(shipKey)) {
      LOG.error("Cannot start with null or empty bootstrap ship key!");
      alive = false;
    }

    LOG.debug("Instantiated OK");
  }

  @Override
  public void work() {
    while (alive) this.run();
  }

  @Override
  public void stop() {
    alive = false;
  }

  @Override
  public boolean isHealthy() {
    return alive && !isCycleStale() && !State.Fail.equals(state.get());
  }

  /**
   @return true if the cycle is stale
   */
  private boolean isCycleStale() {
    if (0 == nextCycleMillis) return false;
    return nextCycleMillis < System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   Run one work cycle
   */
  private void run() {
    var nowMillis = System.currentTimeMillis();
    if (nowMillis < nextCycleMillis) return;
    nextCycleMillis = nowMillis + cycleMillis;

    if (state.get() == State.Active)
      try {
        if (nowMillis > nextShipReloadMillis) {
          nextShipReloadMillis = nowMillis + shipReloadSeconds * MILLIS_PER_SECOND;
          ForkJoinPool.commonPool().execute(sources.spawnChainBoss(shipKey,
            () -> state.set(State.Fail)
          ));
        }

        for (var chunk : chunkManager.getAll(shipKey, nowMillis))
          broadcast.printer(chunk).print();

        if (nowMillis > nextPublishMillis) {
          nextPublishMillis = nowMillis + (publishCycleSeconds * MILLIS_PER_SECOND);
          if (chunkManager.isAssembledFarEnoughAhead(shipKey, nowMillis))
            broadcast.publisher(shipKey, shipTitle, shipSource).publish(nowMillis);
        }

        if (janitorEnabled)
          if (nowMillis > nextJanitorMillis) {
            nextJanitorMillis = nowMillis + (janitorCycleSeconds * MILLIS_PER_SECOND);
            janitor.cleanup();
          }

      } catch (Exception e) {
        var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        LOG.error("Failed while running ship work because {}", detail, e);
        notification.publish(String.format("Failed while running ship work because %s\n\n%s", detail, Text.formatStackTrace(e)), "Failure");
      }
  }

  enum State {
    Active,
    Fail
  }
}
