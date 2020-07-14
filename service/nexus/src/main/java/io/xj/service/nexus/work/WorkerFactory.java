// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import com.google.inject.assistedinject.Assisted;

import java.util.UUID;

/**
 Nexus Worker Factory
 */
public interface WorkerFactory {

  /**
   Create the Boss worker

   @return single instance of Boss runnable
   */
  BossWorker boss();

  /**
   Create the Janitor worker

   @return single instance of Janitor runnable
   */
  JanitorWorker janitor();

  /**
   Create the Medic worker

   @return single instance of Medic runnable
   */
  MedicWorker medic();

  /**
   Create a Chain  worker

   @param chainId chain id
   @return instance of runnable worker
   */
  ChainWorker chain(@Assisted UUID chainId);

  /**
   Create a Chain Segment worker

   @param segmentId segment id
   @return instance of runnable worker
   */
  FabricatorWorker segment(@Assisted UUID segmentId);

}
