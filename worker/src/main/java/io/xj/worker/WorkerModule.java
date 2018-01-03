// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioCloneJob;
import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.InstrumentCloneJob;
import io.xj.worker.job.LinkFabricateJob;
import io.xj.worker.job.PatternCloneJob;
import io.xj.worker.job.PhaseCloneJob;
import io.xj.worker.job.impl.AudioCloneJobImpl;
import io.xj.worker.job.impl.AudioEraseJobImpl;
import io.xj.worker.job.impl.ChainEraseJobImpl;
import io.xj.worker.job.impl.ChainFabricateJobImpl;
import io.xj.worker.job.impl.InstrumentCloneJobImpl;
import io.xj.worker.job.impl.LinkFabricateJobImpl;
import io.xj.worker.job.impl.PatternCloneJobImpl;
import io.xj.worker.job.impl.PhaseCloneJobImpl;

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
      .implement(ChainFabricateJob.class, ChainFabricateJobImpl.class)
      .implement(LinkFabricateJob.class, LinkFabricateJobImpl.class)
      .implement(AudioEraseJob.class, AudioEraseJobImpl.class)
      .implement(ChainEraseJob.class, ChainEraseJobImpl.class)
      .implement(InstrumentCloneJob.class, InstrumentCloneJobImpl.class)
      .implement(AudioCloneJob.class, AudioCloneJobImpl.class)
      .implement(PatternCloneJob.class, PatternCloneJobImpl.class)
      .implement(PhaseCloneJob.class, PhaseCloneJobImpl.class)
      .build(JobTargetFactory.class));
  }

}
