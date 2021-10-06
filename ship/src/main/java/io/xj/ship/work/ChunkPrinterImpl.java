// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.mixer.AudioStreamWriter;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.util.CSV;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import io.xj.ship.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public class ChunkPrinterImpl extends ChunkPrinter {
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImpl.class);
  private static final int AUDIO_CHANNELS = 2;
  private static final float QUALITY = 100;
  private static final int BITS_PER_BYTE = 8;
  private static final int SAMPLE_BITS = 16;
  private final Chunk chunk;
  private final ChunkManager chunkManager;
  private final SegmentAudioManager segmentAudioManager;
  private final String threadName;
  private final String tempFilePathPrefix;
  private final int shipChunkSeconds;
  private int channels;
  private int rate;

  @Inject
  public ChunkPrinterImpl(
    @Assisted("chunk") Chunk chunk,
    ChunkManager chunkManager,
    Environment env,
    SegmentAudioManager segmentAudioManager
  ) {
    this.chunk = chunk;
    this.chunkManager = chunkManager;
    this.segmentAudioManager = segmentAudioManager;

    threadName = String.format("CHUNK:%s", chunk.getStreamOutputKey());
    shipChunkSeconds = env.getShipChunkSeconds();
    tempFilePathPrefix = env.getTempFilePathPrefix();
  }

  @Override
  public void compute() {
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    currentThread.setName(threadName);
    try {
      doWork();
    } catch (ShipException e) {
      LOG.error("Failed to print!", e);
    } finally {
      currentThread.setName(oldName);
    }
  }

  /**
   * Do the work inside a named thread
   */
  private void doWork() throws ShipException {
    switch (chunk.getState()) {
      case Pending -> {
        if (isSourceAudioReady()) mixAndOutput();
      }
      case Mixing, Shipping -> {/* no op */}
    }
  }

  /**
   * Whether all the source segments for this chunk are ready
   *
   * @return true if all segments are ready
   */
  private boolean isSourceAudioReady() {
    List<String> ready = Lists.newArrayList();
    List<String> notReady = Lists.newArrayList();
    var audios = getAllIntersectingAudios();

    if (audios.isEmpty()) {
      LOG.debug("waiting on segments");
      return false;
    }

    for (var audio : audios)
      if (SegmentAudioState.Ready.equals(audio.getState()))
        ready.add(Segments.getIdentifier(audio.getSegment()));
      else
        notReady.add(Segments.getIdentifier(audio.getSegment()));

    if (notReady.isEmpty() && Objects.equals(ready.size(), audios.size())) {
      LOG.info("will mix audio from segments {}", CSV.from(ready));
      return true;
    }

    LOG.warn("waiting on audio from segments {}", CSV.from(notReady));
    return false;
  }

  /**
   * Mix and output the audio
   *
   * @throws ShipException on failure
   */
  private void mixAndOutput() throws ShipException {
    var audios = getAllIntersectingAudios();

    // fail if any of the source audios are not ready
    var notReady = audios.stream()
      .filter(audio -> !SegmentAudioState.Ready.equals(audio.getState()))
      .map(SegmentAudio::getId)
      .collect(Collectors.toList());
    if (!notReady.isEmpty())
      throw new ShipException(String.format("Segment%s[%s] %s not actually ready!",
        1 < notReady.size() ? "s" : "",
        CSV.from(notReady),
        1 < notReady.size() ? "are" : "is"));

    // use any segment to determine audio metadata
    // NOTE: INCONSISTENCY AMONG SOURCE AUDIO RATES WILL RESULT IN A MALFORMED OUTPUT
    var ref = audios.stream().findAny()
      .orElseThrow(() -> new ShipException("No Segment Audio found!"))
      .getInfo();
    rate = ref.rate;
    channels = ref.channels;
    double[][] output = new double[ref.rate * shipChunkSeconds][AUDIO_CHANNELS];

    // get the buffer from each audio and lay it into the output buffer
    LOG.debug("ready; will mix");
    chunkManager.put(chunk.setState(ChunkState.Mixing));

    for (var audio : audios) {
      var initialSourceFrame = audio.getFrame(chunk.getFromInstant());
      for (int f = 0; f < rate * shipChunkSeconds; f++)
        for (var c = 0; c < channels; c++) {
          var dick = read(initialSourceFrame + f, c, audio);
          output[f][c] += dick;
        }
    }

    LOG.debug("mixed; will write");
    try {
      String outputFilePath = String.format("%s%s.wav", tempFilePathPrefix, chunk.getStreamOutputKey());
      var writer = new AudioStreamWriter(output, QUALITY);
      writer.writeToFile(outputFilePath, computeAudioFormat(audios), OutputEncoder.WAV, output.length);
      LOG.info("did write {}", outputFilePath);
    } catch (Exception e) {
      LOG.error("Failed to write audio", e);
      return;
    }

    LOG.debug("mixed; will encode");
    chunkManager.put(chunk.setState(ChunkState.Encoding));
    // NEXT use ffmpeg to encode the shippable audio

    chunkManager.put(chunk.setState(ChunkState.Shipping));
    // NEXT ship audio to stream.xj.io

    chunkManager.put(chunk.setState(ChunkState.Done));
  }

  /**
   * Compute the audio format from the given set of audios
   *
   * @param audios from which to compute format
   * @return audio format
   */
  private AudioFormat computeAudioFormat(Collection<SegmentAudio> audios) {
    var audio = audios.stream().findAny().orElseThrow();
    return new AudioFormat(
      AudioFormat.Encoding.PCM_SIGNED,
      audio.getInfo().rate,
      SAMPLE_BITS,
      audio.getInfo().channels,
      audio.getInfo().channels * SAMPLE_BITS / BITS_PER_BYTE,
      audio.getInfo().rate,
      false
    );
  }

  /**
   * Add one frame of a segment's source audio buffer onto the output buffer
   *
   * @param sourceFrame from which to get source
   * @param audio       source
   */
  private double read(int sourceFrame, int channel, SegmentAudio audio) {
    if (sourceFrame >= 0 && sourceFrame <= audio.getTotalPcmFrames())
      return audio.getPcmData()[channel][sourceFrame];
    else
      return 0;
  }

  /**
   * Get all the segment audios intersecting with this chunk
   *
   * @return segment audios
   */
  private Collection<SegmentAudio> getAllIntersectingAudios() {
    return segmentAudioManager.getAllIntersecting(chunk.getShipKey(), chunk.getFromInstant(), chunk.getToInstant());
  }
}
