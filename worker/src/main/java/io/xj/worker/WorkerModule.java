// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker;

import io.xj.mixer.MixerModule;
import io.xj.worker.work.craft.CraftFactory;
import io.xj.worker.work.craft.FoundationCraft;
import io.xj.worker.work.craft.StructureCraft;
import io.xj.worker.work.craft.VoiceCraft;
import io.xj.worker.work.craft.impl.FoundationCraftImpl;
import io.xj.worker.work.craft.impl.StructureCraftImpl;
import io.xj.worker.work.craft.impl.VoiceCraftImpl;
import io.xj.worker.work.dub.DubFactory;
import io.xj.worker.work.dub.MasterDub;
import io.xj.worker.work.dub.ShipDub;
import io.xj.worker.work.dub.impl.MasterDubImpl;
import io.xj.worker.work.dub.impl.ShipDubImpl;
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
    installCraftFactory();
    installJobTargetFactory();
    installDubFactory();
    install(new MixerModule());
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

  private void installCraftFactory() {
    install(new FactoryModuleBuilder()
      .implement(FoundationCraft.class, FoundationCraftImpl.class)
      .implement(StructureCraft.class, StructureCraftImpl.class)
      .implement(VoiceCraft.class, VoiceCraftImpl.class)
      .build(CraftFactory.class));
  }

  private void installDubFactory() {
    install(new FactoryModuleBuilder()
      .implement(MasterDub.class, MasterDubImpl.class)
      .implement(ShipDub.class, ShipDubImpl.class)
      .build(DubFactory.class));
  }

}
