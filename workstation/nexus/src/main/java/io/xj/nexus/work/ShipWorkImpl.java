// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.util.StringUtils;
import io.xj.nexus.mixer.AudioFileWriter;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.StreamPlayer;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private static final String TIMER_SECTION_SHIP = "Ship";
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final Telemetry telemetry;
  private final DubWork dubWork;
  private long shippedToChainMicros = 0;

  @Nullable
  AudioFileWriter fileWriter;

  @Nullable
  StreamPlayer playback;

  public ShipWorkImpl(
    Telemetry telemetry,
    DubWork dubWork,
    BroadcastFactory broadcastFactory
  ) {
    this.telemetry = telemetry;
    this.dubWork = dubWork;

    var audioFormat = dubWork.getAudioFormat().orElseThrow(() ->
      new RuntimeException("Unable to get audio format from dub work"));

    LOG.info("Will initialize playback output");
    playback = broadcastFactory.player(
      audioFormat,
      (int) (dubWork.getMixerLengthSeconds() * audioFormat.getFrameRate() * audioFormat.getFrameSize())
    );

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
      doShipOutputPlayback();
      telemetry.record(TIMER_SECTION_SHIP, System.currentTimeMillis() - startedAtMillis);

    } catch (Exception e) {
      didFail(e);
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
  public Optional<Long> getShippedToChainMicros() {
    return Objects.nonNull(playback) ? Optional.of(playback.getHeardAtChainMicros()) : Optional.empty();
  }

  /**
   Ship available bytes from the dub mixer buffer to the output method
   */
  void doShipOutputPlayback() throws IOException {
    Objects.requireNonNull(playback);
    if (dubWork.getMixerBuffer().isEmpty())
      throw new IOException("Mixer buffer is empty");
    var availableBytes = dubWork.getMixerBuffer().get().getAvailableByteCount();
    playback.write(dubWork.getMixerBuffer().get().consume(availableBytes));
    shippedToChainMicros = (long) (shippedToChainMicros + availableBytes * dubWork.getMixerOutputMicrosPerByte());
  }

  /**
   Ship did fail
   */
  void didFail(Exception e) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
    LOG.error("Failed while {} because {}", "running ship work", msgCause, e);
    finish();
  }
}
