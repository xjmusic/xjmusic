// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;


import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.jetbrains.annotations.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.lib.util.ValueUtils.MICROS_PER_SECOND;

/**
 * models a single audio source
 * stores a series of Samples in Channels across Time, for audio playback.
 * <p>
 * Dub mixes audio from disk (not memory) to avoid heap overflow https://www.pivotaltracker.com/story/show/180206211
 * <p>
 * Fabrication should not completely fail because of one bad source audio https://www.pivotaltracker.com/story/show/182575665
 */
class SourceImpl implements Source {
  static final Logger LOG = LoggerFactory.getLogger(SourceImpl.class);
  final @Nullable
  AudioFormat audioFormat;
  final String absolutePath;
  final UUID audioId;
  final double lengthSeconds;
  final double microsPerFrame;
  final float frameRate;
  final int channels;
  final long frameLength;
  final long lengthMicros;

  public SourceImpl(
    NotificationProvider notification,
    UUID audioId,
    String absolutePath,
    String description,
    @Value("${environment}")
    String environment
  ) {
    double _microsPerFrame;
    long _lengthMicros;
    double _lengthSeconds;
    long _frameLength;
    float _frameRate;
    int _channels;
    AudioFormat _audioFormat;
    this.absolutePath = absolutePath;
    this.audioId = audioId;
    String envName = StringUtils.toProper(environment);

    try (
      var fileInputStream = FileUtils.openInputStream(new File(absolutePath));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      _audioFormat = audioInputStream.getFormat();
      _channels = _audioFormat.getChannels();
      _frameRate = _audioFormat.getFrameRate();
      _frameLength = audioInputStream.getFrameLength();
      _lengthSeconds = (_frameLength + 0.0) / _frameRate;
      _lengthMicros = (long) (MICROS_PER_SECOND * _lengthSeconds);
      _microsPerFrame = MICROS_PER_SECOND / _frameRate;
      ValueUtils.enforceMaxStereo(_channels);
      LOG.debug("Loaded absolutePath: {}, sourceId: {}, audioFormat: {}, channels: {}, frameRate: {}, frameLength: {}, lengthSeconds: {}, lengthMicros: {}, microsPerFrame: {}",
        absolutePath, audioId, _audioFormat, _channels, _frameRate, _frameLength, _lengthSeconds, _lengthMicros, _microsPerFrame);

    } catch (UnsupportedAudioFileException | IOException | ValueException e) {
      LOG.error("Failed to load source for Audio[{}] \"{}\" because {}", audioId, description, e.getMessage());
      notification.publish(String.format("%s-Chain Mix Source Failure", envName), String.format("Failed to load source for Audio[%s] \"%s\" because %s", audioId, description, e.getMessage()));
      _audioFormat = null;
      _channels = 0;
      _frameRate = 0;
      _frameLength = 0;
      _lengthSeconds = 0;
      _lengthMicros = 0;
      _microsPerFrame = 0;
    }

    microsPerFrame = _microsPerFrame;
    lengthMicros = _lengthMicros;
    lengthSeconds = _lengthSeconds;
    frameLength = _frameLength;
    frameRate = _frameRate;
    channels = _channels;
    audioFormat = _audioFormat;
    LOG.debug("Did load source {}", audioId);
  }

  @Override
  public String getAbsolutePath() {
    return absolutePath;
  }

  @Override
  public Optional<AudioFormat> getAudioFormat() {
    return Optional.ofNullable(audioFormat);
  }

  @Override
  public int getChannels() {
    return channels;
  }

  @Override
  public long getFrameLength() {
    return frameLength;
  }

  @Override
  public float getFrameRate() {
    return frameRate;
  }

  @Override
  public long getLengthMicros() {
    return lengthMicros;
  }

  @Override
  public double getLengthSeconds() {
    return lengthSeconds;
  }

  @Override
  public double getMicrosPerFrame() {
    return microsPerFrame;
  }

  @Override
  public int getSampleSize() {
    return Objects.isNull(audioFormat) ? 0 : audioFormat.getFrameSize() / audioFormat.getChannels();
  }

  @Override
  public UUID getAudioId() {
    return audioId;
  }

  @Override
  public String toString() {
    return String.format("id[%s] frames[%d]", audioId, frameLength);
  }
}

