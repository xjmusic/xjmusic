// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.impl;

import io.xj.core.model.job.JobType;

import net.greghaines.jesque.Job;
import net.greghaines.jesque.worker.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobFactoryImpl implements JobFactory {
  private static final Logger log = LoggerFactory.getLogger(JobFactoryImpl.class);

  /**
   Materializes a job.

   @param job the job to materialize
   @return the materialized job
   @throws Exception if there was an exception creating the object
   */
  @Override
  public Object materializeJob(Job job) throws Exception {
    JobType jobType = JobType.validate(job.getClassName());
    log.info("TODO: materialize job {}", jobType);
    return null;
  }
}
