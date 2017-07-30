// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang;

import io.xj.core.app.exception.ConfigException;
import io.xj.core.model.link.LinkState;

import com.google.inject.assistedinject.Assisted;

public interface ChainGangFactory {

  /**
   Create a Work Leader
   for Entities in a particular state

   @return Leader
   @throws ConfigException on failure
    @param fromState     Entity to be worked on
   @param bufferSeconds to work on Entity
   @param batchSize     at a time
   */
  Leader createLeader(
    @Assisted("fromState") LinkState fromState,
    @Assisted("bufferSeconds") int bufferSeconds,
    @Assisted("batchSize") int batchSize
  ) throws ConfigException;

  /**
   Create a Work Leader
   for nonexistent Entities

   @param bufferSeconds to create new Entity
   @param batchSize     at a time
   @return Leader
   @throws ConfigException on failure
   */
  Leader createLeader(
    @Assisted("bufferSeconds") int bufferSeconds,
    @Assisted("batchSize") int batchSize
  ) throws ConfigException;

  /**
   Create a Worker
   for Entities in a working state that transition to a finished state

   @return Worker
   @throws ConfigException on failure
    @param workingState  Entity during work
   @param finishedState Entity after work is complete
   @param operation     to perform
   */
  Follower createFollower(
    @Assisted("workingState") LinkState workingState,
    @Assisted("finishedState") LinkState finishedState,
    @Assisted("operation") ChainGangOperation operation
  ) throws ConfigException;

  /**
   Create a Worker
   for new Entities

   @return Worker
   @throws ConfigException on failure
    @param finishedState of new Entity
   */
  Follower createFollower(
    @Assisted("finishedState") LinkState finishedState
  ) throws ConfigException;

}
