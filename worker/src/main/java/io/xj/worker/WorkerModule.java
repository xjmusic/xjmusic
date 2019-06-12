// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.SegmentFabricateJob;
import io.xj.worker.job.impl.ChainEraseJobImpl;
import io.xj.worker.job.impl.ChainFabricateJobImpl;
import io.xj.worker.job.impl.SegmentFabricateJobImpl;
import net.greghaines.jesque.worker.JobFactory;

public class WorkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
    installJobTargetFactory();
  }

  private void bindWorker() {
    bind(JobFactory.class).to(JobSourceFactory.class);
  }

  private void installJobTargetFactory() {
    install(new FactoryModuleBuilder()
      .implement(ChainEraseJob.class, ChainEraseJobImpl.class)
      .implement(ChainFabricateJob.class, ChainFabricateJobImpl.class)
      .implement(SegmentFabricateJob.class, SegmentFabricateJobImpl.class)
      .build(JobTargetFactory.class));
  }

}
