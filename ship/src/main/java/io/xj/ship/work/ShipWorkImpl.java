// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.base.Strings;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.Text;
import io.xj.nexus.persistence.ChainManager;
import io.xj.ship.ShipException;
import io.xj.ship.broadcast.BroadcastFactory;
import io.xj.ship.broadcast.ChunkFactory;
import io.xj.ship.broadcast.ChunkMixer;
import io.xj.ship.broadcast.MediaSeqNumProvider;
import io.xj.ship.broadcast.PlaylistPublisher;
import io.xj.ship.broadcast.StreamEncoder;
import io.xj.ship.broadcast.StreamPlayer;
import io.xj.ship.broadcast.StreamWriter;
import io.xj.ship.source.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

/**
 * Ship Work Service Implementation
 * <p>
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 * <p>
 * SEE: https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 * <p>
 * Work is performed by runnable executables in a cached thread pool, with callback functions for success and failure.
 */
@Service
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private static final long INTERNAL_CYCLE_SLEEP_MILLIS = 50;
  private final AtomicReference<State> state;
  private final AudioFormat audioFormat;
  private final BroadcastFactory broadcastFactory;
  private final ChunkFactory chunkFactory;
  private final ChainManager chainManager;
  private final Janitor janitor;
  private final MediaSeqNumProvider mediaSeqNumProvider;
  private final NotificationProvider notificationProvider;
  private final PlaylistPublisher playlistPublisher;
  private final SegmentAudioManager segmentAudioManager;
  private final StreamPlayer player;
  private final boolean janitorEnabled;
  private final int chunkTargetDuration;
  private final int cycleMillis;
  private final int janitorCycleSeconds;
  private final int loadCycleSeconds;
  private final int mixCycleSeconds;
  private final int publishCycleSeconds;
  private final int telemetryCycleSeconds;
  private final long healthCycleStalenessThresholdMillis;
  private final long shipAheadMillis;
  @Nullable
  private final String shipKey;
  private final boolean telemetryEnabled;
  private final long initialSeqNum;
  private final StreamWriter writer;
  private StreamEncoder stream;
  private final AtomicBoolean active = new AtomicBoolean(true);
  private long doneUpToSecondsUTC;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextTelemetryMillis = 0;
  private long nextMixMillis = 0;
  private long nextPublishMillis = 0;
  private long nextLoadMillis = 0;
  private final String envName;

  public ShipWorkImpl(
    AppEnvironment env,
    BroadcastFactory broadcastFactory,
    ChainManager chainManager,
    ChunkFactory chunkFactory,
    Janitor janitor,
    MediaSeqNumProvider mediaSeqNumProvider,
    NotificationProvider notificationProvider,
    PlaylistPublisher playlistPublisher,
    SegmentAudioManager segmentAudioManager
  ) {
    this.broadcastFactory = broadcastFactory;
    this.chainManager = chainManager;
    this.chunkFactory = chunkFactory;
    this.janitor = janitor;
    this.mediaSeqNumProvider = mediaSeqNumProvider;
    this.notificationProvider = notificationProvider;
    this.playlistPublisher = playlistPublisher;
    this.segmentAudioManager = segmentAudioManager;
    envName = env.getWorkEnvironmentName();

    shipKey = env.getShipKey();
    if (Strings.isNullOrEmpty(shipKey)) {
      LOG.error("Cannot start with null or empty ship key!");
      active.set(false);
    }

    chunkTargetDuration = env.getShipChunkTargetDuration();
    cycleMillis = env.getWorkCycleMillis();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.isWorkJanitorEnabled();
    loadCycleSeconds = env.getShipLoadCycleSeconds();
    mixCycleSeconds = env.getShipMixCycleSeconds();
    publishCycleSeconds = env.getWorkPublishCycleSeconds();
    shipAheadMillis = env.getShipPlaylistAheadSeconds() * MILLIS_PER_SECOND;
    telemetryCycleSeconds = env.getWorkTelemetryCycleSeconds();
    telemetryEnabled = env.isTelemetryEnabled();

    audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      48000,
      32,
      2,
      8,
      48000,
      false);

    player = broadcastFactory.player(audioFormat);
    writer = broadcastFactory.writer(audioFormat);

    // Snap this once to avoid the edge case of a difference between subsequently computed values
    var nowMillis = System.currentTimeMillis();

    // This is the initial sequence number when the entire ship process was started:
    // necessary to coordinate between mixing, encoding, and playlist publishing
    initialSeqNum = mediaSeqNumProvider.computeInitialMediaSeqNum(nowMillis);
    LOG.info("Initial media sequence number {}", initialSeqNum);

    // This value will advance each time we compute more chunks
    doneUpToSecondsUTC = initialSeqNum * chunkTargetDuration;

    // updated on state change
    state = new AtomicReference<>(State.Active);
    LOG.debug("Instantiated OK");
  }

  @Override
  public void doWork() {
    playlistPublisher.start(initialSeqNum)
      .ifPresent(maxSeqNum -> doneUpToSecondsUTC = (maxSeqNum + 1) * chunkTargetDuration);

    // Collect garbage before we begin
    var nowSeqNum = mediaSeqNumProvider.computeMediaSeqNum(System.currentTimeMillis());
    playlistPublisher.collectGarbage(nowSeqNum);

    // Check to see if in fact, this is a stale playlist and should be reset
    if (playlistPublisher.isEmpty()) {
      doneUpToSecondsUTC = (long) nowSeqNum * chunkTargetDuration;
    }

    LOG.info("Will start Ship");
    while (active.get()) {
      try {
        this.doCycle();
        //noinspection BusyWait
        Thread.sleep(INTERNAL_CYCLE_SLEEP_MILLIS);
      } catch (InterruptedException e) {
        LOG.warn("Ship interrupted!", e);
      }
    }
    active.set(false);
    state.set(State.Done);
    player.close();
    writer.close();
  }

  @Override
  public void finish() {
    active.set(false);
    player.close();
    writer.close();
    LOG.info("Did finish Ship");
  }

  @Override
  public boolean isHealthy() {
    if (!active.get()) return notHealthy("Not Active!");
    if (isCycleStale()) return notHealthy("Work cycle is stale!");
    if (State.Fail.equals(state.get())) return notHealthy("Work entered a failure state!");
    if (Objects.isNull(stream)) return notHealthy("Stream encoder has not yet started!");
    if (!stream.isHealthy()) return notHealthy("Stream encoder is not healthy!");
    if (!playlistPublisher.isHealthy()) return notHealthy("Playlist is not healthy!");
    return true;
  }

  /**
   * Return false after logging a warning message
   *
   * @param message to warn
   * @return false
   */
  private boolean notHealthy(String message) {
    LOG.warn(message);
    return false;
  }

  /**
   * @return true if the cycle is stale
   */
  private boolean isCycleStale() {
    if (0 == nextCycleMillis) return false;
    return nextCycleMillis < System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   * Run one work cycle
   */
  private void doCycle() throws InterruptedException {
    var nowMillis = System.currentTimeMillis();

    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    if (state.get() == State.Active)
      try {
        doLoadCycle(nowMillis);
        doMixCycle(nowMillis);

        doPublishCycle(nowMillis);
        doCleanupCycle(nowMillis);
        doTelemetryCycle(nowMillis);

      } catch (ShipException e) {
        var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        LOG.error("Failed while running ship work because {}", detail, e);
        notificationProvider.publish(String.format("%s-Chain[%s] Ship Failure", envName, shipKey), String.format("Failed while running ship work because %s\n\n%s", detail, Text.formatStackTrace(e)));
      }

    nextCycleMillis = System.currentTimeMillis() + cycleMillis;
  }

  /**
   * Print the chunks every N milliseconds
   *
   * @param nowMillis current
   */
  private void doMixCycle(long nowMillis) throws ShipException {
    ChunkMixer mixer;

    if (nowMillis < nextMixMillis) return;
    nextMixMillis = nowMillis + mixCycleSeconds * MILLIS_PER_SECOND;

    // Compute next chunk
    if (!chainManager.existsForShipKey(shipKey)) return;
    var computeToSecondsUTC = computeChunkSecondsUTC(nowMillis + shipAheadMillis);
    if (doneUpToSecondsUTC == computeToSecondsUTC) return;
    var chunk = chunkFactory.build(shipKey, doneUpToSecondsUTC / chunkTargetDuration, null, null);

    // Use the first chunk to initialize the stream-- this is how we keep ffmpeg in sync with our chunk scheme
    if (Objects.isNull(stream))
      stream = broadcastFactory.encoder(shipKey, audioFormat, initialSeqNum);

    // get the mixer for this chunk before pulling the trigger
    mixer = broadcastFactory.mixer(chunk, audioFormat);

    // if this chunk isn't ready to be mixed, then quit the mix cycle for now. we won't update done-up-to any further this cycle
    if (!mixer.isReadyToMix()) return;

    // pass the mixed bytes through the various potential output busses, to avoid memory storage of the actual mixed bytes
    player.append(
      writer.append(
        stream.append(
          mixer.mix())));

    if (writer.enabledAndDoneWithOutput()) {
      active.set(false);
    }

    // having arrived here, we confirm that the chunks have been mixed up to here.
    doneUpToSecondsUTC = chunk.getToSecondsUTC();
  }

  /**
   * Load sources every N milliseconds
   *
   * @param nowMillis current
   */
  private void doLoadCycle(long nowMillis) {
    if (nowMillis < nextLoadMillis) return;
    nextLoadMillis = nowMillis + loadCycleSeconds * MILLIS_PER_SECOND;
    segmentAudioManager.loadChain(shipKey, () -> state.set(State.Fail), false).run();
  }

  /**
   * Clean up every N milliseconds
   *
   * @param nowMillis current
   */
  private void doCleanupCycle(long nowMillis) {
    if (!writer.isActive()) {
      active.set(false);
    }
    if (!janitorEnabled) return;
    if (nowMillis < nextJanitorMillis) return;
    nextJanitorMillis = nowMillis + (janitorCycleSeconds * MILLIS_PER_SECOND);
    janitor.cleanup();
  }

  /**
   * Clean up every N milliseconds
   *
   * @param nowMillis current
   */
  private void doTelemetryCycle(long nowMillis) {
    if (!telemetryEnabled) return;
    if (nowMillis < nextTelemetryMillis) return;
    nextTelemetryMillis = nowMillis + (telemetryCycleSeconds * MILLIS_PER_SECOND);
    segmentAudioManager.sendTelemetry();
    playlistPublisher.sendTelemetry();
  }

  /**
   * Publish the playlist every N milliseconds
   *
   * @param nowMillis current
   */
  private void doPublishCycle(long nowMillis) throws ShipException {
    if (nowMillis < nextPublishMillis) return;
    nextPublishMillis = nowMillis + (publishCycleSeconds * MILLIS_PER_SECOND);
    if (Objects.nonNull(stream))
      stream.publish(nowMillis);
  }

  /**
   * Compute the from-seconds-utc for a given now (in milliseconds)
   *
   * @param nowMillis current
   * @return seconds UTC
   */
  public long computeChunkSecondsUTC(long nowMillis) {
    return (long) (Math.floor((double) (nowMillis / MILLIS_PER_SECOND) / chunkTargetDuration) * chunkTargetDuration);
  }

  /**
   * State of the ship work
   */
  enum State {
    Active,
    Done,
    Fail
  }
}
