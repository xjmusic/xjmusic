// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dub.DubModule;
import io.xj.service.nexus.fabricator.FabricatorModule;
import net.greghaines.jesque.worker.JobFactory;

public class NexusModule extends AbstractModule {
  protected void configure() {
    bind(JobFactory.class).to(JobSourceFactory.class);
    install(new FabricatorModule());
    install(new CraftModule());
    install(new DubModule());
    install(new FactoryModuleBuilder()
      .implement(ChainEraseJob.class, ChainEraseJobImpl.class)
      .implement(ChainFabricateJob.class, ChainFabricateJobImpl.class)
      .implement(SegmentFabricateJob.class, SegmentFabricateJobImpl.class)
      .build(JobTargetFactory.class));
  }
}
