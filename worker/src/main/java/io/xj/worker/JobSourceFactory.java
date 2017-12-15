// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
 pluggable to Jesque job factory API.
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
    BigInteger entityId = new BigInteger(String.valueOf(args[0]));
    if (0 == entityId.compareTo(BigInteger.valueOf(0))) {
      throw new WorkException("Job requires non-zero entity id");
    }

    return makeTarget(workType, entityId);
  }

  /**
   target is switched on job type

   @param workType type of job
   @param entityId target entity
   @return job runnable
   */
  private Runnable makeTarget(WorkType workType, BigInteger entityId) throws WorkException {
    switch (workType) {

      case AudioErase:
        return jobTargetFactory.makeAudioEraseJob(entityId);

      case ChainErase:
        return jobTargetFactory.makeChainEraseJob(entityId);

      case ChainFabricate:
        return jobTargetFactory.makeChainFabricateJob(entityId);

      case LinkCraft:
        return jobTargetFactory.makeLinkCraftJob(entityId);

      case LinkDub:
        return jobTargetFactory.makeLinkDubJob(entityId);

      default:
        throw new WorkException("Invalid Job Type");
    }
  }


}
