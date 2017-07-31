// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.work;

import org.jooq.types.ULong;

/**

 [#286] True Chain-Link work management
   (deprecating each link worker reading whatever links are in a certain state)
   in order to prevent the train wrecks we are already seeing at small scale.

 # ChainFabricateJob, ChainStopJob, LinkCraftJob, LinkDubJob

   * Work module deprecates craftworker, dubworker, and eraseworker. These modules will likely be moved into sub-modules of the new worker module
   * Hub enqueues a recurring ChainFabricateJob on creation of a Chain
   * Hub enqueues a recurring ChainFabricateJob on retry of a Chain in a Failed state
   * Hub stops any recurring ChainFabricateJob and creates a ChainStopJob on completion or deletion of a Chain
   * Work establishes a pool of threads to run Clients which will execute jobs of any type
   * Work client executes a ChainFabricateJob, update Chain to Fabricating state, for any necessitated new Link, do macro-choice and create new Link in Planned state, for each Link create scheduled LinkCraftJob and LinkDubJob
   * Work client executes a LinkCraftJob, update Link to Crafting state, do main-choice and link craft, update Link to Crafted state.
   * Work client executes a LinkDubJob, if Link is not in Crafted state, reject the job to be retried
   * Work client executes a LinkDubJob, update Link to Dubbing state, do master dub, do ship dub, update Link to Dubbed state, job complete
   * Work client executes a ChainFabricateJob and determines the Chain is complete, then update the Chain state to Complete
   * Work client executes a ChainFabricateJob and periodically does garbage collection, expiring all Links before a certain staleness, and enqueing LinkDeleteJob for those Link
   * Work client executes a ChainFabricateJob and determines the Chain is no longer in a Fabricating state, cancels the recurring ChainFabricateJob
   * Work client executes a ChainStopJob and updates the Chain to Stopped state.

 # ChainDeleteJob, LinkDeleteJob

   * Hub enqueues a recurring ChainDeleteJob on delete of a Chain
   * Work client executes a ChainDeleteJob and enqueues LinkDeleteJob for each Link in the Chain
   * Work client executes a LinkDeleteJob and deletes the corresponding S3 object, then the Link record
   * Work client executes a ChainDeleteJob and determines the Chain is empty, deletes Chain record, stops ChainDeleteJob

 # AudioDeleteJob

   * Hub enqueues a AudioDeleteJob on delete of an Audio
   * Work client executes a AudioDeleteJob and deletes the corresponding S3 object, then the Audio record

 */
public interface WorkManager {

  /**
   Start fabrication of a Chain,
   by creating a recurring `ChainFabricateJob`.

   @param chainId  to begin fabricating
   */
  void startChainFabrication(ULong chainId);

  /**
   Stop fabrication of a Chain,
   by deleting the recurring `ChainFabricateJob`.

   @param chainId  to stop fabricating
   */
  void stopChainFabrication(ULong chainId);

  /**
   Schedule the crafting of a Link,
   by creating a scheduled `LinkCraftJob`.

   @param linkId for which to schedule Craft
   @param delaySeconds from now to schedule job at
   */
  void scheduleLinkCraft(ULong linkId, Integer delaySeconds);

  /**
   Schedule the dubbing of a Link,
   by creating a scheduled `LinkDubJob`.

   @param linkId for which to schedule Dub
   @param delaySeconds from now to schedule job at
   */
  void scheduleLinkDub(ULong linkId, Integer delaySeconds);

  /**
   Start deletion of a Chain,
   by creating a recurring `ChainDeletionJob`.

   @param chainId  to begin deleting
   */
  void startChainDeletion(ULong chainId);

  /**
   Stop deletion of a Chain,
   by deleting the recurring `ChainDeletionJob`.

   @param chainId  to stop deleting
   */
  void stopChainDeletion(ULong chainId);

  /**
   Schedule the deletion of a Link,
   by creating a `LinkDeletionJob`.

   @param linkId for which to create Deletion job
   */
  void doLinkDeletion(ULong linkId);

  /**
   Schedule the deletion of a Audio,
   by creating a `AudioDeletionJob`.

   @param audioId for which to create Deletion job
   */
  void doAudioDeletion(ULong audioId);

}
