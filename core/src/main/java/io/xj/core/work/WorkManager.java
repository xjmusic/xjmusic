// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work;

import io.xj.core.model.Work;
import io.xj.core.model.WorkState;
import io.xj.core.model.WorkType;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;

import java.util.Collection;
import java.util.UUID;

/**
 [#286] True Chain-Segment work management
 (deprecating each segment worker reading whatever segments are in a certain state)
 in order to prevent the train wrecks we are already seeing at small scale.
 <p>
 # ChainFabricateJob, ChainStopJob, SegmentCraftJob, SegmentDubJob
 <p>
 Work module deprecates craftworker, dubworker, and eraseworker. These modules will likely be moved into sub-modules of the new worker module
 Hub enqueues a recurring ChainFabricateJob on creation of a Chain
 Hub enqueues a recurring ChainFabricateJob on retry of a Chain in a Failed state
 Hub stops any recurring ChainFabricateJob and ofs a ChainStopJob on completion or deletion of a Chain
 Work establishes a pool of threads to run Clients which will execute jobs of any type
 Work client executes a ChainFabricateJob, update Chain to Fabricate state, for any necessitated new Segment, do macro-choice and of new Segment in Planned state, for each Segment of scheduled SegmentCraftJob and SegmentDubJob
 Work client executes a SegmentCraftJob, update Segment to Crafting state, do main-choice and segment craft, update Segment to Crafted state.
 Work client executes a SegmentDubJob, if Segment is not in Crafted state, reject the job to be retried
 Work client executes a SegmentDubJob, update Segment to Dubbing state, do master dub, do ship dub, update Segment to Dubbed state, job complete
 Work client executes a ChainFabricateJob and determines the Chain is complete, then update the Chain state to Complete
 Work client executes a ChainFabricateJob and periodically does garbage collection, expiring all Segments before a certain staleness, and enqueing SegmentDeleteJob for those Segment
 Work client executes a ChainFabricateJob and determines the Chain is no longer in a Fabricate state, cancels the recurring ChainFabricateJob
 Work client executes a ChainStopJob and updates the Chain to Stopped state.
 <p>
 # ChainDeleteJob, SegmentDeleteJob
 <p>
 Hub enqueues a recurring ChainDeleteJob on delete of a Chain
 Work client executes a ChainDeleteJob and enqueues SegmentDeleteJob for each Segment in the Chain
 Work client executes a SegmentDeleteJob and deletes the corresponding S3 object, then the Segment record
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
   by creating a recurring `ChainFabricateJob`.@param chainId to begin fabricate
   */
  void startChainFabrication(UUID chainId);

  /**
   Stop fabrication of a Chain,
   by deleting the recurring `ChainFabricateJob`.@param chainId to stop fabricate
   */
  void stopChainFabrication(UUID chainId);

  /**
   Schedule the crafting of a Segment,
   by creating a scheduled `SegmentCraftJob`.@param delaySeconds of now to schedule job at

   @param segmentId for which to schedule Craft
   */
  void scheduleSegmentFabricate(Integer delaySeconds, UUID segmentId);

  /**
   Start erasing a Chain,
   by creating a recurring `ChainEraseJob`.@param chainId to begin erasing
   */
  void startChainErase(UUID chainId);

  /**
   Stop deletion of a Chain,
   by ending the recurring `ChainEraseJob`.@param chainId to stop erasing
   */
  void stopChainErase(UUID chainId);

  /**
   Get a Worker

   @param jobFactory to get job of
   @return worker
   */
  Worker getWorker(JobFactory jobFactory);

  /**
   Get all work. Each possible type+target combination is either Expected or Queued.
   Only recurring jobs can be considered "expected work"
   <p>
   [#153266872] Admin wants Work tab in order to monitor current platform workload, and a Reinstate All Jobs button to ensure all jobs are up and running
   <p>
   NOTE: Currently, all work types must be manually accounted for within this method implementation.

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

  /**
   Determine if a job exists matching the specified criteria

   @return true if match is found
   */
  Boolean isExistingWork(WorkState state, WorkType type, UUID targetId) throws Exception;

}
