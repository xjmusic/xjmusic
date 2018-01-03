// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work;

import io.xj.core.model.work.Work;

import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;

import java.math.BigInteger;
import java.util.Collection;

/**
 [#286] True Chain-Link work management
 (deprecating each link worker reading whatever links are in a certain state)
 in order to prevent the train wrecks we are already seeing at small scale.
 <p>
 # ChainFabricateJob, ChainStopJob, LinkCraftJob, LinkDubJob
 <p>
 Work module deprecates craftworker, dubworker, and eraseworker. These modules will likely be moved into sub-modules of the new worker module
 Hub enqueues a recurring ChainFabricateJob on creation of a Chain
 Hub enqueues a recurring ChainFabricateJob on retry of a Chain in a Failed state
 Hub stops any recurring ChainFabricateJob and creates a ChainStopJob on completion or deletion of a Chain
 Work establishes a pool of threads to run Clients which will execute jobs of any type
 Work client executes a ChainFabricateJob, update Chain to Fabricate state, for any necessitated new Link, do macro-choice and create new Link in Planned state, for each Link create scheduled LinkCraftJob and LinkDubJob
 Work client executes a LinkCraftJob, update Link to Crafting state, do main-choice and link craft, update Link to Crafted state.
 Work client executes a LinkDubJob, if Link is not in Crafted state, reject the job to be retried
 Work client executes a LinkDubJob, update Link to Dubbing state, do master dub, do ship dub, update Link to Dubbed state, job complete
 Work client executes a ChainFabricateJob and determines the Chain is complete, then update the Chain state to Complete
 Work client executes a ChainFabricateJob and periodically does garbage collection, expiring all Links before a certain staleness, and enqueing LinkDeleteJob for those Link
 Work client executes a ChainFabricateJob and determines the Chain is no longer in a Fabricate state, cancels the recurring ChainFabricateJob
 Work client executes a ChainStopJob and updates the Chain to Stopped state.
 <p>
 # ChainDeleteJob, LinkDeleteJob
 <p>
 Hub enqueues a recurring ChainDeleteJob on delete of a Chain
 Work client executes a ChainDeleteJob and enqueues LinkDeleteJob for each Link in the Chain
 Work client executes a LinkDeleteJob and deletes the corresponding S3 object, then the Link record
 Work client executes a ChainDeleteJob and determines the Chain is empty, deletes Chain record, stops ChainDeleteJob
 <p>
 # AudioDeleteJob
 <p>
 Hub enqueues a AudioDeleteJob on delete of an Audio
 Work client executes a AudioDeleteJob and deletes the corresponding S3 object, then the Audio record
 */
public interface WorkManager {

  /**
   Start fabrication of a Chain,
   by creating a recurring `ChainFabricateJob`.

   @param chainId to begin fabricate
   */
  void startChainFabrication(BigInteger chainId);

  /**
   Stop fabrication of a Chain,
   by deleting the recurring `ChainFabricateJob`.

   @param chainId to stop fabricate
   */
  void stopChainFabrication(BigInteger chainId);

  /**
   Schedule the crafting of a Link,
   by creating a scheduled `LinkCraftJob`.

   @param delaySeconds from now to schedule job at
   @param linkId       for which to schedule Craft
   */
  void scheduleLinkFabricate(Integer delaySeconds, BigInteger linkId);

  /**
   Start erasing a Chain,
   by creating a recurring `ChainEraseJob`.

   @param chainId to begin erasing
   */
  void startChainErase(BigInteger chainId);

  /**
   Stop deletion of a Chain,
   by ending the recurring `ChainEraseJob`.

   @param chainId to stop erasing
   */
  void stopChainErase(BigInteger chainId);

  /**
   Start erasing an Audio,
   by creating a `AudioEraseJob`.

   @param audioId to begin erasing
   */
  void startAudioErase(BigInteger audioId);

  /**
   Stop deletion of a Audio,
   by ending the recurring `AudioEraseJob`.

   @param audioId to stop erasing
   */
  void stopAudioErase(BigInteger audioId);

  /**
   Schedule the cloning of a Instrument,
   by creating a scheduled `InstrumentCloneJob`.
   * @param delaySeconds from now to schedule job at
   @param fromId for which to schedule Clone
   @param toId    to clone instrument to

   */
  void scheduleInstrumentClone(Integer delaySeconds, BigInteger fromId, BigInteger toId);

  /**
   Schedule the cloning of a Audio,
   by creating a scheduled `AudioCloneJob`.
   * @param delaySeconds from now to schedule job at
   @param fromId      for which to schedule Clone
   @param toId    to clone audio to

   */
  void scheduleAudioClone(Integer delaySeconds, BigInteger fromId, BigInteger toId);

  /**
   Schedule the cloning of a Pattern,
   by creating a scheduled `PatternCloneJob`.
   * @param delaySeconds from now to schedule job at
   @param fromId for which to schedule Clone
   @param toId    to clone pattern to

   */
  void schedulePatternClone(Integer delaySeconds, BigInteger fromId, BigInteger toId);

  /**
   Schedule the cloning of a Phase,
   by creating a scheduled `PhaseCloneJob`.
   * @param delaySeconds from now to schedule job at
   @param fromId      for which to schedule Clone
   @param toId    to clone phase to

   */
  void schedulePhaseClone(Integer delaySeconds, BigInteger fromId, BigInteger toId);

  /**
   Get a Worker

   @param jobFactory to get job from
   @return worker
   */
  Worker getWorker(JobFactory jobFactory);

  /**
   Get all work. Each possible type+target combination is either Expected or Queued.
   Only recurring jobs can be considered "expected work"
   <p>
   [#153266872] Admin wants Work tab in order to monitor current platform workload, and a Reinstate All Jobs button to ensure all jobs are up and running

   @return collection of all work
   @throws Exception on failure
   */
  Collection<Work> readAllWork() throws Exception;

  /**
   Reinstate all work. Each possible type+target combination is either Expected or Queued.
   Only recurring jobs can be considered "expected work"
   <p>
   Begins with any Expected work returned by readAllWork()
   (work that was not updated to Queued, meaning that no Jesque job exists)--
   then instantiates a job for each Expected work.
   <p>
   [#153266964] AWS cron job framework implemented in order to call the Reinstate All Jobs endpoint in production every 60 seconds.

   @return collection of work that was reinstated
   @throws Exception on failure
   */
  Collection<Work> reinstateAllWork() throws Exception;
}
