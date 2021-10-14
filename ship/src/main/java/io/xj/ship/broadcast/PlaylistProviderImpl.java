package io.xj.ship.broadcast;

import com.google.inject.Inject;
import io.lindstrom.mpd.MPDParser;
import io.lindstrom.mpd.data.*;
import io.lindstrom.mpd.data.descriptor.Descriptor;
import io.xj.lib.app.Environment;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistProviderImpl implements PlaylistProvider {
  private static final Logger LOG = LoggerFactory.getLogger(PlaylistProviderImpl.class);
  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  private static final String AUDIO_MIME_TYPE = "audio/mp4";
  private static final String AUDIO_CODECS = "mp4a.40.2";
  private static final String REPRESENTATION_BITRATE_UNIT = "kbps";
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
  public String computeMpdXML(String shipKey, String shipTitle, String shipSource, long nowMillis) throws IOException, ShipException {
    var nowSeconds = chunkManager.computeFromSecondUTC(nowMillis);
    var startNumber = nowSeconds / shipChunkSeconds;

    LOG.info("chunks {}",
      chunkManager.getAll(shipKey, nowMillis).stream()
        .map(chunk -> String.format("%s(%s)", chunk.getKey(shipBitrateHigh), chunk.getState()))
        .collect(Collectors.joining(",")));

    var chunks = chunkManager.getContiguousDone(shipKey, nowMillis);
    var chunk = chunks.stream().findFirst().orElseThrow(() -> new ShipException("No chunks!"));

    var audios = segmentAudioManager.getAllIntersecting(chunk.getShipKey(), chunk.getFromInstant(), chunk.getToInstant());

    // use any segment to determine audio metadata
    // NOTE: INCONSISTENCY AMONG SOURCE AUDIO RATES WILL RESULT IN A MALFORMED OUTPUT
    var ref = audios.stream().findAny()
      .orElseThrow(() -> new ShipException("No Segment Audio found!"))
      .getAudioFormat();

    var mpd = MPD.builder()
      .withProfiles(Profiles.builder().withProfiles(List.of(Profile.MPEG_DASH_LIVE)).build())
      .withType(PresentationType.DYNAMIC)
      .withMinBufferTime(Duration.ofSeconds(2))
      .withProgramInformations(List.of(
        ProgramInformation.builder()
          .withSource(shipSource)
          .withTitle(shipTitle)
          .build()))
      .withPeriods(List.of(
        Period.builder()
          .withStart(Duration.ZERO)
          .withAdaptationSet(
            AdaptationSet.builder()
              .withAudioSamplingRate(String.valueOf(ref.getSampleRate()))
              .withCodecs(AUDIO_CODECS)
              .withId(3)
              .withLang("eng")
              .withMimeType(AUDIO_MIME_TYPE)
              .withSegmentAlignment("true")
              .withStartWithSAP(2L)
              .withAudioChannelConfigurations(new Descriptor("urn:mpeg:dash:23003:3:audio_channel_configuration:2011", "2") {
                @Override
                public String getValue() {
                  return "2";
                }
              })
              .withBaseURLs(List.of(BaseURL.builder().withValue("").build()))
              .withSegmentTemplate(
                SegmentTemplate.builder()
                  .withDuration((long) chunkSeconds)
                  .withInitialization(String.format("%s-$RepresentationID$-IS.mp4", shipKey))
                  .withMedia(String.format("%s-$RepresentationID$-$Number$.m4s", shipKey))
                  .withStartNumber(startNumber)
                  .withTimescale(1L)
                  .build())
              .withRepresentations(List.of(
                computeRepresentation(shipBitrateHigh)

// FUTURE: other bitrates                  <Representation id="160kbps" bandwidth="160000" />
// FUTURE: other bitrates                <Representation id="96kbps" bandwidth="96000" />
// FUTURE: other bitrates              <Representation id="128kbps" bandwidth="128000" />

              ))
              .build())
          .build()
      ))
      .build();
    return String.format("%s%s", XML_HEADER, parser.writeAsString(mpd));
  }

  /**
   * Compute a representation for the given bitrate
   *
   * @param bitrate for which to compute representation
   * @return representation
   */
  private Representation computeRepresentation(int bitrate) {
    return Representation.builder()
      .withId(String.format("%d%s", (int) Math.floor(bitrate / (double) 1000), REPRESENTATION_BITRATE_UNIT))
      .withBandwidth(bitrate)
      .build();
  }
}
