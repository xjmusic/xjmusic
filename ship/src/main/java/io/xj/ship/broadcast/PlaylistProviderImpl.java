// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import io.lindstrom.mpd.MPDParser;
import io.lindstrom.mpd.data.*;
import io.lindstrom.mpd.data.descriptor.GenericDescriptor;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistProviderImpl implements PlaylistProvider {
  private static final Logger LOG = LoggerFactory.getLogger(PlaylistProviderImpl.class);
  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  private static final String AUDIO_MIME_TYPE = "audio/mp4";
  private static final String AUDIO_CODECS = "mp4a.40.2";
  private final MPDParser parser;
  private final int shipBitrateHigh;
  private final int shipChunkSeconds;
  private final ChunkManager chunkManager;
  private final SegmentAudioManager segmentAudioManager;
  private final int chunkSeconds;

  @Inject
  public PlaylistProviderImpl(
    ChunkManager chunkManager,
    Environment env,
    SegmentAudioManager segmentAudioManager
  ) {
    this.chunkManager = chunkManager;
    this.segmentAudioManager = segmentAudioManager;
    parser = new MPDParser();
    chunkSeconds = env.getShipChunkSeconds();
    shipBitrateHigh = env.getShipBitrateHigh();
    shipChunkSeconds = env.getShipChunkSeconds();
  }

  @Override
  public String computeMediaPresentationDescriptionXML(String shipKey, String shipTitle, String shipSource, long nowMillis) throws IOException, ShipException {
    var nowSeconds = chunkManager.computeFromSecondUTC(nowMillis);
    var startNumber = nowSeconds / shipChunkSeconds;

    LOG.info("chunks {}",
      chunkManager.getAll(shipKey, nowMillis).stream()
        .map(chunk -> String.format("%s(%s)", chunk.getKey(shipBitrateHigh), chunk.getState()))
        .collect(Collectors.joining(",")));

    var chunks = chunkManager.getContiguousDone(shipKey, nowMillis);
    var chunk = chunks.stream().findFirst().orElseThrow(() -> new ShipException("No chunks!"));

    // use any segment to determine audio metadata
    // NOTE: INCONSISTENCY AMONG SOURCE AUDIO RATES WILL RESULT IN A MALFORMED OUTPUT

    var mpd = MPD.builder()
      .withProfiles(Profiles.builder().withProfiles(List.of(Profile.MPEG_DASH_LIVE)).build())
      .withType(PresentationType.DYNAMIC)
      .withMinBufferTime(Duration.ofSeconds(2))
      .withAvailabilityStartTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()))
      .withProgramInformations(List.of(
        ProgramInformation.builder()
          .withLang("eng")
          .withSource(shipSource)
          .withTitle(shipTitle)
          .build()))
      .withPeriods(List.of(
        Period.builder()
          .withId(String.valueOf(chunk.getSequenceNumber()))
          .withStart(Duration.ofSeconds(nowSeconds))
          .withAdaptationSet(
            AdaptationSet.builder()
              .withId(0)
              .withContentType("audio")
              .withSegmentAlignment("true")
              .withBitstreamSwitching(true)
              .withRepresentations(List.of(

                // FUTURE: multiple available representations, e.g. 320kbps, 240kbps, and 160kbps
                Representation.builder()
                  .withId(Values.k(shipBitrateHigh))
                  .withAudioSamplingRate(String.valueOf(shipBitrateHigh))
                  .withBandwidth(shipBitrateHigh)
                  .withCodecs(AUDIO_CODECS)
                  .withMimeType(AUDIO_MIME_TYPE)
                  .withAudioChannelConfigurations(
                    GenericDescriptor.builder()
                      .withSchemeIdUri("urn:mpeg:dash:23003:3:audio_channel_configuration:2011")
                      .withValue("2")
                      .build())
                  .withSegmentTemplate(
                    SegmentTemplate.builder()
                      .withDuration((long) chunkSeconds * 1000000L)
                      .withInitialization(String.format("%s-$RepresentationID$-IS.mp4", shipKey))
                      .withMedia(String.format("%s-$RepresentationID$-$Number$.m4s", shipKey))
                      .withStartNumber(startNumber)
                      .withTimescale(1000000L)
                      .build()
                  ).build()

              )).build()
          ).build()
      )).build();


    return String.format("%s%s", XML_HEADER, parser.writeAsString(mpd));
  }
}
