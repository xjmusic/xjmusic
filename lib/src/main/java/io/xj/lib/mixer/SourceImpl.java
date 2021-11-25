// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;

/**
 models a single audio source
 stores a series of Samples in Channels across Time, for audio playback.
 <p>
 Dub mixes audio from disk (not memory) to avoid heap overflow #180206211
 */
class SourceImpl implements Source {
  private static final Logger LOG = LoggerFactory.getLogger(SourceImpl.class);
  private final AudioFormat audioFormat;
  private final String absolutePath;
  private final String sourceId;
  private final double lengthSeconds;
  private final double microsPerFrame;
  private final float frameRate;
  private final int channels;
  private final long frameLength;
  private final long lengthMicros;

  @Inject
  public SourceImpl(
    @Assisted("sourceId") String sourceId,
    @Assisted("absolutePath") String absolutePath
  ) throws Exception {
    this.absolutePath = absolutePath;
    this.sourceId = sourceId;

    try (
      var fileInputStream = FileUtils.openInputStream(new File(absolutePath));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      audioFormat = audioInputStream.getFormat();
      channels = audioFormat.getChannels();
      frameRate = audioFormat.getFrameRate();
      frameLength = audioInputStream.getFrameLength();
      lengthSeconds = (frameLength + 0.0) / frameRate;
      lengthMicros = (long) (MICROS_PER_SECOND * lengthSeconds);
      microsPerFrame = MICROS_PER_SECOND / frameRate;
      Values.enforceMaxStereo(channels);
      LOG.debug("Loaded absolutePath: {}, sourceId: {}, audioFormat: {}, channels: {}, frameRate: {}, frameLength: {}, lengthSeconds: {}, lengthMicros: {}, microsPerFrame: {}",
        absolutePath, sourceId, audioFormat, channels, frameRate, frameLength, lengthSeconds, lengthMicros, microsPerFrame);

    } catch (UnsupportedAudioFileException | IOException | ValueException e) {
      throw new MixerException(e);
    }

    LOG.debug("Did load source {}", sourceId);
  }

  @Override
  public String getAbsolutePath() {
    return absolutePath;
  }

  @Override
  public AudioFormat getAudioFormat() {
    return audioFormat;
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
    return audioFormat.getFrameSize() / audioFormat.getChannels();
  }

  @Override
  public String getSourceId() {
    return sourceId;
  }

  @Override
  public String toString() {
    return String.format("id[%s] frames[%d]", sourceId, frameLength);
  }
}

