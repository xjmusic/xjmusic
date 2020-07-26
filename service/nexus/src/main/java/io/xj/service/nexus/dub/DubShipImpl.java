// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.Fabricator;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class DubShipImpl implements DubShip {
  //  private final Logger log = LoggerFactory.getLogger(ShipDubImpl.class);
  private final Fabricator fabricator;
  private final FileStoreProvider fileStoreProvider;
  private final String segmentFileBucket;

  @Inject
  public DubShipImpl(
    @Assisted("basis") Fabricator fabricator,
    FileStoreProvider fileStoreProvider,
    Config config
    /*-*/) {
    this.fabricator = fabricator;
    this.fileStoreProvider = fileStoreProvider;

    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public void doWork() throws DubException {
    SegmentType type = null;
    try {
      type = fabricator.getType();
      shipFinalMetadata();
      shipFinalAudio();
      report();

    } catch (FabricationException | FileStoreException e) {
      throw new DubException(String.format("Failed to do %s-type ShipDub for segment #%s",
        type, fabricator.getSegment().getId().toString()), e);
    }
  }


  /**
   DubShip the final audio
   */
  private void shipFinalAudio() throws FabricationException, FileStoreException {
    fileStoreProvider.putS3ObjectFromTempFile(
      fabricator.getFullQualityAudioOutputFilePath(),
      segmentFileBucket,
      fabricator.getSegment().getOutputWaveformKey());
  }

  /**
   DubShip the final metadata
   */
  private void shipFinalMetadata() throws FabricationException, FileStoreException {
    fileStoreProvider.putS3ObjectFromString(
      fabricator.getResultMetadata(),
      segmentFileBucket,
      fabricator.getSegment().getOutputMetadataKey());
  }

  /**
   report things
   */
  private void report() {
    // future: send report
  }

}
