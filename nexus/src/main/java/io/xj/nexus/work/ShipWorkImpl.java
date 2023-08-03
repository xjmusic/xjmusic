// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.tables.pojos.Program;
import io.xj.lib.mixer.AudioFileWriter;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.util.StringUtils;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.Segment;
import io.xj.nexus.ship.ShipException;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.StreamEncoder;
import io.xj.nexus.ship.broadcast.StreamPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.xj.lib.util.ValueUtils.MICROS_PER_MILLI;
import static io.xj.lib.util.ValueUtils.MICROS_PER_SECOND;

public class ShipWorkImpl implements ShipWork {
  static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  static final long INTERNAL_CYCLE_SLEEP_MILLIS = 50;

  @Nullable
  AudioFileWriter fileWriter;
  @Nullable
  OutputFile outputFile = null;
  @Nullable
  StreamPlayer playback;
  @Nullable
  StreamEncoder encoder;
  MultiStopwatch timer;
  WorkState state = WorkState.Initializing;
  final AtomicBoolean running = new AtomicBoolean(true);
  final BroadcastFactory broadcastFactory;
  final DubWork dubWork;
  final NotificationProvider notification;
  final OutputFileMode outputFileMode;
  final OutputMode outputMode;
  final String outputPathPrefix;
  final int cycleAudioBytes;
  final int outputFileNumberDigits;
  final int outputSeconds;
  final int pcmChunkSizeBytes;
  final int planAheadSeconds;
  final long cycleMillis;
  int outputFileNum = 0;
  long atChainMicros = 0;
  long nextCycleAtSystemMillis = System.currentTimeMillis();
  final long initializedAtSystemMillis = System.currentTimeMillis();

  public ShipWorkImpl(
    DubWork dubWork,
    NotificationProvider notification,
    BroadcastFactory broadcastFactory,
    String outputMode,
    String outputFileMode,
    int outputSeconds,
    long cycleMillis,
    int cycleAudioBytes,
    int planAheadSeconds,
    String outputPathPrefix,
    int outputFileNumberDigits,
    int pcmChunkSizeBytes
  ) {
    this.broadcastFactory = broadcastFactory;
    this.cycleAudioBytes = cycleAudioBytes;
    this.cycleMillis = cycleMillis;
    this.dubWork = dubWork;
    this.notification = notification;
    this.outputFileMode = OutputFileMode.valueOf(outputFileMode.toUpperCase(Locale.ROOT));
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.outputMode = OutputMode.valueOf(outputMode.toUpperCase(Locale.ROOT));
    this.outputPathPrefix = outputPathPrefix;
    this.outputSeconds = outputSeconds;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.planAheadSeconds = planAheadSeconds;
  }

  @Override
  public void start() {
    timer = MultiStopwatch.start();
    while (running.get()) {
      if (dubWork.isFailed()) {
        LOG.warn("DubWork failed, stopping");
        finish();
      }

      try {
        if (System.currentTimeMillis() < nextCycleAtSystemMillis) {
          //noinspection BusyWait
          Thread.sleep(INTERNAL_CYCLE_SLEEP_MILLIS);
          continue;
        }

        nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;

        // Action based on state and mode
        try {
          switch (state) {
            case Initializing -> doInit();
            case Working -> doWork();
          }

        } catch (Exception e) {
          didFailWhile("running a work cycle", e);
        }

        // End lap & do telemetry on all fabricated chains
        timer.lap();
        LOG.debug("Lap time: {}", timer.lapToString());
        timer.clearLapSections();
        nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;
      } catch (InterruptedException e) {
        LOG.warn("Interrupted!", e);
        running.set(false);
      }
    }
  }

  @Override
  public void finish() {
    dubWork.finish();
    if (Objects.nonNull(encoder)) {
      encoder.close();
    }
    if (!running.get()) return;
    running.set(false);
    LOG.info("Finished");
  }

