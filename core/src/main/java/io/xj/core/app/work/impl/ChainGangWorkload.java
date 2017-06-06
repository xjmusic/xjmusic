// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.work.impl;

import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.work.Workload;
import io.xj.core.chain_gang.Leader;
import io.xj.core.chain_gang.Follower;

import org.json.JSONArray;
import org.json.JSONObject;
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
public class ChainGangWorkload implements Workload {
  private ScheduledFuture scheduledFuture;
  private static ScheduledExecutorService leaderExecutor;
  private static ExecutorService workerExecutor;
  private final static Logger log = LoggerFactory.getLogger(ChainGangWorkload.class);
  private String name;
  private Leader leader;
  private Follower follower;

  /**
   Gang workload, with a leader and many workers
   */
  public ChainGangWorkload(String name, Leader leader, Follower follower) throws ConfigException {
    this.name = name;
    this.leader = leader;
    this.follower = follower;

    int workConcurrency = Config.workConcurrency();
    leaderExecutor = Executors.newScheduledThreadPool(workConcurrency);
    workerExecutor = Executors.newFixedThreadPool(workConcurrency);
  }

  @Override
  public void start() throws ConfigException {
    log.info("{} will start now", this);
    scheduledFuture = leaderExecutor.scheduleAtFixedRate(
      this::pollLeader,
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

  private void pollLeader() {
    log.debug("{} polling Leader for tasks", this);
    JSONArray tasks = leader.getTasks();
    if (tasks.length() > 0) {
      log.debug("{} will execute {} Worker tasks", this, tasks.length());
      for (int i = 0; i < tasks.length(); i++) {
        try {
          workerExecutor.execute(
            follower.getTaskRunnable((JSONObject) tasks.get(i)));
        } catch (Exception e) {
          log.error("{} failed execute worker task runnable", this, e);
        }
      }
    } else {
      log.debug("{} has nothing to do", this);
    }
  }

  @Override
  public String toString() {
    return "Workload[" + this.name + "]";
  }
}
