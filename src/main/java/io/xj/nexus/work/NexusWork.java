// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.Chain;
import io.xj.Segment;

import java.util.Collection;

/**
 The Lab Nexus Distributed Work Manager
 <p>
 THERE IS NO SPOON
 <p>
 Any node of Nexus is responsible for all of its own Work.
 The Work Manager maintains a work loop from its.start() to its.finish():
 - Every N seconds (default 1)
 - Stream all Chains from the local entity store-- determine which require Work, and cache the latest state
 - Stream all Segments from the local entity store-- determine which require Work, based on the Chain-work state cache
 <p>
 IMPLEMENTS [#171553408] XJ Lab Distributed Hub/Nexus Architecture, by removing all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!
 <p>
 FUTURE: [#153266872] Admin wants Work tab in order to monitor current platform workload, and a Reinstate All Jobs button to ensure all jobs are up and running
 <p>
 DEPRECATED: [#153266964] AWS cron job framework implemented in order to call the Reinstate All Jobs endpoint in production every 60 seconds.
 <p>
 DEPRECATED: [#286] True Chain-Segment work management
 */
public interface NexusWork extends Runnable {

  /**
   Compute the fabricated-ahead seconds for any collection of Segments

   @param segments for which to get fabricated-ahead seconds
   @return fabricated-ahead seconds for this collection of Segments
   */
  float computeFabricatedAheadSeconds(Chain chain, Collection<Segment> segments);

  /**
   This method just does work until failure, blocks until interrupted
   */
  void work();

  /**
   Stop work
   */
  void stop();

  /**
   Whether the next cycle nanos is above threshold, compared to System.nanoTime();

   @return next cycle nanos
   */
  boolean isHealthy();
}
