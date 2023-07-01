// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.FilePathProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

/**
 * [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class DubUploadImpl implements DubUpload {
  private final FilePathProvider filePathProvider;
  private final Fabricator fabricator;
  private final FileStoreProvider fileStore;
  private final String shipBucket;
  private final int chainJsonMaxAgeSeconds;
  private final boolean isLocalModeEnabled;


  public DubUploadImpl(
    FilePathProvider filePathProvider,
    FileStoreProvider fileStore,
    Fabricator fabricator,
    @Value("${ship.bucket}")
    String shipBucket,
    @Value("${ship.chain.json.max-age-seconds}")
    int chainJsonMaxAgeSeconds,
    @Value("${yard.local-mode.enabled}") boolean isLocalModeEnabled
  ) {
    this.filePathProvider = filePathProvider;
    this.fabricator = fabricator;
    this.fileStore = fileStore;
    this.shipBucket = shipBucket;
    this.chainJsonMaxAgeSeconds = chainJsonMaxAgeSeconds;
    this.isLocalModeEnabled = isLocalModeEnabled;
  }

  @Override
  public void doWork() throws NexusException {
    SegmentType type = null;
    try {
      type = fabricator.getType();
      shipSegmentJson();
      shipChainFullJson();
      shipChainJson();
      if (!isLocalModeEnabled) {
        shipSegmentAudio();
      }

    } catch (NexusException | FileStoreException e) {
      throw new NexusException(String.format("Failed to do %s-type ShipDub for segment #%s",
        type, fabricator.getSegment().getId()), e);
    }
  }


  /**
   * DubUpload the final audio
   */
  private void shipSegmentAudio() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      filePathProvider.computeFullQualityAudioOutputFilePath(fabricator.getSegment()),
      shipBucket,
      fabricator.getSegmentOutputWaveformKey(),
      fabricator.getTemplateConfig().getOutputContentType(),
      null);
  }

  /**
   * DubUpload the final metadata
   */
  private void shipSegmentJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      filePathProvider.computeSegmentJsonOutputFilePath(fabricator.getSegment()),
      shipBucket,
      fabricator.getSegmentJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE,
      null);
  }

  /**
   * DubUpload the final metadata
   */
  private void shipChainFullJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      filePathProvider.computeChainFullJsonOutputFilePath(fabricator.getChain()),
      shipBucket,
      fabricator.getChainFullJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE,
      chainJsonMaxAgeSeconds);
  }

  /**
   * DubUpload the final metadata
   */
  private void shipChainJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      filePathProvider.computeChainJsonOutputFilePath(fabricator.getChain()),
      shipBucket,
      fabricator.getChainJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE,
      chainJsonMaxAgeSeconds);
  }

}
