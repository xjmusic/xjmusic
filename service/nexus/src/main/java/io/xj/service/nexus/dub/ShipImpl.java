// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.service.hub.HubException;
import io.xj.service.hub.persistence.AmazonProvider;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.craft.exception.CraftException;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class ShipImpl implements Ship {
  //  private final Logger log = LoggerFactory.getLogger(ShipDubImpl.class);
  private final Fabricator fabricator;
  private final AmazonProvider amazonProvider;
  private final String segmentFileBucket;

  @Inject
  public ShipImpl(
    @Assisted("basis") Fabricator fabricator,
    AmazonProvider amazonProvider,
    Config config
    /*-*/) {
    this.fabricator = fabricator;
    this.amazonProvider = amazonProvider;

    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public void doWork() throws HubException, CraftException {
    try {
      shipFinalAudio();
      report();

    } catch (HubException e) {
      throw e;
    } catch (Exception e) {
      throw new HubException(
        String.format("Failed to do %s-type ShipDub for segment #%s",
          fabricator.getType(), fabricator.getSegment().getId().toString()), e);
    }
  }


  /**
   Ship the final audio

   @throws Exception on failure
   */
  private void shipFinalAudio() throws Exception {
    amazonProvider.putS3Object(
      fabricator.getOutputFilePath(),
      segmentFileBucket,
      fabricator.getSegment().getWaveformKey());
  }

  /**
   report things
   */
  private void report() {
    // future: send report
  }

}
