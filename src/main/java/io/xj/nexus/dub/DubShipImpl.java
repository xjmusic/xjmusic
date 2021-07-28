// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.Segment;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;

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
    Environment env
    /*-*/) {
    this.fabricator = fabricator;
    this.fileStore = fileStore;

    segmentFileBucket = env.getSegmentFileBucket();
  }

  @Override
  public void doWork() throws NexusException {
    Segment.Type type = null;
    try {
      type = fabricator.getType();
      shipSegmentMetadata();
      shipSegmentAudio();
      shipChainFullMetadata();
      shipChainMetadata();

    } catch (NexusException | FileStoreException e) {
      throw new NexusException(String.format("Failed to do %s-type ShipDub for segment #%s",
        type, fabricator.getSegment().getId()), e);
    }
  }


  /**
   DubShip the final audio
   */
  private void shipSegmentAudio() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      fabricator.getFullQualityAudioOutputFilePath(),
      segmentFileBucket,
      fabricator.getSegmentOutputWaveformKey());
  }

  /**
   DubShip the final metadata
   */
  private void shipSegmentMetadata() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getSegmentMetadataJson(),
      segmentFileBucket,
      fabricator.getSegmentOutputMetadataKey(),
      MediaType.APPLICATION_JSONAPI);
  }

  /**
   DubShip the final metadata
   */
  private void shipChainFullMetadata() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getChainMetadataFullJson(),
      segmentFileBucket,
      fabricator.getChainMetadataFullKey(),
      MediaType.APPLICATION_JSONAPI);
  }

  /**
   DubShip the final metadata
   */
  private void shipChainMetadata() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getChainMetadataJson(),
      segmentFileBucket,
      fabricator.getChainMetadataKey(),
      MediaType.APPLICATION_JSONAPI);
  }

}
