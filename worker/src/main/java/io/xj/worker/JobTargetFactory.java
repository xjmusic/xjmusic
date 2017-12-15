// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.LinkCraftJob;
import io.xj.worker.job.LinkDubJob;

import java.math.BigInteger;

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
    @Assisted("entityId") BigInteger entityId
  );

  /**
   AudioErase job instance

   @param entityId to delete
   @return job instance
   */
  AudioEraseJob makeAudioEraseJob(
    @Assisted("entityId") BigInteger entityId
  );


  /**
   ChainFabricateJobImpl job instance

   @param entityId chain id
   @return job instance
   */
  ChainFabricateJob makeChainFabricateJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   LinkCraftJobImpl job instance

   @param entityId link id
   @return job instance
   */
  LinkCraftJob makeLinkCraftJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   LinkDubJobImpl job instance

   @param entityId link id
   @return job instance
   */
  LinkDubJob makeLinkDubJob(
    @Assisted("entityId") BigInteger entityId
  );
}
