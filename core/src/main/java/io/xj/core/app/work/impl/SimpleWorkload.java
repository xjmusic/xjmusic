// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.work.impl;

import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.work.Worker;
import io.xj.core.app.work.Workload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 A Gang Workload runs a Leader + Worker-group
 */
public class SimpleWorkload implements Workload {
  private ScheduledFuture scheduledFuture;
  private static ScheduledExecutorService leaderExecutor;
  private static ExecutorService workerExecutor;
  private final static Logger log = LoggerFactory.getLogger(SimpleWorkload.class);
  private String name;
  private Worker worker;

  /**
   Simple workload is just a worker
   */
  public SimpleWorkload(String name, Worker worker) throws ConfigException {
    this.name = name;
    this.worker = worker;

    int workConcurrency = Config.workConcurrency();
    leaderExecutor = Executors.newScheduledThreadPool(workConcurrency);
    workerExecutor = Executors.newFixedThreadPool(workConcurrency);
  }

  @Override
  public void start() throws ConfigException {
    log.info("{} will start now", this);
    scheduledFuture = leaderExecutor.scheduleAtFixedRate(
      this::doWork,
      Config.workBatchSleepSeconds(),
      Config.workBatchSleepSeconds(),
      TimeUnit.SECONDS);
    log.info("{} up", this);
  }

  @Override
  public void stop() {
    log.info("{} will shutdown now", this);
    if (Objects.nonNull(scheduledFuture))
      scheduledFuture.cancel(false);
    log.info("{} did shutdown OK", this);
  }

  private void doWork() {
    try {
      workerExecutor.execute(
        worker.getTaskRunnable());
    } catch (Exception e) {
      log.error("{} failed execute worker task runnable", this, e);
    }
  }

  @Override
  public String toString() {
    return "Workload[" + this.name + "]";
  }
}