  /**
   * Attempt to initialize the output method
   */
  void doInit() {
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
      case HLS -> {
        LOG.info("Will initialize HLS output");
        encoder = broadcastFactory.encoder(audioFormat.get(), dubWork.getInputTemplateKey());
        doInitializedOK();
      }
      case PLAYBACK -> {
        LOG.info("Will initialize playback output");
        playback = broadcastFactory.player(audioFormat.get());
        doInitializedOK();
      }
      case FILE -> {
        LOG.info("Will initialize file output");
        fileWriter = broadcastFactory.writer(audioFormat.get());
        doInitializedOK();
      }
    }
  }

  /**
   * Do the output
   */
  void doWork() throws InterruptedException, ShipException, IOException {
    if (dubWork.getMixerBuffer().isEmpty() || dubWork.getMixerOutputMicrosPerByte().isEmpty()) return;
    if (isAheadOfSync()) return;
    switch (outputMode) {
      case HLS -> doShipOutputStream();
      case PLAYBACK -> doShipOutputPlayback();
      case FILE -> doShipOutputFile();
    }
  }


  /**
   * Called after initialization to start the work cycle
   */
  void doInitializedOK() {
    if (0 < outputSeconds) {
      LOG.info("Will start in {} output mode and run {} seconds", outputMode, outputSeconds);
    } else {
      LOG.info("Will start in {} output mode and run indefinitely", outputMode);
    }
    state = WorkState.Working;
  }

  /**
   * Whether the ship service is ahead of sync (e.g. in realtime playback mode)
   *
   * @return true if ahead of sync, false if not
   */
  boolean isAheadOfSync() {
    if (outputMode.isSync()) {
      var aheadSeconds = (float) (atChainMicros - MICROS_PER_MILLI * (System.currentTimeMillis() - initializedAtSystemMillis)) / MICROS_PER_SECOND;
      if (aheadSeconds > planAheadSeconds) {
        LOG.debug("Ahead by {}s in synchronous output; will skip work", String.format("%.1f", aheadSeconds));
        return true;
      }
    }
    return false;
  }

  /**
   * Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputStream() throws IOException, ShipException {
    {
      if (Objects.isNull(encoder)) {
        didFailWhile("shipping bytes to local playback", new IllegalStateException("Player is null"));
        return;
      }
      var availableBytes = dubWork.getMixerBuffer().orElseThrow().getAvailableByteCount();
      if (availableBytes >= cycleAudioBytes) {
        LOG.debug("Shipping {} bytes to local playback", cycleAudioBytes);
        encoder.append(dubWork.getMixerBuffer().orElseThrow().consume(cycleAudioBytes));
        atChainMicros = (long) (atChainMicros + cycleAudioBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow());
        encoder.setPlaylistAtChainMicros(atChainMicros);
      }
    }
  }

  /**
   * Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputPlayback() throws IOException, ShipException {
    {
      if (Objects.isNull(playback)) {
        didFailWhile("shipping bytes to local playback", new IllegalStateException("Player is null"));
        return;
      }
      var availableBytes = dubWork.getMixerBuffer().orElseThrow().getAvailableByteCount();
      if (availableBytes >= cycleAudioBytes) {
        LOG.debug("Shipping {} bytes to local playback", cycleAudioBytes);
        playback.append(dubWork.getMixerBuffer().orElseThrow().consume(cycleAudioBytes));
        atChainMicros = (long) (atChainMicros + cycleAudioBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow());
      }
    }
  }

  /**
   * Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputFile() throws IOException {
    if (Objects.isNull(fileWriter)) {
      LOG.debug("File writer is null, won't ship output file");
      return;
    }
    Optional<Segment> segment;
    segment = dubWork.getSegmentAtChainMicros(atChainMicros); // first, get the segment exactly at the play head
    if (segment.isEmpty()) {
      LOG.debug("No segment available at chain micros {}", atChainMicros);
      return;
    }

    if (Objects.isNull(outputFile)) { // First output file
      doShipOutputFileStartNext(segment.get());

    } else { // Continue output file in progress, start next if needed, finish if done
      if (outputFile.getToChainMicros() - atChainMicros < pcmChunkSizeBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow()) { // check to see if we are at risk of getting stuck in this last pcm chunk of the segment-- this would never advance to the next segment/output file
        LOG.debug("Not enough space in current output file, will advance to next segment");
        var nextOffset = segment.get().getOffset() + 1;
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

      int availableBytes = Math.min(dubWork.getMixerBuffer().orElseThrow().getAvailableByteCount(), cycleAudioBytes);
      int currentFileMaxBytes = (int) ((outputFile.getToChainMicros() - atChainMicros) / dubWork.getMixerOutputMicrosPerByte().orElseThrow());
      int shipBytes = (int) (pcmChunkSizeBytes * Math.floor((float) Math.min(availableBytes, currentFileMaxBytes) / pcmChunkSizeBytes)); // rounded down to a multiple of pcm chunk size bytes
      if (0 == shipBytes) {
        LOG.debug("Will not ship any bytes");
        return;
      }
      LOG.debug("Will ship {} bytes to local files", shipBytes);
      fileWriter.append(dubWork.getMixerBuffer().orElseThrow().consume(shipBytes));
      atChainMicros = atChainMicros + (long) (shipBytes * dubWork.getMixerOutputMicrosPerByte().orElseThrow());
      if (shippedEnoughSeconds()) {
        doShipOutputFileClose();
      }
    }
  }

  void doShipOutputFileStartNext(Segment firstSegment) {
    doShipOutputFileClose();
    outputFile = new OutputFile(firstSegment);

    try {
      Objects.requireNonNull(fileWriter).open(outputFile.getPath());
    } catch (IOException e) {
      didFailWhile("opening file writer", e);
      return;
    }

    LOG.info("Starting next output file {}", outputFile.getPath());
  }

  void doShipOutputFileClose() {
    if (Objects.requireNonNull(fileWriter).isWriting()) {
      try {
        fileWriter.close();
      } catch (IOException e) {
        didFailWhile("closing file writer", e);
      }
    }
  }

  /**
   * If a finite number of seconds was specified, check if we have shipped that many seconds
   * If we are shipped past the target seconds, exit
   */
  boolean shippedEnoughSeconds() {
    var shippedSeconds = (float) atChainMicros / MICROS_PER_SECOND;
    if (0 == outputSeconds) {
      LOG.info("Shipped {} seconds", String.format("%.1f", shippedSeconds));
      return false;
    }

    // Finite number-zero number of output seconds has been specified
    LOG.info("Shipped {} seconds ({})", String.format("%.2f", shippedSeconds), StringUtils.percentage(shippedSeconds / outputSeconds));

    // But leave if we have not yet shipped that many seconds
    if (shippedSeconds < outputSeconds) return false;

    // We're done! log performance info, close the wav container, and finish
    var realtimeRatio = shippedSeconds / timer.getTotalSeconds();
    LOG.info("Overall performance at {} real-time", String.format("%.1fx", realtimeRatio));
    finish();
    return true;
  }

  /**
   * Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key
   *
   * @param msgWhile phrased like "Doing work"
   * @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    LOG.error("Failed while {} because {}", msgWhile, msgCause, e);

    notification.publish(
      "Ship Failure",
      String.format("Failed while %s because %s\n\n%s", msgWhile, msgCause, StringUtils.formatStackTrace(e)));

    finish();
  }

  @Override
  public boolean isHealthy() {
    // future check whether ship work is actually healthy
    return true;
  }

  /**
   * Output File
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
        dubWork.getInputTemplateKey() +
        "-" + StringUtils.zeroPadded(outputFileNum++, outputFileNumberDigits) +
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
