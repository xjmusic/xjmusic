// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.mixer.AudioFileWriter;
import io.xj.nexus.model.Segment;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.StreamPlayer;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.MILLIS_PER_SECOND;

public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private static final String TIMER_SECTION_SHIP = "Ship";
  private final long startedAtMillis;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final Telemetry telemetry;
  private final DubWork dubWork;
  private final OutputFileMode outputFileMode;
  private final OutputMode outputMode;
  private final String outputPathPrefix;
  private final int outputFileNumberDigits;
  private final int outputSeconds;
  private final int pcmChunkSizeBytes;
  private final String shipKey;

  private int outputFileNum = 0;
  private long shippedToChainMicros = 0;
  private float progress;

  @Nullable
  AudioFileWriter fileWriter;

  @Nullable
  OutputFile outputFile = null;

  @Nullable
  StreamPlayer playback;

  public ShipWorkImpl(
    Telemetry telemetry,
    DubWork dubWork,
    BroadcastFactory broadcastFactory,
    OutputMode outputMode,
    OutputFileMode outputFileMode,
    int outputSeconds,
    String shipKey,
    String outputPathPrefix,
    int outputFileNumberDigits,
    int pcmChunkSizeBytes
  ) {
    this.telemetry = telemetry;
    this.dubWork = dubWork;
    this.outputFileMode = outputFileMode;
    this.shipKey = shipKey;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.outputMode = outputMode;
    this.outputPathPrefix = outputPathPrefix;
    this.outputSeconds = outputSeconds;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;

    var audioFormat = dubWork.getAudioFormat().orElseThrow(() ->
      new RuntimeException("Unable to get audio format from dub work"));

    switch (outputMode) {
      case PLAYBACK -> {
        LOG.info("Will initialize playback output");
        playback = broadcastFactory.player(
          audioFormat,
          (int) (dubWork.getMixerLengthSeconds() * audioFormat.getFrameRate() * audioFormat.getFrameSize())
        );
      }
      case FILE -> {
        LOG.info("Will initialize file output");
        fileWriter = broadcastFactory.writer(audioFormat);
      }
    }

    if (0 < outputSeconds) {
      LOG.info("Will start in {} output mode and run {} seconds", outputMode, outputSeconds);
    } else {
      LOG.info("Will start in {} output mode and run indefinitely", outputMode);
    }

    startedAtMillis = System.currentTimeMillis();
    running.set(true);
  }

  @Override
  public void runCycle() {
    if (!running.get()) return;

    if (dubWork.isFinished()) {
      LOG.warn("DubWork not running, will stop");
      finish();
    }

    // Action based on mode
    try {
      if (dubWork.getMixerBuffer().isEmpty()) return;
      long startedAtMillis = System.currentTimeMillis();
      switch (outputMode) {
        case PLAYBACK -> doShipOutputPlayback();
        case FILE -> doShipOutputFile();
      }
      telemetry.record(TIMER_SECTION_SHIP, System.currentTimeMillis() - startedAtMillis);

    } catch (Exception e) {
      didFailWhile("running ship work", e);
    }
  }

  @Override
  public void finish() {
    if (!running.get()) return;
    running.set(false);
    if (Objects.nonNull(playback)) {
      playback.finish();
    }
    if (Objects.nonNull(fileWriter)) {
      try {
        fileWriter.finish();
      } catch (IOException e) {
        LOG.error("Failed to finish file writer", e);
      }
    }
    dubWork.finish();
    LOG.info("Finished");
  }

  @Override
  public boolean isFinished() {
    return !running.get();
  }

  @Override
  public float getProgress() {
    return progress;
  }

  @Override
  public Optional<Long> getShippedToChainMicros() {
    return switch (outputMode) {
      case PLAYBACK -> Objects.nonNull(playback) ? Optional.of(playback.getHeardAtChainMicros()) : Optional.empty();
      case FILE -> Optional.of(shippedToChainMicros);
    };
  }

  /**
   Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputPlayback() throws IOException {
    assert Objects.nonNull(playback);
    var availableBytes = dubWork.getMixerBuffer().orElseThrow().getAvailableByteCount();
    playback.write(dubWork.getMixerBuffer().orElseThrow().consume(availableBytes));
    shippedToChainMicros = (long) (shippedToChainMicros + availableBytes * dubWork.getMixerOutputMicrosPerByte());
  }

  /**
   Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputFile() throws IOException {
    if (Objects.isNull(fileWriter)) {
      LOG.debug("File writer is null, won't ship output file");
      return;
    }
    Optional<Segment> segment;
    segment = dubWork.getSegmentAtChainMicros(shippedToChainMicros); // first, get the segment exactly at the play head
    if (segment.isEmpty()) {
      LOG.debug("No segment available at chain micros {}", shippedToChainMicros);
      return;
    }

    if (Objects.isNull(outputFile)) { // First output file
      doShipOutputFileStartNext(segment.get());

    } else { // Continue output file in progress, start next if needed, finish if done
      if (outputFile.getToChainMicros() - shippedToChainMicros < pcmChunkSizeBytes * dubWork.getMixerOutputMicrosPerByte()) { // check to see if we are at risk of getting stuck in this last pcm chunk of the segment-- this would never advance to the next segment/output file
        LOG.debug("Not enough space in current output file, will advance to next segment");
        var nextOffset = segment.get().getId() + 1;
        segment = dubWork.getSegmentAtOffset(nextOffset);
        if (segment.isEmpty()) {
          LOG.debug("No segment available at chain offset {}", nextOffset);
          return;
        }
      }
      if (!outputFile.contains(segment.get())) {
        switch (outputFileMode) {
          case CONTINUOUS -> outputFile.add(segment.get());
          case SEGMENT -> doShipOutputFileStartNext(segment.get());
          case MAIN -> {
            var currentMainProgram = dubWork.getMainProgram(outputFile.getLastSegment());
            var nextMainProgram = dubWork.getMainProgram(segment.get());
            if (currentMainProgram.isEmpty() || nextMainProgram.isEmpty()) {
              LOG.debug("Not fully ready, waiting for current and/or next main program");
              return;
            }
            if (!currentMainProgram.get().getId().equals(nextMainProgram.get().getId())) {
              doShipOutputFileStartNext(segment.get());
            } else {
              outputFile.add(segment.get());
            }
          }
          case MACRO -> {
            var currentMacroProgram = dubWork.getMacroProgram(outputFile.getLastSegment());
            var nextMacroProgram = dubWork.getMacroProgram(segment.get());
            if (currentMacroProgram.isEmpty() || nextMacroProgram.isEmpty()) {
              LOG.debug("Not fully ready, waiting for current and/or next macro program");
              return;
            }
            if (!currentMacroProgram.get().getId().equals(nextMacroProgram.get().getId())) {
              doShipOutputFileStartNext(segment.get());
            } else {
              outputFile.add(segment.get());
            }
          }
        }
      }

      int availableBytes = dubWork.getMixerBuffer().orElseThrow().getAvailableByteCount();
      int currentFileMaxBytes = (int) ((outputFile.getToChainMicros() - shippedToChainMicros) / dubWork.getMixerOutputMicrosPerByte());
      int shipBytes = (int) (pcmChunkSizeBytes * Math.floor((float) Math.min(availableBytes, currentFileMaxBytes) / pcmChunkSizeBytes)); // rounded down to a multiple of pcm chunk size bytes
      if (0 == shipBytes) {
        LOG.debug("Will not ship any bytes");
        return;
      }
      LOG.debug("Will ship {} bytes to local files", shipBytes);
      fileWriter.append(dubWork.getMixerBuffer().orElseThrow().consume(shipBytes));
      shippedToChainMicros = shippedToChainMicros + (long) (shipBytes * dubWork.getMixerOutputMicrosPerByte());
      if (shippedEnoughSeconds()) {
        if (doShipOutputFileClose()) {
          outputFileNum++;
        }
      }
    }
  }

  void doShipOutputFileStartNext(Segment firstSegment) {
    if (doShipOutputFileClose()) {
      outputFileNum++;
    }
    outputFile = new OutputFile(firstSegment);

    try {
      Objects.requireNonNull(fileWriter).open(outputFile.getPath());
    } catch (IOException e) {
      didFailWhile("opening file writer", e);
      return;
    }

    LOG.info("Starting next output file {}", outputFile.getPath());
  }

  boolean doShipOutputFileClose() {
    if (Objects.requireNonNull(fileWriter).isWriting()) {
      try {
        return fileWriter.finish();
      } catch (IOException e) {
        didFailWhile("closing file writer", e);
      }
    }
    return false;
  }

  /**
   Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    LOG.error("Failed while {} because {}", msgWhile, msgCause, e);

    finish();
  }

  /**
   If a finite number of seconds was specified, check if we have shipped that many seconds
   If we are shipped past the target seconds, exit
   */
  boolean shippedEnoughSeconds() {
    var shippedSeconds = (float) shippedToChainMicros / MICROS_PER_SECOND;
    if (0 == outputSeconds) {
      LOG.debug("Shipped {} seconds", String.format("%.1f", shippedSeconds));
      return false;
    }

    // Finite number-zero number of output seconds has been specified
    progress = shippedSeconds / outputSeconds;
    LOG.debug("Shipped {} seconds ({})", String.format("%.2f", shippedSeconds), StringUtils.percentage(progress));

    // But leave if we have not yet shipped that many seconds
    if (shippedSeconds < outputSeconds) return false;

    // We're done! log performance info, close the wav container, and finish
    double totalSeconds = (double) (System.currentTimeMillis() - startedAtMillis) / MILLIS_PER_SECOND;
    var realtimeRatio = shippedSeconds / totalSeconds;
    LOG.info("Overall performance at {} real-time", String.format("%.1fx", realtimeRatio));
    finish();
    return true;
  }

  /**
   Output File
   */
  class OutputFile {

    final List<Segment> segments;
    long toChainMicros;

    OutputFile(
      Segment firstSegment
    ) {
      this.segments = new ArrayList<>();
      segments.add(firstSegment);
      this.toChainMicros = firstSegment.getBeginAtChainMicros() + Objects.requireNonNull(firstSegment.getDurationMicros());
    }

    void add(Segment segment) {
      segments.add(segment);
      toChainMicros = segment.getBeginAtChainMicros() + Objects.requireNonNull(segment.getDurationMicros());
    }

    public long getToChainMicros() {
      return toChainMicros;
    }

    public boolean contains(Segment segment) {
      return segments.stream().anyMatch(s -> s.getId().equals(segment.getId()));
    }

    public Segment getLastSegment() {
      return segments.get(segments.size() - 1);
    }

    public String getPath() {
      return outputPathPrefix +
        shipKey +
        "-" + StringUtils.zeroPadded(outputFileNum, outputFileNumberDigits) +
        getPathDescriptionIfRelevant() +
        ".wav";
    }

    String getPathDescriptionIfRelevant() {
      switch (outputFileMode) {
        default -> {
          return "";
        }
        case MAIN -> {
          Optional<Program> mainProgram = dubWork.getMainProgram(getLastSegment());
          return mainProgram.map(program -> "-" + StringUtils.toLowerHyphenatedSlug(program.getName())).orElse("");
        }
        case MACRO -> {
          Optional<Program> macroProgram = dubWork.getMacroProgram(getLastSegment());
          return macroProgram.map(program -> "-" + StringUtils.toLowerHyphenatedSlug(program.getName())).orElse("");
        }
      }
    }
  }
}
