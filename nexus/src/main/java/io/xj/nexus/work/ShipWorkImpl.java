// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import io.xj.lib.mixer.BytePipeline;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.Segment;
import io.xj.nexus.ship.ShipException;
import io.xj.lib.mixer.AudioFileWriter;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.StreamPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.xj.lib.util.Values.MICROS_PER_MILLI;
import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

@Service
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private static final long INTERNAL_CYCLE_SLEEP_MILLIS = 50;

  @Nullable
  private AudioFileWriter fileWriter;
  @Nullable
  private OutputFile outputFile = null;
  @Nullable
  private StreamPlayer playback;
  private MultiStopwatch timer;
  private WorkState state = WorkState.Initializing;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final BroadcastFactory broadcastFactory;
  private final CraftWork craftWork;
  private final DubWork dubWork;
  private final NotificationProvider notification;
  private final OutputFileMode outputFileMode;
  private final OutputMode outputMode;
  private final String outputPathPrefix;
  private final boolean janitorEnabled;
  private final int cycleAudioBytes;
  private final int janitorCycleSeconds;
  private final int outputFileNumberDigits;
  private final int outputSeconds;
  private final int pcmChunkSizeBytes;
  private final int planAheadSeconds;
  private final long cycleMillis;
  private int outputFileNum = 0;
  private long atChainMicros = 0;
  private long nextCycleAtSystemMillis = System.currentTimeMillis();
  private long nextJanitorAtSystemMillis = System.currentTimeMillis();
  private final long initializedAtSystemMillis = System.currentTimeMillis();

  @Autowired
  public ShipWorkImpl(
    DubWork dubWork,
    CraftWork craftWork,
    NotificationProvider notification,
    BroadcastFactory broadcastFactory,
    @Value("${output.mode}") String outputMode,
    @Value("${output.file.mode}") String outputFileMode,
    @Value("${output.seconds}") int outputSeconds,
    @Value("${ship.cycle.millis}") long cycleMillis,
    @Value("${ship.janitor.enabled}") boolean janitorEnabled,
    @Value("${ship.janitor.cycle.seconds}") int janitorCycleSeconds,
    @Value("${ship.cycle.audio.bytes}") int cycleAudioBytes,
    @Value("${ship.output.synchronous.plan.ahead.seconds}") int planAheadSeconds,
    @Value("${output.path.prefix}") String outputPathPrefix,
    @Value("${output.file.number.digits}") int outputFileNumberDigits,
    @Value("${output.pcm.chunk.size.bytes}") int pcmChunkSizeBytes
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftWork = craftWork;
    this.cycleAudioBytes = cycleAudioBytes;
    this.cycleMillis = cycleMillis;
    this.dubWork = dubWork;
    this.janitorCycleSeconds = janitorCycleSeconds;
    this.janitorEnabled = janitorEnabled;
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
      if (craftWork.isFailed()) {
        LOG.warn("CraftWork failed, stopping");
        finish();
      }

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
          if (janitorEnabled) doJanitor();

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
    if (!running.get()) return;
    running.set(false);
    LOG.info("Finished");
  }

  /**
   * Attempt to initialize the output method
   */
  private void doInit() {
    var audioFormat = dubWork.getAudioFormat();
    if (audioFormat.isEmpty()) {
      LOG.debug("Waiting for audio format to be available.");
      return;
    }
    var chain = craftWork.getChain();
    if (chain.isEmpty()) {
      LOG.debug("Waiting for Dub to begin");
      return;
    }

    switch (outputMode) {
      case HLS -> {
        LOG.info("Will initialize HLS output");
        // future: implement HLS S3 output initialization
        didFailWhile("initializing HLS S3 output", new UnsupportedOperationException("it's not yet implemented"));
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
  private void doWork() throws InterruptedException, ShipException, IOException, NexusException {
    var mixerBuffer = dubWork.getMixerBuffer();
    var microsecondsPerByte = dubWork.getMixerOutputMicrosecondsPerByte();
    if (mixerBuffer.isEmpty() || microsecondsPerByte.isEmpty()) return;
    if (isAheadOfSync()) return;
    switch (outputMode) {
      case HLS -> {
        // Future: implement HLS S3 byte shipment
      }
      case PLAYBACK -> doShipOutputPlayback(mixerBuffer.get(), microsecondsPerByte.get());
      case FILE -> doShipOutputFiles(mixerBuffer.get(), microsecondsPerByte.get());
    }
  }


  /**
   * Called after initialization to start the work cycle
   */
  private void doInitializedOK() {
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
  private boolean isAheadOfSync() {
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
  private void doShipOutputPlayback(BytePipeline pipeline, float bytesPerMicrosecond) throws IOException, ShipException {
    {
      if (Objects.isNull(playback)) {
        didFailWhile("shipping bytes to local playback", new IllegalStateException("Player is null"));
        return;
      }
      var availableBytes = pipeline.getAvailableByteCount();
      if (availableBytes >= cycleAudioBytes) {
        LOG.debug("Shipping {} bytes to local playback", cycleAudioBytes);
        playback.append(pipeline.consume(cycleAudioBytes));
        atChainMicros = atChainMicros + (long) (cycleAudioBytes / bytesPerMicrosecond);
      }
    }
  }

  /**
   * Ship available bytes from the dub mixer buffer to the output method
   */
  private void doShipOutputFiles(BytePipeline pipeline, float microSecondsPerByte) throws IOException, NexusException, ShipException {
    if (Objects.isNull(fileWriter)) {
      LOG.debug("File writer is null, won't ship output files");
      return;
    }
    var segment = craftWork.getSegmentAt(atChainMicros);
    if (segment.isEmpty()) {
      LOG.debug("No segment available for {}", atChainMicros);
      return;
    }

    if (Objects.isNull(outputFile)) { // First output file
      doShipOutputFileStartNext(segment.get());

    } else { // Continue output file in progress, start next if needed, finish if done
      if (!outputFile.contains(segment.get())) {
        switch (outputFileMode) {
          case CONTINUOUS -> outputFile.add(segment.get());
          case SEGMENT -> doShipOutputFileStartNext(segment.get());
          case MAIN -> {
            var currentMainProgram = craftWork.getMainProgram(outputFile.getLastSegment());
            var nextMainProgram = craftWork.getMainProgram(segment.get());
            if (currentMainProgram.isEmpty() || nextMainProgram.isEmpty()) {
              LOG.debug("Not fully ready, waiting for current and/or next main program");
              return;
            }
            if (!currentMainProgram.get().getId().equals(nextMainProgram.get().getId())) {
              doShipOutputFileStartNext(segment.get());
            }
          }
          case MACRO -> {
            var currentMacroProgram = craftWork.getMacroProgram(outputFile.getLastSegment());
            var nextMacroProgram = craftWork.getMacroProgram(segment.get());
            if (currentMacroProgram.isEmpty() || nextMacroProgram.isEmpty()) {
              LOG.debug("Not fully ready, waiting for current and/or next macro program");
              return;
            }
            if (!currentMacroProgram.get().getId().equals(nextMacroProgram.get().getId())) {
              doShipOutputFileStartNext(segment.get());
            }
          }
        }
      }

      var availableBytes = pipeline.getAvailableByteCount();
      var shipBytes = (int) (pcmChunkSizeBytes * Math.floor(Math.min(Math.min(availableBytes, cycleAudioBytes), (outputFile.getToChainMicros() - atChainMicros) / microSecondsPerByte) / pcmChunkSizeBytes));
      if (0 == shipBytes) {
        LOG.debug("Will not ship any bytes");
        return;
      }
      LOG.debug("Will ship {} bytes to local files", shipBytes);
      fileWriter.append(pipeline.consume(shipBytes));
      atChainMicros = atChainMicros + (long) (shipBytes * microSecondsPerByte);
      if (shippedEnoughSeconds()) {
        doShipOutputFileClose();
      }
    }
  }

  private void doShipOutputFileStartNext(Segment firstSegment) {
    doShipOutputFileClose();

    var path = outputPathPrefix + craftWork.getInputTemplateKey() + "-" + Text.zeroPadded(outputFileNum++, outputFileNumberDigits) + ".wav";
    try {
      Objects.requireNonNull(fileWriter).open(path);
    } catch (IOException e) {
      didFailWhile("opening file writer", e);
      return;
    }

    outputFile = new OutputFile(firstSegment);
    LOG.info("Starting next output file {}", path);
  }

  private void doShipOutputFileClose() {
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
  private boolean shippedEnoughSeconds() {
    var shippedSeconds = (float) atChainMicros / MICROS_PER_SECOND;
    if (0 == outputSeconds) {
      LOG.info("Shipped {} seconds", String.format("%.1f", shippedSeconds));
      return false;
    }

    // Finite number-zero number of output seconds has been specified
    LOG.info("Shipped {} seconds ({})", String.format("%.2f", shippedSeconds), Text.percentage(shippedSeconds / outputSeconds));

    // But leave if we have not yet shipped that many seconds
    if (shippedSeconds < outputSeconds) return false;

    // We're done! log performance info, close the wav container, and finish
    var realtimeRatio = shippedSeconds / timer.getTotalSeconds();
    LOG.info("Overall performance at {} real-time", String.format("%.1fx", realtimeRatio));
    finish();
    return true;
  }

  /**
   * Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  protected void doJanitor() {
    if (System.currentTimeMillis() < nextJanitorAtSystemMillis) return;
    nextJanitorAtSystemMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Janitor");

    // future: ship work janitor
  }

  /**
   * Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key
   *
   * @param msgWhile phrased like "Doing work"
   * @param e        exception (optional)
   */
  private void didFailWhile(String msgWhile, Exception e) {
    var msgCause = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    LOG.error("Failed while {} because {}", msgWhile, msgCause, e);

    notification.publish(
      "Ship Failure",
      String.format("Failed while %s because %s\n\n%s", msgWhile, msgCause, Text.formatStackTrace(e)));

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
  static class OutputFile {

    private final List<Segment> segments;
    private long toChainMicros;

    OutputFile(
      Segment firstSegment
    ) {
      this.segments = Lists.newArrayList();
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
  }
}
