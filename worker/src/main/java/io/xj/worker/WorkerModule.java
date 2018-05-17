// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioCloneJob;
import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.InstrumentCloneJob;
import io.xj.worker.job.SegmentFabricateJob;
import io.xj.worker.job.SequenceCloneJob;
import io.xj.worker.job.SequenceEraseJob;
import io.xj.worker.job.PatternCloneJob;
import io.xj.worker.job.PatternEraseJob;
import io.xj.worker.job.impl.AudioCloneJobImpl;
import io.xj.worker.job.impl.AudioEraseJobImpl;
import io.xj.worker.job.impl.ChainEraseJobImpl;
import io.xj.worker.job.impl.ChainFabricateJobImpl;
import io.xj.worker.job.impl.InstrumentCloneJobImpl;
import io.xj.worker.job.impl.SegmentFabricateJobImpl;
import io.xj.worker.job.impl.SequenceCloneJobImpl;
import io.xj.worker.job.impl.SequenceEraseJobImpl;
import io.xj.worker.job.impl.PatternCloneJobImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import io.xj.worker.job.impl.PatternEraseJobImpl;
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
      .implement(AudioCloneJob.class, AudioCloneJobImpl.class)
      .implement(AudioEraseJob.class, AudioEraseJobImpl.class)
      .implement(ChainEraseJob.class, ChainEraseJobImpl.class)
      .implement(ChainFabricateJob.class, ChainFabricateJobImpl.class)
      .implement(InstrumentCloneJob.class, InstrumentCloneJobImpl.class)
      .implement(SegmentFabricateJob.class, SegmentFabricateJobImpl.class)
      .implement(SequenceCloneJob.class, SequenceCloneJobImpl.class)
      .implement(SequenceEraseJob.class, SequenceEraseJobImpl.class)
      .implement(PatternCloneJob.class, PatternCloneJobImpl.class)
      .implement(PatternEraseJob.class, PatternEraseJobImpl.class)
      .build(JobTargetFactory.class));
  }

}
