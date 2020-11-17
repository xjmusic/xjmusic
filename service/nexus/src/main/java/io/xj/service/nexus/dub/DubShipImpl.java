// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.Segment;
import io.xj.SegmentMessage;
import io.xj.lib.entity.MessageType;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.pubsub.PubSubProvider;
import io.xj.lib.util.Text;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public class DubShipImpl implements DubShip {
  private final Logger log = LoggerFactory.getLogger(DubShipImpl.class);
  private final Fabricator fabricator;
  private final FileStoreProvider fileStore;
  private final String segmentFileBucket;
  private final PubSubProvider pubSub;

  @Inject
  public DubShipImpl(
    @Assisted("basis") Fabricator fabricator,
    FileStoreProvider fileStore,
    PubSubProvider pubSub,
    Config config
    /*-*/) {
    this.fabricator = fabricator;
    this.fileStore = fileStore;
    this.pubSub = pubSub;

    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public void doWork() throws DubException {
    Segment.Type type = null;
    try {
      type = fabricator.getType();
      shipFinalMetadata();
      shipFinalAudio();
      publishWarningMessages();

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

  /**
   report things
   */
  private void publishWarningMessages() {
    try {
      Collection<SegmentMessage> messages = fabricator.getSegmentMessages();
      if (0 < messages.size()) {
        MessageType mostSevereType = MessageType.mostSevereType(messages);
        if (MessageType.isMoreSevere(mostSevereType, MessageType.Info))
          pubSub.publish(
            String.format("There were messages!\n\n%s", Text.formatMultiline(
              messages.stream()
                .map(segmentMessage -> String.format("[%s] %s\n\n", segmentMessage.getType(), segmentMessage.getBody()))
                .toArray()
            )),
            mostSevereType.toString()
          );
      }

    } catch (FabricationException e) {
      log.error("Failed to publish report", e);
    }
  }

}
