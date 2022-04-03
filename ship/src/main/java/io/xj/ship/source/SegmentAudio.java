// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.lib.app.Environment;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.ValueException;
import io.xj.ship.ShipException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;
import static io.xj.lib.util.Values.enforceMaxStereo;

/**
 An HTTP Live Streaming Media Segment
 <p>
 SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 <p>
 SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 <p>
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public class SegmentAudio {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudio.class);
  private static final int MILLIS_PER_SECOND = 1000;
  private final Segment segment;
  private final String absolutePath;
  private final String shipKey;
  private final int shipSegmentLoadTimeoutMillis;
  private final Instant endAt;
  private final Instant beginAt;
  private final AudioFormat audioFormat;
  private final int channels;
  private final float frameRate;
  private final int frameSize;
  private final int sampleSizeInBits;
  private Instant updated;
  private SegmentAudioState state;

  @Inject
  public SegmentAudio(
    @Assisted("absolutePath") String absolutePath,
    @Assisted("segment") Segment segment,
    @Assisted("shipKey") String shipKey,
    TelemetryProvider telemetryProvider,
    Environment env
  ) throws ShipException {
    this.absolutePath = absolutePath;
    this.shipKey = shipKey;
    this.segment = segment;
    shipSegmentLoadTimeoutMillis = env.getShipSegmentLoadTimeoutSeconds() * MILLIS_PER_SECOND;
    state = SegmentAudioState.Pending;
    beginAt = Instant.parse(segment.getBeginAt()).minusNanos((long) (segment.getWaveformPreroll() * NANOS_PER_SECOND));
    endAt = Instant.parse(segment.getEndAt()).plusNanos((long) (segment.getWaveformPostroll() * NANOS_PER_SECOND));

    var SEGMENT_AUDIO_LOADED_SECONDS = telemetryProvider.count("segment_audio_loaded_seconds", "Segment Audio Loaded Seconds", "s");

    try (
      var fileInputStream = FileUtils.openInputStream(new File(absolutePath));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      audioFormat = audioInputStream.getFormat();
      channels = audioFormat.getChannels();
      frameRate = audioFormat.getFrameRate();
      frameSize = audioFormat.getFrameSize();
      long frameLength = audioInputStream.getFrameLength();
      sampleSizeInBits = audioFormat.getSampleSizeInBits();
      double lengthSeconds = (frameLength + 0.0) / frameRate;
      enforceMaxStereo(channels);
      state = SegmentAudioState.Ready;
      LOG.debug("Loaded absolutePath: {}, sourceId: {}, audioFormat: {}, channels: {}, frameRate: {}, frameLength: {}, lengthSeconds: {}",
        absolutePath, shipKey, audioFormat, channels, frameRate, frameLength, lengthSeconds);
      telemetryProvider.put(SEGMENT_AUDIO_LOADED_SECONDS, (long) lengthSeconds);

    } catch (UnsupportedAudioFileException | IOException | ValueException e) {
      throw new ShipException(String.format("Failed to read audio from disk %s", absolutePath), e);
    }
  }

  /**
   @return channels of segment audio
   */
  public int getChannels() {
    return channels;
  }

  /**
   @return frameRate of segment audio
   */
  public float getFrameRate() {
    return frameRate;
  }

  /**
   @return frame size (in bytes) of segment audio
   */
  public int getFrameSize() {
    return frameSize;
  }

  /**
   Get metadata about the original OGG/Vorbis audio

   @return info
   */
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }

  /**
   @return sample size (in bits) of segment audio
   */
  public int getSampleSizeInBits() {
    return sampleSizeInBits;
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
   @return the ship key
   */
  public String getShipKey() {
    return shipKey;
  }

  /**
   Get the total number of frames in the pcm buffer
   <p>
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

  /**
   @return absolut path to uncompressed WAV segment audio on disk
   */
  public String getAbsolutePath() {
    return absolutePath;
  }
}
