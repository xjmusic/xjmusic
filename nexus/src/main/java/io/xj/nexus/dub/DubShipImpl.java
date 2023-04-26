// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;


import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentType;
import org.springframework.http.MediaType;

/**
 * [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class DubShipImpl implements DubShip {
  private final Fabricator fabricator;
  private final FileStoreProvider fileStore;
  private final String shipBucket;
  private final int chainJsonMaxAgeSeconds;

  public DubShipImpl(
    AppEnvironment env, FileStoreProvider fileStore, Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
    this.fileStore = fileStore;

    chainJsonMaxAgeSeconds = env.getShipChainJsonMaxAgeSeconds();
    shipBucket = env.getShipBucket();
  }

  @Override
  public void doWork() throws NexusException {
    SegmentType type = null;
    try {
      type = fabricator.getType();
      shipSegmentJson();
      shipSegmentAudio();
      shipChainFullJson();
      shipChainJson();

    } catch (NexusException | FileStoreException e) {
      throw new NexusException(String.format("Failed to do %s-type ShipDub for segment #%s",
        type, fabricator.getSegment().getId()), e);
    }
  }


  /**
   * DubShip the final audio
   */
  private void shipSegmentAudio() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromTempFile(
      fabricator.getFullQualityAudioOutputFilePath(),
      shipBucket,
      fabricator.getSegmentOutputWaveformKey(),
      fabricator.getTemplateConfig().getOutputContentType());
  }

  /**
   * DubShip the final metadata
   */
  private void shipSegmentJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getSegmentJson(),
      shipBucket,
      fabricator.getSegmentJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE, null);
  }

  /**
   * DubShip the final metadata
   */
  private void shipChainFullJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getChainFullJson(),
      shipBucket,
      fabricator.getChainFullJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE,
      chainJsonMaxAgeSeconds);
  }

  /**
   * DubShip the final metadata
   */
  private void shipChainJson() throws NexusException, FileStoreException {
    fileStore.putS3ObjectFromString(
      fabricator.getChainJson(),
      shipBucket,
      fabricator.getChainJsonOutputKey(),
      MediaType.APPLICATION_JSON_VALUE,
      chainJsonMaxAgeSeconds);
  }

}
