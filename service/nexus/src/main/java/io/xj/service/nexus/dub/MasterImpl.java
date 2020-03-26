// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.service.hub.cache.AudioCacheProvider;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.HubException;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.hub.model.SegmentType;
import io.xj.service.hub.model.ChainConfigType;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.lib.util.Text;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerConfig;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.mixer.OutputEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MasterImpl implements Master {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private static final long OUTPUT_LENGTH_EXTRA_SECONDS = 2;
  private final Logger log = LoggerFactory.getLogger(MasterImpl.class);
  private final Fabricator fabricator;
  private final MixerFactory mixerFactory;
  private final List<String> warnings = Lists.newArrayList();
  private final Map<UUID, Double> pickOffsetStart = Maps.newHashMap();
  private final Map<UUID, Double> pickPitchRatio = Maps.newHashMap();
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
  public MasterImpl(
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
    SegmentType type = null;
    try {
      type = fabricator.getType();
      doMixerSourceLoading();
      Double preroll = computePreroll();
      doMixerTargetSetting(preroll);
      doMix();
      reportWarnings();
      report();

      // write updated segment with waveform preroll
      fabricator.getSegment().setWaveformPreroll(preroll);
      fabricator.done();

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
   Iterate through every picked audio and, based on its transient and position in the segment, determine the preroll required, and keep the maximum preroll required out of all the audios.
   <p>
   [#165799913] Dubbed audio can begin before segment start
   - During dub work, the waveform preroll required for the current segment is determined by finding the earliest positioned audio sample. **This process must factor in the transient of each audio sample.**

   @return computed preroll (in seconds)
   */
  private double computePreroll() {
    double maxPreroll = 0.0;
    for (SegmentChoiceArrangementPick pick : fabricator.getSegmentPicks())
      try {
        maxPreroll = Math.max(maxPreroll, computeOffsetStart(pick) - pick.getStart());
      } catch (Exception e) {
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
    return maxPreroll;
  }

  /**
   Implements Mixer module to set playback for Picks in current Segment
   <p>
   [#165799913] Dubbed audio can begin before segment start
   - During dub work, output audio includes the head start, and `waveform_preroll` value is persisted to segment

   @param preroll (seconds)
   */
  private void doMixerTargetSetting(Double preroll) {
    for (SegmentChoiceArrangementPick pick : fabricator.getSegmentPicks())
      try {
        setupTarget(preroll, pick);
      } catch (Exception e) {
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
  }

  /**
   Set playback for a pick
   <p>
   [#283] Pitch ratio should result in lower audio playback for lower note
   [#341] Dub process takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter
   [#165799913] Dubbed audio can begin before segment start
   - During dub work, output audio includes the head start, and `waveform_preroll` value is persisted to segment
   [#171224848] Duration of events should include segment preroll

   @param preroll (seconds)
   @param pick    to set playback for
   */
  private void setupTarget(Double preroll, SegmentChoiceArrangementPick pick) throws Exception {
    mixer().put(pick.getInstrumentAudioId().toString(),
      toMicros(preroll + pick.getStart() - computeOffsetStart(pick)),
      toMicros(preroll + pick.getStart() + pick.getLength()) + audioReleaseMicros,
      audioAttackMicros,
      audioReleaseMicros,
      pick.getAmplitude(),
      computePitchRatio(pick),
      0);
  }

  /**
   Compute the pitch ratio for a pick, and cache results
   <p>
   If the picked audio is at higher pitch than the original source material, the ratio will be < 1.0 --
   meaning that the audio is to be played back at a slower speed (lower pitch).
   <p>
   A ratio > 1.0 means the audio is to be played back at a faster speed (higher pitch).

   @param pick to get pitch ratio for
   @return pitch ratio, or cached result
   @throws HubException on failure
   */
  private Double computePitchRatio(SegmentChoiceArrangementPick pick) throws HubException {
    if (!pickPitchRatio.containsKey(pick.getId()))
      pickPitchRatio.put(pick.getId(), fabricator.getSourceMaterial().getAudio(pick).getPitch() / pick.getPitch());
    return pickPitchRatio.get(pick.getId());
  }

  /**
   Compute the offset start for a pick, and cache results

   @param pick to get offset start for
   @return offset start, or cached result
   @throws HubException on failure
   */
  private Double computeOffsetStart(SegmentChoiceArrangementPick pick) throws HubException {
    if (!pickOffsetStart.containsKey(pick.getId()))
      pickOffsetStart.put(pick.getId(), fabricator.getSourceMaterial().getAudio(pick).getStart() / computePitchRatio(pick));
    return pickOffsetStart.get(pick.getId());
  }

  /**
   MasterDub implements Mixer module to mix final output to waveform streamed directly to Amazon S3@param preroll
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
      MixerConfig config = new MixerConfig(fabricator.getOutputAudioFormat(), fabricator.getSegmentTotalLength().plusSeconds(OUTPUT_LENGTH_EXTRA_SECONDS)) // TODO need to compute actual longest sound in segment
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
