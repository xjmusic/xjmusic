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
import io.xj.ship.source.SourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

/**
 Ship Work Service Implementation
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 SEE: https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 SEE: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html
 <p>
 Work is performed by runnable executables in a cached thread pool, with callback functions for success and failure.
 */
@Singleton
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private final AtomicReference<State> state;
  private final BroadcastFactory broadcast;
  private final Janitor janitor;
  private final NotificationProvider notification;
  private final PlaylistPublisher publisher;
  private final SourceFactory sources;
  private final boolean janitorEnabled;
  private final int cycleMillis;
  private final int janitorCycleSeconds;
  private final int loadCycleSeconds;
  private final int mixCycleSeconds;
  private final int publishCycleSeconds;
  private final long healthCycleStalenessThresholdMillis;
  @Nullable
  private final String shipKey;
  private final ChainManager chains;
  private final int chunkTargetDuration;
  private final long shipAheadMillis;
  private final StreamPlayer player;
  private final AudioFormat audioFormat;
  private StreamEncoder stream;
  private boolean active = true;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextPrintMillis = 0;
  private long nextPublishMillis = 0;
  private long nextShipReloadMillis = 0;
  private long doneUpToSecondsUTC;

  @Inject
  public ShipWorkImpl(
    Environment env,
    BroadcastFactory broadcast,
    ChainManager chains,
    Janitor janitor,
    NotificationProvider notification,
    SourceFactory sources
  ) {
    this.chains = chains;
    this.broadcast = broadcast;
    this.janitor = janitor;
    this.notification = notification;
    this.sources = sources;

    shipKey = env.getShipKey();
    if (Strings.isNullOrEmpty(shipKey)) {
      LOG.error("Cannot start with null or empty ship key!");
      active = false;
    }

    cycleMillis = env.getWorkCycleMillis();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.isWorkJanitorEnabled();
    loadCycleSeconds = env.getShipReloadSeconds();
    mixCycleSeconds = env.getWorkMixCycleSeconds();
    publishCycleSeconds = env.getWorkPublishCycleSeconds();
    chunkTargetDuration = env.getShipChunkTargetDuration();
    shipAheadMillis = env.getShipChunkPlaylistTargetSize() * chunkTargetDuration * MILLIS_PER_SECOND;

    audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      48000,
      32,
      2,
      8,
      48000,
      false);

    player = broadcast.player(audioFormat);
    publisher = broadcast.publisher(shipKey);

    // This value will advance each time we compute more chunks
    doneUpToSecondsUTC = computeChunkSecondsUTC(System.currentTimeMillis());

    // updated on state change
    state = new AtomicReference<>(State.Active);
    LOG.debug("Instantiated OK");
  }

  @Override
  public void work() {
    // Ship rehydrates from last shipped .m3u8 playlist file #180723357
    publisher.rehydratePlaylist();

    while (active) this.run();
  }

  @Override
  public void stop() {
    active = false;
    player.close();
  }

  @Override
  public boolean isHealthy() {
    return active && !isCycleStale() && !State.Fail.equals(state.get());
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
  private void run() {
    var nowMillis = System.currentTimeMillis();
    if (nowMillis < nextCycleMillis) return;
    nextCycleMillis = nowMillis + cycleMillis;

    if (state.get() == State.Active)
      try {
        doLoadCycle(nowMillis);
        doMixCycle(nowMillis);
        doPublishCycle(nowMillis);
        doCleanupCycle(nowMillis);

      } catch (Exception e) {
        var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        LOG.error("Failed while running ship work because {}", detail, e);
        notification.publish(String.format("Failed while running ship work because %s\n\n%s", detail, Text.formatStackTrace(e)), "Failure");
      }
  }

  /**
   Print the chunks every N milliseconds

   @param nowMillis current
   */
  private void doMixCycle(long nowMillis) throws ShipException {
    if (nowMillis < nextPrintMillis) return;
    nextPrintMillis = nowMillis + mixCycleSeconds * MILLIS_PER_SECOND;
    for (var chunk : computeNextChunks(nowMillis)) {

      // Use the first chunk to initialize the stream-- this is how we keep ffmpeg in sync with our chunk scheme
      if (Objects.isNull(stream))
        stream = broadcast.encoder(shipKey, audioFormat);

      // pass the mixed bytes through the various potential output busses
      player.append(stream.append(broadcast.mixer(chunk, audioFormat).mix()));
    }
  }

  /**
   Load sources every N milliseconds

   @param nowMillis current
   */
  private void doLoadCycle(long nowMillis) {
    if (nowMillis < nextShipReloadMillis) return;
    nextShipReloadMillis = nowMillis + loadCycleSeconds * MILLIS_PER_SECOND;
    ForkJoinPool.commonPool().execute(sources.spawnChainBoss(shipKey,
      () -> state.set(State.Fail)
    ));
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
   Publish the playlist every N milliseconds

   @param nowMillis current
   */
  private void doPublishCycle(long nowMillis) throws ShipException {
    if (nowMillis < nextPublishMillis) return;
    nextPublishMillis = nowMillis + (publishCycleSeconds * MILLIS_PER_SECOND);
    publisher.publish(nowMillis);
    if (Objects.nonNull(stream))
      stream.publish(nowMillis);
  }


  /**
   Make the next chunk(s)

   @param nowMillis for which to make chunks
   @return chunks
   */
  public List<Chunk> computeNextChunks(long nowMillis) {
    if (!chains.existsForShipKey(shipKey)) return List.of();
    var computeToSecondsUTC = computeChunkSecondsUTC(nowMillis + shipAheadMillis);
    if (doneUpToSecondsUTC == computeToSecondsUTC) return List.of();
    var chunks = Stream.iterate(doneUpToSecondsUTC, n -> n + chunkTargetDuration)
      .limit((computeToSecondsUTC - doneUpToSecondsUTC) / chunkTargetDuration)
      .map(fromSecondsUTC -> broadcast.chunk(shipKey, fromSecondsUTC / chunkTargetDuration, null, null))
      .collect(Collectors.toList());
    doneUpToSecondsUTC = computeToSecondsUTC;
    return chunks;
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
