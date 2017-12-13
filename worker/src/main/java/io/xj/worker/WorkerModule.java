// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.LinkCraftJob;
import io.xj.worker.job.LinkDubJob;
import io.xj.worker.job.impl.AudioEraseJobImpl;
import io.xj.worker.job.impl.ChainEraseJobImpl;
import io.xj.worker.job.impl.ChainFabricateJobImpl;
import io.xj.worker.job.impl.LinkCraftJobImpl;
import io.xj.worker.job.impl.LinkDubJobImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

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
      .implement(AudioEraseJob.class, AudioEraseJobImpl.class)
      .implement(ChainEraseJob.class, ChainEraseJobImpl.class)
      .implement(ChainFabricateJob.class, ChainFabricateJobImpl.class)
      .implement(LinkCraftJob.class, LinkCraftJobImpl.class)
      .implement(LinkDubJob.class, LinkDubJobImpl.class)
      .build(JobTargetFactory.class));
  }

}
