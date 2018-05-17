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

import com.google.inject.assistedinject.Assisted;

import java.math.BigInteger;

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
   SequenceErase job instance
   [#154887174] SequenceErase job erase a Sequence and all its Patterns in the background, in order to keep the UI functioning at a reasonable speed.

   @param entityId to delete
   @return job instance
   */
  SequenceEraseJob makeSequenceEraseJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   PatternErase job instance
   [#153976888] PatternErase job erase a Pattern in the background, in order to keep the UI functioning at a reasonable speed.

   @param entityId to delete
   @return job instance
   */
  PatternEraseJob makePatternEraseJob(
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
   ChainFabricateJob instance

   @param entityId chain id
   @return job instance
   */
  ChainFabricateJob makeChainFabricateJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   SegmentCreateJob instance

   @param entityId segment id
   @return job instance
   */
  SegmentFabricateJob makeSegmentFabricateJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   InstrumentCloneJob instance

   @param fromId entity to clone from
   @param toId to clone instrument into
   @return job instance
   */
  InstrumentCloneJob makeInstrumentCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

  /**
   AudioCloneJob instance

   @param fromId entity to clone from
   @param toId to clone audio into
   @return job instance
   */
  AudioCloneJob makeAudioCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

  /**
   SequenceCloneJob instance

   @param fromId entity to clone from
   @param toId to clone sequence into
   @return job instance
   */
  SequenceCloneJob makeSequenceCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

  /**
   PatternCloneJob instance

   @param fromId entity to clone from
   @param toId to clone pattern into
   @return job instance
   */
  PatternCloneJob makePatternCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

}
