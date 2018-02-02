// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.core.exception.WorkException;
import io.xj.core.model.work.WorkType;

import com.google.inject.Inject;

import net.greghaines.jesque.Job;
import net.greghaines.jesque.worker.JobFactory;

import java.math.BigInteger;
import java.util.Objects;

/**
 Factory to instantiate workers for jobs,
 pluggable to Jesque job macro API.
 */
public class JobSourceFactory implements JobFactory {
  //  private static final Logger log = LoggerFactory.getLogger(JobSourceFactory.class);
  private final JobTargetFactory jobTargetFactory;

  @Inject
  JobSourceFactory(
    JobTargetFactory jobTargetFactory
  ) {
    this.jobTargetFactory = jobTargetFactory;
  }

  /**
   Materializes a job.

   @param job the job to materialize
   @return the materialized job
   @throws Exception if there was an exception creating the object
   */
  @Override
  public Runnable materializeJob(Job job) throws Exception {
    WorkType workType = WorkType.validate(job.getClassName());
    Object[] args = job.getArgs();
    if (Objects.isNull(args) || 1 > args.length) {
      throw new WorkException("Job requires at least 1 argument");
    }

    return makeTarget(workType, args);
  }

  /**
   target is switched on job type

   @param workType type of job
   @param args     target arguments (the first one needs to be a BigInteger target entity id)
   @return job runnable
   */
  private Runnable makeTarget(WorkType workType, Object[] args) throws WorkException {
    switch (workType) {

      case AudioClone:
        return jobTargetFactory.makeAudioCloneJob(entityId(args[0]), entityId(args[1]));

      case AudioErase:
        return jobTargetFactory.makeAudioEraseJob(entityId(args[0]));

      case ChainErase:
        return jobTargetFactory.makeChainEraseJob(entityId(args[0]));

      case ChainFabricate:
        return jobTargetFactory.makeChainFabricateJob(entityId(args[0]));

      case InstrumentClone:
        return jobTargetFactory.makeInstrumentCloneJob(entityId(args[0]), entityId(args[1]));

      case LinkFabricate:
        return jobTargetFactory.makeLinkFabricateJob(entityId(args[0]));

      case PatternClone:
        return jobTargetFactory.makePatternCloneJob(entityId(args[0]), entityId(args[1]));

      case PatternErase:
        return jobTargetFactory.makePatternEraseJob(entityId(args[0]));

      case PhaseClone:
        return jobTargetFactory.makePhaseCloneJob(entityId(args[0]), entityId(args[1]));

      case PhaseErase:
        return jobTargetFactory.makePhaseEraseJob(entityId(args[0]));

      default:
        throw new WorkException("Invalid Job Type");
    }
  }

  /**
   Get entity id value from argument

   @param arg to parse
   @return id value
   @throws WorkException on failure
   */
  private static BigInteger entityId(Object arg) throws WorkException {
    BigInteger entityId = new BigInteger(String.valueOf(arg));
    if (0 == entityId.compareTo(BigInteger.valueOf(0))) {
      throw new WorkException("Job requires non-zero entity id");
    }
    return entityId;
  }


}
