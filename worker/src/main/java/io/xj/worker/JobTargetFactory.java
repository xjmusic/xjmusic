// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker;

import io.xj.worker.job.AudioCloneJob;
import io.xj.worker.job.AudioEraseJob;
import io.xj.worker.job.ChainEraseJob;
import io.xj.worker.job.ChainFabricateJob;
import io.xj.worker.job.InstrumentCloneJob;
import io.xj.worker.job.LinkFabricateJob;
import io.xj.worker.job.PatternCloneJob;
import io.xj.worker.job.PatternEraseJob;
import io.xj.worker.job.PhaseCloneJob;
import io.xj.worker.job.PhaseEraseJob;

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
   PatternErase job instance
   [#154887174] PatternErase job erase a Pattern and all its Phases in the background, in order to keep the UI functioning at a reasonable speed.

   @param entityId to delete
   @return job instance
   */
  PatternEraseJob makePatternEraseJob(
    @Assisted("entityId") BigInteger entityId
  );

  /**
   PhaseErase job instance
   [#153976888] PhaseErase job erase a Phase in the background, in order to keep the UI functioning at a reasonable speed.

   @param entityId to delete
   @return job instance
   */
  PhaseEraseJob makePhaseEraseJob(
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
   LinkCreateJob instance

   @param entityId link id
   @return job instance
   */
  LinkFabricateJob makeLinkFabricateJob(
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
   PatternCloneJob instance

   @param fromId entity to clone from
   @param toId to clone pattern into
   @return job instance
   */
  PatternCloneJob makePatternCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

  /**
   PhaseCloneJob instance

   @param fromId entity to clone from
   @param toId to clone phase into
   @return job instance
   */
  PhaseCloneJob makePhaseCloneJob(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId
  );

}
