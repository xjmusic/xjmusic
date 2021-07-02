// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.InstrumentAudio;
import io.xj.Segment;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentMessage;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerConfig;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DubMasterImpl implements DubMaster {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private final Logger log = LoggerFactory.getLogger(DubMasterImpl.class);
  private final Fabricator fabricator;
  private final MixerFactory mixerFactory;
  private final List<String> warnings = Lists.newArrayList();
  private final Map<String, Double> pickOffsetStart = Maps.newHashMap();
  private final DubAudioCache dubAudioCache;
  private Mixer mixer;

  @Inject
  public DubMasterImpl(
    @Assisted("basis") Fabricator fabricator,
    DubAudioCache dubAudioCache,
    MixerFactory mixerFactory
    /*-*/) {
    this.dubAudioCache = dubAudioCache;
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
  @Trace(resourceName = "nexus/dub/master", operationName = "doWork")
  public void doWork() throws NexusException {
    Segment.Type type = null;
    try {
      type = fabricator.getType();
      doMixerSourceLoading();
      double preroll = computePreroll();
      doMixerTargetSetting(preroll);
      doMix();
      reportWarnings();
      report();

      // write updated segment with waveform preroll
      fabricator.updateSegment(fabricator.getSegment().toBuilder()
        .setWaveformPreroll(preroll).build());
      fabricator.done();

    } catch (Exception e) {
      throw new NexusException(
        String.format("Failed to do %s-type MasterDub for segment #%s",
          type, fabricator.getSegment().getId()), e);
    }
  }

  /**
   @throws Exception if failed to stream data of item of cache
   */
  @Trace(resourceName = "nexus/dub/master", operationName = "doMixerSourceLoading")
  private void doMixerSourceLoading() throws Exception {
    for (InstrumentAudio audio : fabricator.getPickedAudios()) {
      String key = audio.getWaveformKey();
      if (Strings.isNullOrEmpty(key)) continue;

      if (!mixer().hasLoadedSource(audio.getId())) try {
        mixer().loadSource(audio.getId(), dubAudioCache.get(key));

      } catch (Exception e) {
        dubAudioCache.refresh(key);
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
  @Trace(resourceName = "nexus/dub/master", operationName = "computePreroll")
  private double computePreroll() {
    double maxPreroll = 0.0;
    for (SegmentChoiceArrangementPick pick : fabricator.getPicks())
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
  @Trace(resourceName = "nexus/dub/master", operationName = "doMixerTargetSetting")
  private void doMixerTargetSetting(Double preroll) {
    for (SegmentChoiceArrangementPick pick : fabricator.getPicks())
      try {
        setupTarget(preroll, pick);
      } catch (Exception e) {
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
  }

  /**
   Set playback for a pick
   <p>
   [#341] Dub process takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter
   [#165799913] Dubbed audio can begin before segment start
   - During dub work, output audio includes the head start, and `waveform_preroll` value is persisted to segment
   [#171224848] Duration of events should include segment preroll

   @param preroll (seconds)
   @param pick    to set playback for
   */
  @Trace(resourceName = "nexus/dub/master", operationName = "setupTarget")
  private void setupTarget(Double preroll, SegmentChoiceArrangementPick pick) throws Exception {
    mixer().put(pick.getInstrumentAudioId(),
      toMicros(preroll + pick.getStart() - computeOffsetStart(pick)),
      toMicros(preroll + pick.getStart() + pick.getLength()) +
        fabricator.getChainConfig().getMixerSampleReleaseMicros(),
      fabricator.getChainConfig().getMixerSampleAttackMicros(),
      fabricator.getChainConfig().getMixerSampleReleaseMicros(),
      pick.getAmplitude() * fabricator.getAudioVolume(pick) * fabricator.getAmplitudeForInstrumentType(pick),
      0);
  }

  /**
   Compute the offset start for a pick, and cache results

   @param pick to get offset start for
   @return offset start, or cached result
   */
  @Trace(resourceName = "nexus/dub/master", operationName = "computeOffsetStart")
  private Double computeOffsetStart(SegmentChoiceArrangementPick pick) throws NexusException {
    if (!pickOffsetStart.containsKey(pick.getId()))
      pickOffsetStart.put(pick.getId(),
        fabricator.getSourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
          .orElseThrow(() -> new NexusException("compute offset start"))
          .getStart());
    return pickOffsetStart.get(pick.getId());
  }

  /**
   MasterDub implements Mixer module to mix final output to waveform streamed directly to Amazon S3@param preroll
   */
  @Trace(resourceName = "nexus/dub/master", operationName = "doMix")
  private void doMix() throws Exception {
    float quality = (float) fabricator.getChainConfig().getOutputEncodingQuality();
    mixer().mixToFile(OutputEncoder.parse(fabricator.getChainConfig().getOutputContainer()), fabricator.getFullQualityAudioOutputFilePath(), quality);
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  private Mixer mixer() throws Exception {
    if (Objects.isNull(mixer)) {
      MixerConfig config = new MixerConfig(fabricator.getOutputAudioFormat())
        .setLogPrefix(String.format("[segId=%s] ", fabricator.getSegment().getId()))
        .setNormalizationMax(fabricator.getChainConfig().getMixerNormalizationMax())
        .setDSPBufferSize(fabricator.getChainConfig().getMixerDspBufferSize())
        .setCompressRatioMax(fabricator.getChainConfig().getMixerCompressRatioMax())
        .setCompressRatioMin(fabricator.getChainConfig().getMixerCompressRatioMin())
        .setHighpassThresholdHz(fabricator.getChainConfig().getMixerHighpassThresholdHz())
        .setLowpassThresholdHz(fabricator.getChainConfig().getMixerLowpassThresholdHz())
        .setCompressToAmplitude(fabricator.getChainConfig().getMixerCompressToAmplitude())
        .setCompressAheadSeconds(fabricator.getChainConfig().getMixerCompressAheadSeconds())
        .setCompressDecaySeconds(fabricator.getChainConfig().getMixerCompressDecaySeconds());

      mixer = mixerFactory.createMixer(config);
    }

    return mixer;
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
    try {
      if (warnings.isEmpty()) return;

      StringBuilder body = new StringBuilder("MasterDub had warnings:");
      for (String warning : warnings) {
        body.append(String.format("%n%n%s", warning));
      }

      fabricator.add(SegmentMessage.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setType(SegmentMessage.Type.Warning)
        .setBody(body.toString())
        .build());
    } catch (Exception e1) {
      log.warn("Failed to create SegmentMessage", e1);
    }
  }

}
