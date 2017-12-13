// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dub.impl;

import io.xj.core.config.Config;
import io.xj.core.dub.ShipDub;
import io.xj.core.exception.BusinessException;
import io.xj.core.work.basis.Basis;
import io.xj.core.external.amazon.AmazonProvider;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 [#264] Link audio is compressed to MP3 and shipped to https://link.xj.io
 */
public class ShipDubImpl implements ShipDub {
//  private final Logger log = LoggerFactory.getLogger(ShipDubImpl.class);
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
    // future: send report
  }

}
