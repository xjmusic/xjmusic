// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 The Mk3 Nexus Distributed Work Manager (Implementation)
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
@Singleton
public class NexusWorkImpl implements NexusWork {
  private static final Logger log = LoggerFactory.getLogger(NexusWorkImpl.class);
  private final ScheduledExecutorService scheduler;
  private ScheduledFuture<?> boss;
  private ScheduledFuture<?> janitor;
  private ScheduledFuture<?> medic;
  private final WorkerFactory worker;

  // Name pool threads
  private final Map<String/*ChainID*/, ScheduledFuture<?>/*ChainWorker*/> work = Maps.newConcurrentMap();
  private final int bossDelayMillis;
  private final int janitorDelayMillis;
  private final int medicDelayMillis;
  private final int chainDelayMillis;

  @Inject
  NexusWorkImpl(
    WorkerFactory worker,
    Config config
  ) {
    this.worker = worker;

    bossDelayMillis = config.getInt("work.bossDelayMillis");
    janitorDelayMillis = config.getInt("work.janitorDelayMillis");
    medicDelayMillis = config.getInt("work.medicDelayMillis");
    chainDelayMillis = config.getInt("work.chainDelayMillis");
    int workConcurrency = config.getInt("work.concurrency");

    scheduler = Executors.newScheduledThreadPool(workConcurrency, new ThreadFactoryBuilder()
      .setNameFormat(NexusWork.class.getSimpleName() + "-%d")
      .setDaemon(true)
      .build());
  }

  @Override
  public void start() {
    boss = scheduler.scheduleWithFixedDelay(worker.boss(), bossDelayMillis, bossDelayMillis, TimeUnit.MILLISECONDS);
    janitor = scheduler.scheduleWithFixedDelay(worker.janitor(), janitorDelayMillis, janitorDelayMillis, TimeUnit.MILLISECONDS);
    medic = scheduler.scheduleWithFixedDelay(worker.medic(), medicDelayMillis, medicDelayMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public void finish() {
    cancelAllChainWork();
    if (Objects.nonNull(boss)) boss.cancel(false);
    if (Objects.nonNull(janitor)) janitor.cancel(false);
    if (Objects.nonNull(medic)) medic.cancel(false);
    scheduler.shutdown();
    try {
      if (scheduler.awaitTermination(1, TimeUnit.MINUTES))
        log.info("Executor service did terminate OK");
      else
        log.error("Executor service failed to terminate!");
    } catch (InterruptedException e) {
      log.error("Timout waiting to for termination of executor service!", e);
    }
  }

  @Override
  public void cancelAllChainWork() {
    for (String chainId : work.keySet())
      cancelChainWork(chainId);
  }

  @Override
  public boolean isWorkingOnChain(String id) {
    return work.containsKey(id);
  }

  @Override
  public void beginChainWork(String chainId) {
    if (work.containsKey(chainId)) return;
    work.put(chainId, scheduler.scheduleWithFixedDelay(worker.chain(chainId), chainDelayMillis, chainDelayMillis, TimeUnit.MILLISECONDS));
  }

  @Override
  public void cancelChainWork(String chainId) {
    if (!work.containsKey(chainId)) return;
    work.get(chainId).cancel(false);
    work.remove(chainId);
  }

  @Override
  public Collection<String> getChainWorkingIds() {
    return work.keySet();
  }

}
