// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master.impl;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.config.Config;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.util.Text;
import io.xj.dub.exception.DubException;
import io.xj.dub.master.MasterDub;
import io.xj.mixer.Mixer;
import io.xj.mixer.MixerConfig;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.OutputEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Objects;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MasterDubImpl implements MasterDub {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private final Logger log = LoggerFactory.getLogger(MasterDubImpl.class);
  private final Fabricator fabricator;
  private final MixerFactory mixerFactory;
  private final List<String> warnings = Lists.newArrayList();
  private final AudioCacheProvider audioCacheProvider;
  private final long audioAttackMicros = Config.getMixerSampleAttackMicros();
  private final long audioReleaseMicros = Config.getMixerSampleReleaseMicros();
  private Mixer _mixer;

  @Inject
  public MasterDubImpl(
    @Assisted("basis") Fabricator fabricator,
    AudioCacheProvider audioCacheProvider,
    MixerFactory mixerFactory
    /*-*/) {
    this.audioCacheProvider = audioCacheProvider;
    this.fabricator = fabricator;
    this.mixerFactory = mixerFactory;
  }

  /**
   Microseconds of seconds

   @param seconds to convert
   @return microseconds
   */
  private static Long toMicros(Double seconds) {
    return (long) (seconds * MICROSECONDS_PER_SECOND);
  }

  @Override
  public void doWork() throws DubException {
    FabricatorType type = null;
    try {
      type = fabricator.getType();
      doMixerSourceLoading();
      doMixerTargetSetting();
      doMix();
      reportWarnings();
      report();

    } catch (Exception e) {
      throw new DubException(
        String.format("Failed to do %s-type MasterDub for segment #%s",
          type, fabricator.getSegment().getId().toString()), e);
    }
  }

  /**
   @throws Exception if failed to stream data of item from cache
   */
  private void doMixerSourceLoading() throws Exception {
    for (Audio audio : fabricator.getPickedAudios()) {
      String key = audio.getWaveformKey();

      if (!mixer().hasLoadedSource(audio.getId().toString())) try {
        BufferedInputStream stream = audioCacheProvider.get(key).stream();
        mixer().loadSource(audio.getId().toString(), stream);
        stream.close();

      } catch (Exception e) {
        audioCacheProvider.refresh(key);
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
    }
  }

  /**
   Implements Mixer module to set playback for Picks in current Segment
   */
  private void doMixerTargetSetting() {
    for (Pick pick : fabricator.getSegment().getPicks())
      try {
        setupTarget(pick);
      } catch (Exception e) {
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
  }

  /**
   Set playback for a pick
   <p>
   [#283] Pitch ratio should result in lower audio playback for lower note
   [#341] Dubworker takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter

   @param pick to set playback for
   */
  private void setupTarget(Pick pick) throws Exception {
    double pitchRatio = fabricator.getAudio(pick.getAudioId()).getPitch() / pick.getPitch();
    double offsetStart = fabricator.getAudio(pick.getAudioId()).getStart() / pitchRatio;

    mixer().put(
      pick.getAudioId().toString(),
      toMicros(pick.getStart() - offsetStart),
      toMicros(pick.getStart() + pick.getLength()),
      audioAttackMicros,
      audioReleaseMicros,
      pick.getAmplitude(), pitchRatio, 0);
  }

  /**
   MasterDub implements Mixer module to mix final output to waveform streamed directly to Amazon S3
   */
  private void doMix() throws Exception {
    float quality = Float.valueOf(fabricator.getChainConfig(ChainConfigType.OutputEncodingQuality).getValue());
    mixer().mixToFile(OutputEncoder.parse(fabricator.getChainConfig(ChainConfigType.OutputContainer)), fabricator.getOutputFilePath(), quality);
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  private Mixer mixer() throws Exception {
    if (Objects.isNull(_mixer)) {
      MixerConfig config = new MixerConfig(fabricator.getOutputAudioFormat(), fabricator.getSegmentTotalLength())
        .setNormalizationMax(Config.getMixerNormalizationMax())
        .setDSPBufferSize(Config.getDSPBufferSize())
        .setCompressRatioMax(Config.getMixerCompressRatioMax())
        .setCompressRatioMin(Config.getMixerCompressRatioMin())
        .setHighpassThresholdHz(Config.getMixerHighpassThresholdHz())
        .setLowpassThresholdHz(Config.getMixerLowpassThresholdHz())
        .setCompressToAmplitude(Config.getMixerCompressToAmplitude())
        .setCompressAheadSeconds(Config.getMixerCompressAheadSeconds())
        .setCompressDecaySeconds(Config.getMixerCompressDecaySeconds());
      _mixer = mixerFactory.createMixer(config);
    }

    return _mixer;
  }

  /**
   Report
   */
  private void report() {
    // fabricator.report() anything else interesting from the dub operation
  }

  /**
   Report warnings in a concatenated Segment Message
   */
  private void reportWarnings() {
    if (warnings.isEmpty()) return;

    StringBuilder body = new StringBuilder("MasterDub had warnings:");
    for (String warning : warnings) {
      body.append(String.format("%n%n%s", warning));
    }
    createSegmentMessage(MessageType.Warning, body.toString());
  }

  /**
   [#226] Messages pertaining to a Segment

   @param type of message
   @param body of message
   */
  private void createSegmentMessage(MessageType type, String body) {
    try {
      fabricator.add(new SegmentMessage()
        .setType(type.toString())
        .setBody(body));
    } catch (Exception e1) {
      log.warn("Failed to create SegmentMessage", e1);
    }
  }

}
