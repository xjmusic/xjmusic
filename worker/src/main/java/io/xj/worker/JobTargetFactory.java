// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import com.google.inject.assistedinject.Assisted;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.SegmentFabricateJob;

import java.util.UUID;

/**
 Job Factory
 */
public interface JobTargetFactory {

  /**
   ChainErase job instance

   @param entityId to delete
   @return job instance
   */
  ChainEraseJob makeChainEraseJob(
    @Assisted("entityId") UUID entityId
  );

  /**
   ChainFabricateJob instance

   @param entityId chain id
   @return job instance
   */
  ChainFabricateJob makeChainFabricateJob(
    @Assisted("entityId") UUID entityId
  );

  /**
   SegmentCreateJob instance

   @param entityId segment id
   @return job instance
   */
  SegmentFabricateJob makeSegmentFabricateJob(
    @Assisted("entityId") UUID entityId
  );

}
