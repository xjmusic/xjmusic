// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.Text;
import io.xj.nexus.persistence.ChainManager;
import io.xj.ship.ShipException;
import io.xj.ship.broadcast.*;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

/**
 Ship Work Service Implementation
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 SEE: https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 <p>
 Work is performed by runnable executables in a cached thread pool, with callback functions for success and failure.
 */
@Singleton
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private final AtomicReference<State> state;
  private final AudioFormat audioFormat;
  private final BroadcastFactory broadcast;
  private final ChainManager chains;
  private final Janitor janitor;
  private final NotificationProvider notification;
  private final PlaylistPublisher playlist;
  private final SegmentAudioManager segmentAudios;
  private final SourceFactory sources;
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
  private StreamEncoder stream;
  private boolean active = true;
  private long doneUpToSecondsUTC;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextTelemetryMillis = 0;
  private long nextMixMillis = 0;
  private long nextPublishMillis = 0;
  private long nextLoadMillis = 0;

  @Inject
  public ShipWorkImpl(
    BroadcastFactory broadcast,
    ChainManager chains,
    Environment env,
    Janitor janitor,
    NotificationProvider notification,
    PlaylistPublisher playlist,
    SegmentAudioManager segmentAudios,
    SourceFactory sources
  ) {
    this.broadcast = broadcast;
    this.chains = chains;
    this.janitor = janitor;
    this.notification = notification;
    this.playlist = playlist;
    this.segmentAudios = segmentAudios;
    this.sources = sources;

    shipKey = env.getShipKey();
    if (Strings.isNullOrEmpty(shipKey)) {
      LOG.error("Cannot start with null or empty ship key!");
      active = false;
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
    telemetryEnabled = env.isTelemetryEnabled();
    telemetryCycleSeconds = env.getWorkTelemetryCycleSeconds();

    audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      48000,
      32,
      2,
      8,
      48000,
      false);

    player = broadcast.player(audioFormat);

    // This value will advance each time we compute more chunks
    doneUpToSecondsUTC = computeChunkSecondsUTC(System.currentTimeMillis());

    // updated on state change
    state = new AtomicReference<>(State.Active);
    LOG.debug("Instantiated OK");
  }

  @Override
  public void start() {
    // Ship rehydrates from last shipped .m3u8 playlist file #180723357
    playlist.rehydrate()
      .ifPresent(maxSeqNum -> doneUpToSecondsUTC = (maxSeqNum + 1) * chunkTargetDuration);

    // Collect garbage before we begin
    var nowSeqNum = playlist.computeMediaSequence(System.currentTimeMillis());
    playlist.collectGarbage(nowSeqNum);

    // Check to see if in fact, this is a stale playlist and should be reset
    if (playlist.isEmpty()) {
      doneUpToSecondsUTC = (long) nowSeqNum * chunkTargetDuration;
    }

    while (active) this.doCycle();
  }

  @Override
  public void finish() {
    active = false;
    player.close();
  }

  @Override
  public boolean isHealthy() {
    if (!active) return notHealthy("Not Active!");
    if (isCycleStale()) return notHealthy("Work cycle is stale!");
    if (State.Fail.equals(state.get())) return notHealthy("Work entered a failure state!");
    if (Objects.isNull(stream)) return notHealthy("Stream encoder has not yet started!");
    if (!stream.isHealthy()) return notHealthy("Stream encoder is not healthy!");
    if (!playlist.isHealthy()) return notHealthy("Playlist is not healthy!");
    return true;
  }

  /**
   Return false after logging a warning message

   @param message to warn
   @return false
   */
  private boolean notHealthy(String message) {
    LOG.warn(message);
    return false;
  }

  /**
   @return true if the cycle is stale
   */
  private boolean isCycleStale() {
    if (0 == nextCycleMillis) return false;
    return nextCycleMillis < System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   Run one work cycle
   */
  private void doCycle() {
    var nowMillis = System.currentTimeMillis();
    if (nowMillis < nextCycleMillis) return;
    nextCycleMillis = nowMillis + cycleMillis;

    if (state.get() == State.Active)
      try {
        doLoadCycle(nowMillis);
        doMixCycle(nowMillis);
        doPublishCycle(nowMillis);
        doCleanupCycle(nowMillis);
        doTelemetryCycle(nowMillis);

      } catch (Exception e) {
        var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        LOG.error("Failed while running ship work because {}", detail, e);
        notification.publish("Failure", String.format("Failed while running ship work because %s\n\n%s", detail, Text.formatStackTrace(e)));
      }
  }

  /**
   Print the chunks every N milliseconds

   @param nowMillis current
   */
  private void doMixCycle(long nowMillis) throws ShipException {
    ChunkMixer mixer;

    if (nowMillis < nextMixMillis) return;
    nextMixMillis = nowMillis + mixCycleSeconds * MILLIS_PER_SECOND;

    // Compute next chunk
    if (!chains.existsForShipKey(shipKey)) return;
    var computeToSecondsUTC = computeChunkSecondsUTC(nowMillis + shipAheadMillis);
    if (doneUpToSecondsUTC == computeToSecondsUTC) return;
    var chunk = broadcast.chunk(shipKey, doneUpToSecondsUTC / chunkTargetDuration, null, null);

    // Use the first chunk to initialize the stream-- this is how we keep ffmpeg in sync with our chunk scheme
    if (Objects.isNull(stream))
      stream = broadcast.encoder(shipKey, audioFormat);

    // get the mixer for this chunk before pulling the trigger
    mixer = broadcast.mixer(chunk, audioFormat);

    // if this chunk isn't ready to be mixed, then quit the mix cycle for now. we won't update done-up-to any further this cycle
    if (!mixer.isReadyToMix()) return;

    // pass the mixed bytes through the various potential output busses
    player.append(stream.append(mixer.mix()));

    // having arrived here, we confirm that the chunks have been mixed up to here.
    doneUpToSecondsUTC = chunk.getToSecondsUTC();
  }

  /**
   Load sources every N milliseconds

   @param nowMillis current
   */
  private void doLoadCycle(long nowMillis) {
    if (nowMillis < nextLoadMillis) return;
    nextLoadMillis = nowMillis + loadCycleSeconds * MILLIS_PER_SECOND;
    sources.loadChain(shipKey, () -> state.set(State.Fail)).run();
  }

  /**
   Clean up every N milliseconds

   @param nowMillis current
   */
  private void doCleanupCycle(long nowMillis) {
    if (!janitorEnabled) return;
    if (nowMillis < nextJanitorMillis) return;
    nextJanitorMillis = nowMillis + (janitorCycleSeconds * MILLIS_PER_SECOND);
    janitor.cleanup();
  }

  /**
   Clean up every N milliseconds

   @param nowMillis current
   */
  private void doTelemetryCycle(long nowMillis) {
    if (!telemetryEnabled) return;
    if (nowMillis < nextTelemetryMillis) return;
    nextTelemetryMillis = nowMillis + (telemetryCycleSeconds * MILLIS_PER_SECOND);
    segmentAudios.sendTelemetry();
    playlist.sendTelemetry();
  }

  /**
   Publish the playlist every N milliseconds

   @param nowMillis current
   */
  private void doPublishCycle(long nowMillis) throws ShipException {
    if (nowMillis < nextPublishMillis) return;
    nextPublishMillis = nowMillis + (publishCycleSeconds * MILLIS_PER_SECOND);
    if (Objects.nonNull(stream))
      stream.publish(nowMillis);
  }

  /**
   Compute the from-seconds-utc for a given now (in milliseconds)

   @param nowMillis current
   @return seconds UTC
   */
  public long computeChunkSecondsUTC(long nowMillis) {
    return (long) (Math.floor((double) (nowMillis / MILLIS_PER_SECOND) / chunkTargetDuration) * chunkTargetDuration);
  }

  /**
   State of the ship work
   */
  enum State {
    Active,
    Fail
  }
}
