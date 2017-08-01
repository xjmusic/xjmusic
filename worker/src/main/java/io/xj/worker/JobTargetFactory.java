// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.LinkCraftJob;
import io.xj.worker.job.LinkDubJob;

import org.jooq.types.ULong;

import com.google.inject.assistedinject.Assisted;

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
    @Assisted("entityId") ULong entityId
  );

  /**
   AudioErase job instance

   @param entityId to delete
   @return job instance
   */
  AudioEraseJob makeAudioEraseJob(
    @Assisted("entityId") ULong entityId
  );


  /**
   ChainFabricateJobImpl job instance

   @param entityId chain id
   @return job instance
   */
  ChainFabricateJob makeChainFabricateJob(
    @Assisted("entityId") ULong entityId
  );

  /**
   LinkCraftJobImpl job instance

   @param entityId link id
   @return job instance
   */
  LinkCraftJob makeLinkCraftJob(
    @Assisted("entityId") ULong entityId
  );

  /**
   LinkDubJobImpl job instance

   @param entityId link id
   @return job instance
   */
  LinkDubJob makeLinkDubJob(
    @Assisted("entityId") ULong entityId
  );
}
