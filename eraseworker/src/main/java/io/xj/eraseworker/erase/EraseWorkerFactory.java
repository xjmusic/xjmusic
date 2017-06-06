// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.eraseworker.erase;

import io.xj.core.app.exception.ConfigException;

import com.google.inject.assistedinject.Assisted;

/**
 Erase is performed:
 1. Erase
 <p>
 Basis basis = basisFactory.createBasis(link);
 eraseFactory.master(basis).erase();
 basis.sendReport();
 */
public interface EraseWorkerFactory {

  /**
   Chain Erase worker instance

   @param batchSize at a time
   @return worker instance
   @throws ConfigException on failure
   */
  ChainEraseWorker chainEraseWorker(
    @Assisted("batchSize") Integer batchSize
  ) throws ConfigException;

  /**
   Audio Erase worker instance

   @param batchSize at a time
   @return worker instance
   @throws ConfigException on failure
   */
  AudioEraseWorker audioEraseWorker(
    @Assisted("batchSize") Integer batchSize
  ) throws ConfigException;

}
