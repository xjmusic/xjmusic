// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.external.AmazonProvider;
import io.xj.core.fabricator.Fabricator;
import io.xj.craft.exception.CraftException;
import io.xj.dub.ship.ShipDub;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class ShipDubImpl implements ShipDub {
  //  private final Logger log = LoggerFactory.getLogger(ShipDubImpl.class);
  private final Fabricator fabricator;
  private final AmazonProvider amazonProvider;
  private final String segmentFileBucket;

  @Inject
  public ShipDubImpl(
    @Assisted("basis") Fabricator fabricator,
    AmazonProvider amazonProvider,
    Config config
    /*-*/) {
    this.fabricator = fabricator;
    this.amazonProvider = amazonProvider;

    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public void doWork() throws CoreException, CraftException {
    try {
      shipFinalAudio();
      report();

    } catch (CoreException e) {
      throw e;
    } catch (Exception e) {
      throw new CoreException(
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
