// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.lib.mixer.AudioFileWriter;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.Segment;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.StreamPlayer;
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

public class ShipWorkImpl implements ShipWork {
  static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);

  @Nullable
  AudioFileWriter fileWriter;
  @Nullable
  OutputFile outputFile = null;
  @Nullable
  StreamPlayer playback;
  final MultiStopwatch timer;
  final AtomicBoolean running = new AtomicBoolean(true);
  final BroadcastFactory broadcastFactory;
  final DubWork dubWork;
  final OutputFileMode outputFileMode;
  final OutputMode outputMode;
  final String outputPathPrefix;
  final int cycleAudioBytes;
  final int outputFileNumberDigits;
  final int outputSeconds;
  final int pcmChunkSizeBytes;
  final int shipAheadSeconds;
  final long cycleMillis;
  int outputFileNum = 0;
  long targetChainMicros = 0;
  long nextCycleAtSystemMillis = System.currentTimeMillis();
  private final String shipKey;
  private float progress;

  public ShipWorkImpl(
    DubWork dubWork,
    BroadcastFactory broadcastFactory,
    OutputMode outputMode,
    OutputFileMode outputFileMode,
    int outputSeconds,
    long cycleMillis,
    int cycleAudioBytes,
    String shipKey,
    String outputPathPrefix,
    int outputFileNumberDigits,
    int pcmChunkSizeBytes,
    int shipAheadSeconds
  ) {
    this.broadcastFactory = broadcastFactory;
    this.cycleAudioBytes = cycleAudioBytes;
    this.cycleMillis = cycleMillis;
    this.dubWork = dubWork;
    this.outputFileMode = outputFileMode;
    this.shipKey = shipKey;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.outputMode = outputMode;
    this.outputPathPrefix = outputPathPrefix;
    this.outputSeconds = outputSeconds;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.shipAheadSeconds = shipAheadSeconds;

    timer = MultiStopwatch.start();

    var audioFormat = dubWork.getAudioFormat();
    if (audioFormat.isEmpty()) {
      LOG.debug("Waiting for audio format to be available.");
      return;
    }
    var chain = dubWork.getChain();
    if (chain.isEmpty()) {
      LOG.debug("Waiting for Dub to begin");
      return;
    }

    switch (outputMode) {
      case PLAYBACK -> {
        LOG.info("Will initialize playback output");
        playback = broadcastFactory.player(audioFormat.get());
      }
      case FILE -> {
        LOG.info("Will initialize file output");
        fileWriter = broadcastFactory.writer(audioFormat.get());
      }
    }

    if (0 < outputSeconds) {
      LOG.info("Will start in {} output mode and run {} seconds", outputMode, outputSeconds);
    } else {
      LOG.info("Will start in {} output mode and run indefinitely", outputMode);
    }

    running.set(true);
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
  public void runCycle() {
    if (!running.get()) return;

    if (dubWork.isFinished()) {
      LOG.warn("DubWork not running, will stop");
      finish();
    }

    if (System.currentTimeMillis() < nextCycleAtSystemMillis) return;

    nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;

    // Action based on mode
    try {
      if (dubWork.getMixerBuffer().isEmpty() || dubWork.getMixerOutputMicrosPerByte().isEmpty()) return;
      if (isAheadOfSync()) return;
      switch (outputMode) {
        case PLAYBACK -> doShipOutputPlayback();
        case FILE -> doShipOutputFile();
      }
      getShippedToChainMicros().ifPresent(dubWork::setNowAtToChainMicros);
    } catch (Exception e) {
      didFailWhile("running a work cycle", e);
    }

    // End lap & do telemetry on all fabricated chains
    timer.lap();
    LOG.debug("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
    nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;
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
      case FILE -> Objects.nonNull(outputFile) ? Optional.of(outputFile.getToChainMicros()) : Optional.empty();
    };
  }

  @Override
  public Optional<Long> getShipTargetChainMicros() {
    return switch (outputMode) {
      case PLAYBACK -> Optional.of(targetChainMicros);
      case FILE -> Objects.nonNull(outputFile) ? Optional.of(outputFile.getToChainMicros()) : Optional.empty();
    };
  }

  /**
   Whether the ship service is ahead of sync (e.g. in realtime playback mode)

   @return true if ahead of sync, false if not
   */
  boolean isAheadOfSync() {
    var shippedToChainMicros = getShippedToChainMicros();
    if (outputMode.isSync() && shippedToChainMicros.isPresent()) {
      var aheadSeconds = (float) (targetChainMicros - shippedToChainMicros.get()) / MICROS_PER_SECOND;
      if (aheadSeconds > shipAheadSeconds) {
        LOG.debug("Ahead by {}s in synchronous output; will skip work", String.format("%.1f", aheadSeconds));
        return true;
      }
    }
    return false;
  }

  /**
   Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputPlayback() {
    {
      if (Objects.isNull(playback)) {
        didFailWhile("shipping bytes to local playback", new IllegalStateException("Player is null"));
        return;
      }

      LOG.debug("Shipping {} bytes to local playback", cycleAudioBytes);
      playback.append(dubWork.mixNext(cycleAudioBytes));
      targetChainMicros = (long) (targetChainMicros + cycleAudioBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow());
    }
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
    segment = dubWork.getSegmentAtChainMicros(targetChainMicros); // first, get the segment exactly at the play head
    if (segment.isEmpty()) {
      LOG.debug("No segment available at chain micros {}", targetChainMicros);
      return;
    }

    if (Objects.isNull(outputFile)) { // First output file
      doShipOutputFileStartNext(segment.get());

    } else { // Continue output file in progress, start next if needed, finish if done
      if (outputFile.getToChainMicros() - targetChainMicros < pcmChunkSizeBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow()) { // check to see if we are at risk of getting stuck in this last pcm chunk of the segment-- this would never advance to the next segment/output file
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

      int currentFileMaxBytes = (int) ((outputFile.getToChainMicros() - targetChainMicros) / dubWork.getMixerOutputMicrosPerByte().orElseThrow());
      int shipBytes = (int) (pcmChunkSizeBytes * Math.floor((float) Math.min(cycleAudioBytes, currentFileMaxBytes) / pcmChunkSizeBytes)); // rounded down to a multiple of pcm chunk size bytes
      if (0 == shipBytes) {
        LOG.debug("Will not ship any bytes");
        return;
      }
      LOG.debug("Will ship {} bytes to local files", shipBytes);
      fileWriter.append(dubWork.mixNext(shipBytes));
      targetChainMicros = targetChainMicros + (long) (shipBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow());
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
    var shippedSeconds = (float) targetChainMicros / MICROS_PER_SECOND;
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
    var realtimeRatio = shippedSeconds / timer.getTotalSeconds();
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
