// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.core.exception.WorkException;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkType;

import com.google.inject.Inject;

import net.greghaines.jesque.Job;
import net.greghaines.jesque.worker.JobFactory;

import java.math.BigInteger;
import java.util.Map;
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
    Map<String, Object> vars = job.getVars();
    if (Objects.isNull(vars) || 1 > vars.size()) {
      throw new WorkException("Job ought to have been created with at least 1 named variable");
    }

    return makeTarget(workType, vars);
  }

  /**
   target is switched on job type

   @return job runnable
   @param workType type of job
   @param vars     target arguments (the first one needs to be a BigInteger target entity id)
   */
  private Runnable makeTarget(WorkType workType, Map<String, Object> vars) throws WorkException {
    switch (workType) {

      case AudioClone:
        return jobTargetFactory.makeAudioCloneJob(entityId(vars.get(Work.KEY_SOURCE_ID)), entityId(vars.get(Work.KEY_TARGET_ID)));

      case AudioErase:
        return jobTargetFactory.makeAudioEraseJob(entityId(vars.get(Work.KEY_TARGET_ID)));

      case ChainErase:
        return jobTargetFactory.makeChainEraseJob(entityId(vars.get(Work.KEY_TARGET_ID)));

      case ChainFabricate:
        return jobTargetFactory.makeChainFabricateJob(entityId(vars.get(Work.KEY_TARGET_ID)));

      case InstrumentClone:
        return jobTargetFactory.makeInstrumentCloneJob(entityId(vars.get(Work.KEY_SOURCE_ID)), entityId(vars.get(Work.KEY_TARGET_ID)));

      case SegmentFabricate:
        return jobTargetFactory.makeSegmentFabricateJob(entityId(vars.get(Work.KEY_TARGET_ID)));

      case SequenceClone:
        return jobTargetFactory.makeSequenceCloneJob(entityId(vars.get(Work.KEY_SOURCE_ID)), entityId(vars.get(Work.KEY_TARGET_ID)));

      case SequenceErase:
        return jobTargetFactory.makeSequenceEraseJob(entityId(vars.get(Work.KEY_TARGET_ID)));

      case PatternClone:
        return jobTargetFactory.makePatternCloneJob(entityId(vars.get(Work.KEY_SOURCE_ID)), entityId(vars.get(Work.KEY_TARGET_ID)));

      case PatternErase:
        return jobTargetFactory.makePatternEraseJob(entityId(vars.get(Work.KEY_TARGET_ID)));

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
