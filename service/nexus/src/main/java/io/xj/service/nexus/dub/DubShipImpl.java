// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.Segment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.Fabricator;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class DubShipImpl implements DubShip {
  private final Fabricator fabricator;
  private final FileStoreProvider fileStore;
  private final String segmentFileBucket;

  @Inject
  public DubShipImpl(
    @Assisted("basis") Fabricator fabricator,
    FileStoreProvider fileStore,
    Config config
    /*-*/) {
    this.fabricator = fabricator;
    this.fileStore = fileStore;

    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public void doWork() throws DubException {
    Segment.Type type = null;
    try {
      type = fabricator.getType();
      shipFinalMetadata();
      shipFinalAudio();

    } catch (FabricationException | FileStoreException e) {
      throw new DubException(String.format("Failed to do %s-type ShipDub for segment #%s",
        type, fabricator.getSegment().getId()), e);
    }
  }


  /**
   DubShip the final audio
   */
  private void shipFinalAudio() throws FabricationException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      fabricator.getFullQualityAudioOutputFilePath(),
      segmentFileBucket,
      fabricator.getSegmentOutputWaveformKey());
  }

  /**
   DubShip the final metadata
   */
  private void shipFinalMetadata() throws FabricationException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getResultMetadataJson(),
      segmentFileBucket,
      fabricator.getSegmentOutputMetadataKey());
  }

}
