// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 An HTTP Live Streaming Media Segment
 <p>
 SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 <p>
 SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class SegmentAudio {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudio.class);
  private static final int MILLIS_PER_SECOND = 1000;
  private final Segment segment;
  private final String shipKey;
  private final int shipSegmentLoadTimeoutMillis;
  private final Instant endAt;
  private final Instant beginAt;
  private Instant updated;
  private OGGVorbisDecoder ogg;
  private SegmentAudioState state;

  @Inject
  public SegmentAudio(
    @Assisted("segment") Segment segment,
    @Assisted("shipKey") String shipKey,
    Environment env
  ) {
    this.shipKey = shipKey;
    this.segment = segment;
    shipSegmentLoadTimeoutMillis = env.getShipSegmentLoadTimeoutSeconds() * MILLIS_PER_SECOND;
    state = SegmentAudioState.Pending;
    beginAt = Instant.parse(segment.getBeginAt()).minusNanos((long) (segment.getWaveformPreroll() * NANOS_PER_SECOND));
    endAt = Instant.parse(segment.getEndAt()).plusNanos((long) (segment.getWaveformPostroll() * NANOS_PER_SECOND));
  }

  /**
   Get metadata about the original OGG/Vorbis audio

   @return info
   */
  public AudioFormat getAudioFormat() {
    return ogg.getAudioFormat();
  }

  /**
   Whether this segment intersects the specified ship key, from instant, and to instant

   @param shipKey to test for intersection
   @param from    to test for intersection
   @param to      to test for intersection
   @return true if the ship key matches, fromInstant is before the segment end, and toInstant is after the segment beginning
   */
  public boolean intersects(String shipKey, Instant from, Instant to) {
    if (!Objects.equals(shipKey, this.shipKey)) return false;
    return from.isBefore(endAt) && to.isAfter(beginAt);
  }

  /**
   Load OGG/Vorbis audio data into PCM buffer

   @param inputStream from which to load
   */
  public SegmentAudio loadOggVorbis(InputStream inputStream) {
    state = SegmentAudioState.Decoding;
    ogg = OGGVorbisDecoder.decode(inputStream);
    LOG.info("Decoded {} frames from OGG/Vorbis", getTotalPcmFrames());
    state = SegmentAudioState.Ready;

    return this;
  }

  public SegmentAudioState getState() {
    return state;
  }

  public void setState(SegmentAudioState state) {
    this.state = state;
  }

  public UUID getId() {
    return segment.getId();
  }

  public Segment getSegment() {
    return segment;
  }

  /**
   Get the frame for a given instant

   @param of instant
   @return source audio frame for the specified instant
   */
  public int getFrameIndex(Instant of) {
    return (int)
      Math.floor(getAudioFormat().getSampleRate() *
        (Values.toEpochMicros(of) - Values.toEpochMicros(beginAt))
        / MICROS_PER_SECOND);
  }

  /**
   @return the ship key
   */
  public String getShipKey() {
    return shipKey;
  }

  /**
   Get the total number of frames in the pcm buffer

   @return total number of frames
   */
  public int getTotalPcmFrames() {
    return ogg.getPcmData().size();
  }

  /**
   Get the PCM data

   @return PCM data
   */
  public List<double[]> getPcmData() {
    return ogg.getPcmData();
  }

  /**
   Whether this segment audio is loading (within timeout of the given now millis) or ready

   @param nowMillis from which to test
   @return true if loading or ready
   */
  public boolean isLoadingOrReady(Long nowMillis) {
    return switch (state) {
      case Pending, Decoding -> updated.toEpochMilli() < nowMillis - shipSegmentLoadTimeoutMillis;
      case Ready -> true;
      case Failed -> false;
    };
  }

  /**
   Set update time

   @param updated time
   */
  public void setUpdated(Instant updated) {
    this.updated = updated;
  }
}
