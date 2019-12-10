// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.core.cache.AudioCacheProvider;
import io.xj.core.entity.MessageType;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentMessage;
import io.xj.core.util.Text;
import io.xj.dub.exception.DubException;
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
  private final long audioAttackMicros;
  private final long audioReleaseMicros;
  private Mixer _mixer;
  private double normalizationMax;
  private int dspBufferSize;
  private double compressRatioMax;
  private double compressRatioMin;
  private double highpassThresholdHz;
  private double lowpassThresholdHz;
  private double compressToAmplitude;
  private double compressAheadSeconds;
  private double compressDecaySeconds;

  @Inject
  public MasterDubImpl(
    @Assisted("basis") Fabricator fabricator,
    AudioCacheProvider audioCacheProvider,
    MixerFactory mixerFactory,
    Config config
    /*-*/) {
    this.audioCacheProvider = audioCacheProvider;
    this.fabricator = fabricator;
    this.mixerFactory = mixerFactory;

    audioAttackMicros = config.getInt("mixer.sampleAttackMicros");
    audioReleaseMicros = config.getInt("mixer.sampleReleaseMicros");
    normalizationMax = config.getDouble("mixer.normalizationMax");
    dspBufferSize = config.getInt("mixer.dspBufferSize");
    compressRatioMax = config.getDouble("mixer.compressRatioMax");
    compressRatioMin = config.getDouble("mixer.compressRatioMin");
    highpassThresholdHz = config.getDouble("mixer.highpassThresholdHz");
    lowpassThresholdHz = config.getDouble("mixer.lowpassThresholdHz");
    compressToAmplitude = config.getDouble("mixer.compressToAmplitude");
    compressAheadSeconds = config.getDouble("mixer.compressAheadSeconds");
    compressDecaySeconds = config.getDouble("mixer.compressDecaySeconds");
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
   @throws Exception if failed to stream data of item of cache
   */
  private void doMixerSourceLoading() throws Exception {
    for (InstrumentAudio audio : fabricator.getPickedAudios()) {
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
    for (SegmentChoiceArrangementPick pick : fabricator.getSegmentPicks())
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
  private void setupTarget(SegmentChoiceArrangementPick pick) throws Exception {
    double pitchRatio = fabricator.getSourceMaterial().getAudio(pick).getPitch() / pick.getPitch();
    double offsetStart = fabricator.getSourceMaterial().getAudio(pick).getStart() / pitchRatio;

    mixer().put(
      pick.getInstrumentAudioId().toString(),
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
        .setLogPrefix(String.format("[segId=%s] ", fabricator.getSegment().getId()))
        .setNormalizationMax(normalizationMax)
        .setDSPBufferSize(dspBufferSize)
        .setCompressRatioMax(compressRatioMax)
        .setCompressRatioMin(compressRatioMin)
        .setHighpassThresholdHz(highpassThresholdHz)
        .setLowpassThresholdHz(lowpassThresholdHz)
        .setCompressToAmplitude(compressToAmplitude)
        .setCompressAheadSeconds(compressAheadSeconds)
        .setCompressDecaySeconds(compressDecaySeconds);


      _mixer = mixerFactory.createMixer(config);
    }

    return _mixer;
  }

  /**
   Report
   */
  private void report() {
    // fabricator.report() anything else interesting of the dub operation
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
      fabricator.add(SegmentMessage.create(fabricator.getSegment(), type, body));
    } catch (Exception e1) {
      log.warn("Failed to create SegmentMessage", e1);
    }
  }

}
