// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

/**
 The Nexus Work Chain Bootstrap operates in one of two modes,
 depending on whether Nexus is being run as a Hub sidecar or for Yard production.
 <p>
 LAB_MODE: Runs as a sidecar to a Hub, maintaining a chain for each of its template playbacks.
 YARD_MODE: Runs in production, maintaining one specific chain.
 <p>
 - Templates: enhanced preview chain creation for artists in Lab UI #178457569
 - Nexus bootstraps Chains from JSON file on startup #176285826
 */
public interface NexusWorkChainManager {
  /**
   Check current status, change states if necessary, perform actions or blow up.
   Called from within the Nexus Work loop.
   */
  void poll();

  /**
   Test whether all expected chains are healthy, depending on chain manager mode

   @return true if all chains are healthy
   */
  boolean isHealthy();
}
