// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import java.util.Collection;

/**
 The Mk3 Nexus Distributed Work Manager
 <p>
 THERE IS NO SPOON
 <p>
 Any node of Nexus is responsible for all of its own Work.
 The Work Manager maintains a work loop from its.start() to its.finish():
 - Every N seconds (default 1)
 - Stream all Chains from the local entity store-- determine which require Work, and cache the latest state
 - Stream all Segments from the local entity store-- determine which require Work, based on the Chain-work state cache
 <p>
 IMPLEMENTS [#171553408] XJ Mk3 Distributed Hub/Nexus Architecture, by removing all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!
 <p>
 FUTURE: [#153266872] Admin wants Work tab in order to monitor current platform workload, and a Reinstate All Jobs button to ensure all jobs are up and running
 <p>
 DEPRECATED: [#153266964] AWS cron job framework implemented in order to call the Reinstate All Jobs endpoint in production every 60 seconds.
 <p>
 DEPRECATED: [#286] True Chain-Segment work management
 */
public interface NexusWork {

  /**
   Start performing work
   */
  void start();

  /**
   Finish performing work
   */
  void finish();

  /**
   Cancel all current work
   */
  void cancelAllChainWork();

  /**
   Whether this Nexus is working on Chain with the given ID

   @param id to check for working status
   @return true if Nexus is working on the given Chain ID
   */
  boolean isWorkingOnChain(String id);

  /**
   Begin scheduled work on a chain

   @param chainId to begin work for
   */
  void beginChainWork(String chainId);

  /**
   Explicit and safe shutdown of a Work executor service

   @param chainId to cancel & shutdown work for
   */
  void cancelChainWork(String chainId);

  /**
   Get all IDs of Chains that we are currently working on

   @return IDs of Chains that we are currently working on
   */
  Collection<String> getChainWorkingIds();
}
