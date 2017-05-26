// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.dubworker.dub.impl;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.basis.Basis;
import io.outright.xj.core.external.amazon.AmazonProvider;
import io.outright.xj.dubworker.dub.ShipDub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 [#264] Link audio is compressed to MP3 and shipped to https://link.xj.outright.io
 */
public class ShipDubImpl implements ShipDub {
  //  private final Logger log = LoggerFactory.getLogger(MasterDubImpl.class);
  private final Basis basis;
  private final AmazonProvider amazonProvider;

  @Inject
  public ShipDubImpl(
    @Assisted("basis") Basis basis,
    AmazonProvider amazonProvider
  /*-*/) throws BusinessException {
    this.basis = basis;
    this.amazonProvider = amazonProvider;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      shipFinalAudio();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type ShipDub for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }


  /**
   Ship the final audio

   @throws Exception on failure
   */
  private void shipFinalAudio() throws Exception {
    amazonProvider.putS3Object(
      basis.outputFilePath(),
      Config.linkFileBucket(),
      basis.link().getWaveformKey());
  }

  /**
   report things
   */
  private void report() {
    // TODO send report
  }

}
