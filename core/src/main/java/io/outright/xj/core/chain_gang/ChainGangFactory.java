package io.outright.xj.core.chain_gang;

import io.outright.xj.core.app.exception.ConfigException;

import com.google.inject.assistedinject.Assisted;

public interface ChainGangFactory {

  /**
   Create a Work Leader
   for Entities in a particular state

   @param fromState     Entity to be worked on
   @param bufferSeconds to work on Entity
   @param batchSize     at a time
   @return Leader
   @throws ConfigException on failure
   */
  Leader createLeader(
    @Assisted("fromState") String fromState,
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

   @param workingState  Entity during work
   @param finishedState Entity after work is complete
   @param operation     to perform
   @return Worker
   @throws ConfigException on failure
   */
  Follower createFollower(
    @Assisted("workingState") String workingState,
    @Assisted("finishedState") String finishedState,
    @Assisted("operation") ChainGangOperation operation
  ) throws ConfigException;

  /**
   Create a Worker
   for new Entities

   @param finishedState of new Entity
   @return Worker
   @throws ConfigException on failure
   */
  Follower createFollower(
    @Assisted("finishedState") String finishedState
  ) throws ConfigException;

}
