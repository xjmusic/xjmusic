package io.outright.xj.core.work;

import io.outright.xj.core.app.exception.ConfigException;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public interface WorkFactory {

  /**
   * Create a Work Leader
   * for Links in a particular state
   *
   * @param fromState    Link to be worked on
   * @param aheadSeconds to work on Link
   * @return ChainWorkMaster
   * @throws ConfigException on failure
   */
  Leader createLeader(
    @Assisted("fromState") String fromState,
    @Assisted("aheadSeconds") int aheadSeconds,
    @Assisted("batchSize") int batchSize
  ) throws ConfigException;

  /**
   * Create a Work Leader
   * for nonexistent Links
   *
   * @param aheadSeconds to create new Link
   * @return ChainWorkMaster
   * @throws ConfigException on failure
   */
  Leader createLeader(
    @Assisted("aheadSeconds") int aheadSeconds,
    @Assisted("batchSize") int batchSize
  ) throws ConfigException;

  /**
   * Create a Worker
   * for Links in a working state that transition to a finished state
   *
   * @param workingState Link during work
   * @param finishedState      Link after work is complete
   * @return ChainWorkMaster
   * @throws ConfigException on failure
   */
  Worker createWorker(
    @Assisted("workingState") String workingState,
    @Assisted("finishedState") String finishedState,
    @Assisted("operation") WorkerOperation operation
  ) throws ConfigException;

  /**
   * Create a Worker
   * for new Links
   *
   * @param finishedState of new Link
   * @return ChainWorkMaster
   * @throws ConfigException on failure
   */
  Worker createWorker(
    @Assisted("finishedState") String finishedState
  ) throws ConfigException;

}
