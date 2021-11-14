// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentType;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerConfig;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DubMasterImpl implements DubMaster {
  private final Logger log = LoggerFactory.getLogger(DubMasterImpl.class);
  private final Fabricator fabricator;
  private final MixerFactory mixerFactory;
  private final List<String> warnings = Lists.newArrayList();
  private final Map<UUID, Float> pickOffsetStart = Maps.newHashMap();
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
    return (long) (seconds * MICROS_PER_SECOND);
  }

  @Override
  public void doWork() throws NexusException {
    SegmentType type = null;
    try {
      type = fabricator.getType();
      doMixerSourceLoading();
      double preroll = computePreroll();
      doMixerTargetSetting(preroll);
      var postroll = doMix() - computeLengthSeconds(fabricator.getSegment()) - preroll;
      reportWarnings();
      report();

      // write updated segment with waveform preroll
      var segment =
        fabricator.getSegment()
          .waveformPreroll(preroll)
          .waveformPostroll(postroll);
      fabricator.updateSegment(segment);
      fabricator.done();

    } catch (Exception e) {
      throw new NexusException(
        String.format("Failed to do %s-type MasterDub for segment #%s",
          type, fabricator.getSegment().getId()), e);
    }
  }

  /**
   Compute the length of a segment, in seconds double floating-point

   @param segment for which to compute length between start and end
   @return length in seconds double floating-point
   */
  private double computeLengthSeconds(Segment segment) {
    return Duration.between(Instant.parse(segment.getBeginAt()), Instant.parse(segment.getEndAt())).toNanos() / NANOS_PER_SECOND;
  }

  /**
   @throws Exception if failed to stream data of item of cache
   */
  private void doMixerSourceLoading() throws Exception {
    for (InstrumentAudio audio : fabricator.getPickedAudios()) {
      String key = audio.getWaveformKey();
      if (Strings.isNullOrEmpty(key)) continue;

      if (!mixer().hasLoadedSource(audio.getId().toString()))
        mixer().loadSource(audio.getId().toString(), dubAudioCache.getAbsolutePath(key));
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
  private void setupTarget(Double preroll, SegmentChoiceArrangementPick pick) throws Exception {
    mixer().put(
      fabricator.sourceMaterial().getInstrumentTypeForAudioId(pick.getInstrumentAudioId()).toString(),
      pick.getInstrumentAudioId().toString(),
      toMicros(preroll + pick.getStart() - computeOffsetStart(pick)),
      toMicros(preroll + pick.getStart() + computeLengthSeconds(pick)),
      pick.getAmplitude() * fabricator.getAudioVolume(pick));
  }

  /**
   Compute the length of a pick

   @param pick for which to compute length
   @return length
   */
  private Double computeLengthSeconds(SegmentChoiceArrangementPick pick) {
    if (Objects.nonNull(pick.getLength())) return pick.getLength();
    return mixer.getSource(pick.getInstrumentAudioId().toString()).getLengthSeconds();
  }

  /**
   Compute the offset start for a pick, and cache results

   @param pick to get offset start for
   @return offset start, or cached result
   */
  private float computeOffsetStart(SegmentChoiceArrangementPick pick) throws NexusException {
    if (!pickOffsetStart.containsKey(pick.getId()))
      pickOffsetStart.put(pick.getId(),
        fabricator.sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
          .orElseThrow(() -> new NexusException("compute offset start"))
          .getTransientSeconds());
    return pickOffsetStart.get(pick.getId());
  }

  /**
   MasterDub implements Mixer module to mix final output to waveform streamed directly to Amazon S3

   @return postroll seconds after mixing
   */
  private double doMix() throws Exception {
    float quality = (float) fabricator.getTemplateConfig().getOutputEncodingQuality();
    return mixer().mixToFile(OutputEncoder.parse(fabricator.getTemplateConfig().getOutputContainer()), fabricator.getFullQualityAudioOutputFilePath(), quality);
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  private Mixer mixer() throws Exception {
    if (Objects.isNull(mixer)) {
      MixerConfig config = new MixerConfig(fabricator.getOutputAudioFormat())
        .setCompressAheadSeconds(fabricator.getTemplateConfig().getMixerCompressAheadSeconds())
        .setCompressDecaySeconds(fabricator.getTemplateConfig().getMixerCompressDecaySeconds())
        .setCompressRatioMax(fabricator.getTemplateConfig().getMixerCompressRatioMax())
        .setCompressRatioMin(fabricator.getTemplateConfig().getMixerCompressRatioMin())
        .setCompressToAmplitude(fabricator.getTemplateConfig().getMixerCompressToAmplitude())
        .setDSPBufferSize(fabricator.getTemplateConfig().getMixerDspBufferSize())
        .setHighpassThresholdHz(fabricator.getTemplateConfig().getMixerHighpassThresholdHz())
        .setLogPrefix(String.format("[segId=%s] ", fabricator.getSegment().getId()))
        .setLowpassThresholdHz(fabricator.getTemplateConfig().getMixerLowpassThresholdHz())
        .setNormalizationBoostThreshold(fabricator.getTemplateConfig().getMixerNormalizationBoostThreshold())
        .setNormalizationCeiling(fabricator.getTemplateConfig().getMixerNormalizationCeiling());

      mixer = mixerFactory.createMixer(config);
    }

    mixer.setBusLevel("Bass", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypeBass());
    mixer.setBusLevel("Drum", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypeDrum());
    mixer.setBusLevel("Pad", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypePad());
    mixer.setBusLevel("PercLoop", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypePercLoop());
    mixer.setBusLevel("Stab", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypeStab());
    mixer.setBusLevel("Sticky", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypeSticky());
    mixer.setBusLevel("Stripe", fabricator.getTemplateConfig().getDubMasterVolumeInstrumentTypeStripe());

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

      fabricator.addWarningMessage(body.toString());
    } catch (Exception e1) {
      log.warn("Failed to create SegmentMessage", e1);
    }
  }

}
