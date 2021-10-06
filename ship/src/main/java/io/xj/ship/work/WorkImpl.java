// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.Text;
import io.xj.ship.persistence.ChunkManager;
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
public class WorkImpl implements Work {
  private static final Logger LOG = LoggerFactory.getLogger(WorkImpl.class);
  private boolean alive = true;
  private final AtomicReference<State> state;
  private final boolean janitorEnabled;
  private final ChunkManager chunkManager;
  private final int cycleMillis;
  private final int janitorCycleSeconds;
  private final int shipReloadSeconds;
  private final long healthCycleStalenessThresholdMillis;
  private final WorkFactory work;
  private final NotificationProvider notification;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextShipReloadMillis = 0;

  @Nullable
  private final String shipKey;

  @Inject
  public WorkImpl(
    ChunkManager chunkManager,
    Environment env,
    NotificationProvider notification,
    WorkFactory work
  ) {
    this.work = work;
    this.chunkManager = chunkManager;
    this.notification = notification;

    shipKey = env.getBootstrapShipKey();
    state = new AtomicReference<>(State.Active);
    shipReloadSeconds = env.getShipReloadSeconds();

    cycleMillis = env.getWorkCycleMillis();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.getWorkJanitorEnabled();

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
   * @return true if the cycle is stale
   */
  private boolean isCycleStale() {
    if (0 == nextCycleMillis) return false;
    return nextCycleMillis < System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   Run one work cycle
   */
  private void run() {
    if (System.currentTimeMillis() < nextCycleMillis) return;
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    if (state.get() == State.Active)
      try {
        var nowMillis = System.currentTimeMillis();
        if (nowMillis > nextShipReloadMillis) {
          nextShipReloadMillis = System.currentTimeMillis() + shipReloadSeconds * MILLIS_PER_SECOND;
          ForkJoinPool.commonPool().execute(work.loadChainManifest(shipKey,
            () -> state.set(State.Fail)
          ));
        }

        if (chunkManager.isAssembledFarEnoughAhead()) {
          for (var chunk : chunkManager.computeAll(shipKey)) {
            ForkJoinPool.commonPool().execute(work.print(chunk));
            // NEXT for each planned chunk, try to print it with a media chunk printer
            // NEXT update chunk before putting it back
          }
        }

        if (janitorEnabled) {
          if (System.currentTimeMillis() < nextJanitorMillis) return;
          nextJanitorMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
          ForkJoinPool.commonPool().execute(work.doCleanup());
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
