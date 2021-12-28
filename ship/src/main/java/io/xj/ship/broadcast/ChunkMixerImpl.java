// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.mixer.AudioSampleFormat;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.CSV;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SegmentAudioState;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.toEpochMicros;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 Side lab experiments done in
 - https://github.com/charneykaye/encode-fmp4-demo
 referenced in the question I posted on Stack Overflow
 - https://stackoverflow.com/questions/69625970/java-mp4parser-to-create-a-single-m4s-fragment
 */
class ChunkMixerImpl implements ChunkMixer {
  public static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final Logger LOG = LoggerFactory.getLogger(ChunkMixerImpl.class);
  private static final int READ_BUFFER_BYTE_SIZE = 4096;
  final double[][] buffer; // final output [frame][channel]
  private final AudioFormat format;
  private final Chunk chunk;
  private final SegmentAudioManager segmentAudioManager;
  private final NotificationProvider notification;

  @Inject
  public ChunkMixerImpl(
    @Assisted("audioFormat") AudioFormat format,
    @Assisted("chunk") Chunk chunk,
    NotificationProvider notification,
    SegmentAudioManager segmentAudioManager
  ) {
    this.chunk = chunk;
    this.format = format;
    this.notification = notification;
    this.segmentAudioManager = segmentAudioManager;

    buffer = new double[(int) (format.getFrameRate() * chunk.getActualDuration())][format.getChannels()];
  }

  @Override
  public boolean isReadyToMix() {
    var audios = getAllIntersectingAudios(chunk);

    if (audios.isEmpty()) {
      LOG.warn("Not ready to mix Chunk[{}] while waiting on segments", chunk.getKey());
      return false;
    }

    var notReady = anyNotReady(audios);
    if (!notReady.isEmpty()) {
      LOG.warn("Not ready to mix Chunk[{}] while waiting on audio from segments {}", chunk.getKey(), CSV.from(notReady));
      return false;
    }

    return true;
  }

  @Override
  public double[][] mix() throws ShipException {
    var audios = getAllIntersectingAudios(chunk);

    if (audios.isEmpty()) {
      LOG.error("Attempted to mix Chunk[{}] while waiting on segments", chunk.getKey());
      notification.publish("Failure", String.format("Attempted to mix Chunk[%s] while waiting on segments", chunk.getKey()));
      return buffer;
    }

    var notReady = anyNotReady(audios);
    if (!notReady.isEmpty()) {
      LOG.error("Attempted to mix Chunk[{}] while waiting on audio from segments {}", chunk.getKey(), CSV.from(notReady));
      notification.publish("Failure", String.format("Attempted to mix Chunk[%s] while waiting on audio from segments %s", chunk.getKey(), CSV.from(notReady)));
      return buffer;
    }

    // get the buffer from each audio and lay it into the output buffer
    LOG.debug("will mix source audio to buffer");
    for (var audio : audios)
      applySource(chunk, audio, buffer);
    LOG.info("Chunk[{}] mixed from audio of {}", chunk.getSequenceNumber(),
      CSV.from(audios.stream().map(SegmentAudio::getSegment).map(Segments::getIdentifier).collect(Collectors.toList())));

    return buffer;
  }

  /**
   apply one source to the mixing buffer@param chunk  to mix

   @param source to apply
   @param buffer to which we will add source
   */
  private void applySource(Chunk chunk, SegmentAudio source, double[][] buffer) throws ShipException {
    int i; // frame iterator
    int b; // bytes iterator
    int tc; // target channel iterator
    int tf, otf = -1; // target output buffer frame, and cache the old value in order to skip non-advanced frames (upsampling), init at -1 to force initial frame
    int ptf; // put at frame iterator (in output)
    double v; // a single sample value, and the enveloped value

    // source frame starts at negative number to compensate for segment preroll
    var sf = (int) -(source.getSegment().getWaveformPreroll() * source.getAudioFormat().getFrameRate());

    // actual microseconds-since-epoch to begin segment audio
    // (not including waveform preroll- we factor that in above, during source audio reading)
    var sam = toEpochMicros(Instant.parse(source.getSegment().getBeginAt()));

    // actual microseconds-since-epoch to begin this output chunk
    var oam = toEpochMicros(chunk.getFromInstant());

    // calculate the starting frame (in output buffer) to align with frame 0 of this source audio
    // compute which frame in output to align with 0 frame of source (this may be negative if source begins before output)
    var frf = (int) Math.floor(format.getSampleRate() * (sam - oam) / MICROS_PER_SECOND);

    // ratio of target frame rate to source frame rate
    // e.g. mixing from 96hz source to 48hz target = 0.5
    var fr = format.getFrameRate() / source.getFrameRate();

    try (
      var fileInputStream = FileUtils.openInputStream(new File(source.getAbsolutePath()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = source.getFrameSize();
      var channels = source.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE <= expectBytes)
        throw new ShipException("loading audio steams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == frameSize) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / source.getFrameSize();
      } else {
        expectFrames = source.getFrameSize();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new ShipException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(source.getAudioFormat());

      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[source.getSampleSizeInBits() / 8];
      byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer; b += frameSize) {
          tf = (int) Math.floor((sf) * fr); // compute the target frame (converted from source rate to target rate)
          for (tc = 0; tc < format.getChannels(); tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            v = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            for (i = otf + 1; i <= tf; i++) { // in case we need to write more than one target frame per source frame (upsampling)
              ptf = frf + i;
              if (ptf < 0 || ptf >= buffer.length) continue;
              buffer[ptf][tc] += v;
            }
          }
          otf = tf;
          sf++;
        }
      }

    } catch (UnsupportedAudioFileException | IOException | FormatException e) {
      throw new ShipException(String.format("Failed to apply Source[%s]", source.getShipKey()), e);
    }
  }

  /**
   Whether all the source segments for this chunk are ready

   @return true if all segments are ready
   */
  private List<String> anyNotReady(Collection<SegmentAudio> audios) {
    List<String> ready = Lists.newArrayList();
    List<String> notReady = Lists.newArrayList();

    for (var audio : audios)
      if (SegmentAudioState.Ready.equals(audio.getState()))
        ready.add(Segments.getIdentifier(audio.getSegment()));
      else
        notReady.add(Segments.getIdentifier(audio.getSegment()));

    return notReady;
  }

  /**
   Get all the segment audios intersecting with this chunk

   @param chunk for which to get intersecting audios
   @return segment audios
   */
  private Collection<SegmentAudio> getAllIntersectingAudios(Chunk chunk) {
    return segmentAudioManager.getAllIntersecting(chunk.getShipKey(), chunk.getFromInstant(), chunk.getToInstant());
  }

}
