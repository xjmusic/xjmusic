// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.audio.AudioMathUtils;
import io.xj.nexus.mixer.ActiveAudio;
import io.xj.nexus.mixer.BytePipeline;
import io.xj.nexus.mixer.Mixer;
import io.xj.nexus.mixer.MixerConfig;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class DubWorkImpl implements DubWork {
  private static final Logger LOG = LoggerFactory.getLogger(DubWorkImpl.class);
  private static final int BITS_PER_BYTE = 8;
  private static final String TIMER_SECTION_DUB = "Dub";
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final Telemetry telemetry;
  private final CraftWork craftWork;
  private final MixerFactory mixerFactory;
  private final int mixerLengthSeconds;
  private final long mixerLengthMicros;
  private final int outputChannels;
  private final double outputFrameRate;
  private final long dubAheadMicros;
  private final Float mixerOutputMicrosecondsPerByte;
  private final Mixer mixer;
  private final TemplateConfig templateConfig;

  private long atChainMicros; // dubbing is done up to this point


  // Intensity override is null if no override, or a value between 0 and 1
  private final AtomicReference<Double> intensityOverride = new AtomicReference<>(null);

  // Next/Prev intensity updated each segment, either from segment or from override
  private final AtomicReference<Double> nextIntensity = new AtomicReference<>(1.0);
  private final AtomicReference<Double> prevIntensity = new AtomicReference<>(1.0);

  public DubWorkImpl(
      Telemetry telemetry,
      CraftWork craftWork,
      MixerFactory mixerFactory,
      int mixerSeconds,
      int dubAheadSeconds,
      double outputFrameRate,
      int outputChannels
  ) {
    this.telemetry = telemetry;
    this.craftWork = craftWork;
    this.mixerLengthSeconds = mixerSeconds;
    this.outputFrameRate = outputFrameRate;
    this.outputChannels = outputChannels;
    this.mixerFactory = mixerFactory;

    dubAheadMicros = dubAheadSeconds * MICROS_PER_SECOND;
    mixerLengthMicros = mixerLengthSeconds * MICROS_PER_SECOND;

    templateConfig = craftWork.getTemplateConfig();
    var chain = craftWork.getChain();
    if (chain.isEmpty()) {
      throw new RuntimeException("Cannot initialize DubWork without TemplateConfig and Chain");
    }
    try {
      mixer = mixerInit(templateConfig);
      mixerOutputMicrosecondsPerByte = MICROS_PER_SECOND / (mixer.getAudioFormat().getFrameSize() * mixer.getAudioFormat().getFrameRate());
    } catch (Exception e) {
      didFailWhile("initializing mixer", e);
      throw new RuntimeException(e);
    }

    atChainMicros = 0;

    running.set(true);
  }

  @Override
  public void finish() {
    if (!running.get()) return;
    running.set(false);
    craftWork.finish();
    LOG.info("Finished");
  }

  @Override
  public void runCycle(long shippedToChainMicros) {
    if (!running.get()) return;

    // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
    // live performance modulation https://www.pivotaltracker.com/story/show/186003440
    if (!craftWork.isReady()) {
      LOG.debug("Waiting for Craft readiness...");
      return;
    }

    if (craftWork.isFinished()) {
      LOG.info("Craft is finished. Dub will finish.");
      finish();
      return;
    }

    if (atChainMicros >= shippedToChainMicros + dubAheadMicros) {
      LOG.debug("Waiting to catch up with dub-ahead");
      return;
    }

    // Action based on state and mode
    try {
      long startedAtMillis = System.currentTimeMillis();
      doDubFrame();
      telemetry.record(TIMER_SECTION_DUB, System.currentTimeMillis() - startedAtMillis);

    } catch (
        Exception e) {
      didFailWhile("running dub work", e);
    }
  }

  @Override
  public boolean isFinished() {
    return !running.get();
  }

  @Override
  @Nullable
  public Optional<BytePipeline> getMixerBuffer() {
    return Objects.nonNull(mixer) ? Optional.of(mixer.getBuffer()) : Optional.empty();
  }

  @Override
  public int getMixerBufferAvailableBytesCount() throws IOException {
    return Objects.nonNull(mixer) ? mixer.getBuffer().getAvailableByteCount() : 0;
  }

  @Override
  public Optional<AudioFormat> getAudioFormat() {
    return Objects.nonNull(mixer) ? Optional.of(mixer.getAudioFormat()) : Optional.empty();
  }

  @Override
  public Float getMixerOutputMicrosPerByte() {
    return mixerOutputMicrosecondsPerByte;
  }

  @Override
  public Optional<Chain> getChain() {
    return craftWork.getChain();
  }

  @Override
  public Optional<Segment> getSegmentAtChainMicros(long atChainMicros) {
    return craftWork.getSegmentAtChainMicros(atChainMicros);
  }

  @Override
  public Optional<Segment> getSegmentAtOffset(int offset) {
    return craftWork.getSegmentAtOffset(offset);
  }

  @Override
  public Optional<Program> getMainProgram(Segment segment) {
    return craftWork.getMainProgram(segment);
  }

  @Override
  public Optional<Program> getMacroProgram(Segment segment) {
    return craftWork.getMacroProgram(segment);
  }

  @Override
  public Optional<Long> getDubbedToChainMicros() {
    return Optional.of(atChainMicros);
  }

  @Override
  public int getMixerLengthSeconds() {
    return mixerLengthSeconds;
  }

  @Override
  public void setIntensityOverride(@Nullable Double intensity) {
    this.intensityOverride.set(intensity);
  }

  /**
   Do dub frame
   <p>
   instead of mixing to file, mix to memory (produce to a BytePipeline) and let ship work consume the buffer
   use the same mixer from chunk to chunk, only changing the active audios
   <p>
   Ensure mixer has continuity of its processes/effects, e.g. the compressor levels at the last frame of the last chunk are carried over to the first frame of the next chunk
   */
  void doDubFrame() {
    if (Objects.isNull(mixer)) return;

    var toChainMicros = atChainMicros + mixerLengthMicros;
    List<Segment> segments = craftWork.getSegmentsIfReady(atChainMicros, toChainMicros);
    Map<Integer, Segment> segmentById = segments.stream().collect(Collectors.toMap(Segment::getId, segment -> segment));
    if (segments.isEmpty()) {
      LOG.debug("Waiting for segments");
      return;
    }

    if (Objects.nonNull(intensityOverride.get())) {
      nextIntensity.set(intensityOverride.get());
    } else {
      nextIntensity.set(segments.get(0).getIntensity());
    }

    InstrumentAudio audio;
    long transientMicros;
    long startAtMixerMicros;
    @Nullable Long lengthMicros;
    @Nullable Long stopAtMixerMicros;
    try {
      List<SegmentChoiceArrangementPick> picks =
          craftWork.getPicks(segments).stream().filter(pick -> !craftWork.isMuted(pick)).toList();
      List<ActiveAudio> activeAudios = new ArrayList<>();
      for (SegmentChoiceArrangementPick pick : picks) {
        audio = craftWork.getInstrumentAudio(pick);
        if (StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
          continue;
        }
        transientMicros = Objects.nonNull(audio.getTransientSeconds()) ? (long) (audio.getTransientSeconds() * MICROS_PER_SECOND) : 0; // audio transient microseconds (to start audio before picked time)
        lengthMicros = Objects.nonNull(pick.getLengthMicros()) ? pick.getLengthMicros() : null; // pick length microseconds, or empty if infinite
        startAtMixerMicros =
            segmentById.get(pick.getSegmentId())
                .getBeginAtChainMicros() // segment begin at chain microseconds
                + pick.getStartAtSegmentMicros()  // plus pick start microseconds
                - transientMicros // minus transient microseconds
                - atChainMicros; // relative to beginning of this chunk
        stopAtMixerMicros =
            Objects.nonNull(lengthMicros) ?
                startAtMixerMicros // from start of this active audio
                    + transientMicros // revert transient microseconds from previous computation
                    + lengthMicros
                : null; // add length of pick in microseconds
        if (startAtMixerMicros <= mixerLengthMicros && (Objects.isNull(stopAtMixerMicros) || stopAtMixerMicros >= 0)) {
          var instrument = craftWork.getInstrument(audio);
          activeAudios.add(new ActiveAudio(
              pick,
              instrument,
              audio,
              startAtMixerMicros,
              Objects.nonNull(stopAtMixerMicros) ? stopAtMixerMicros : null,
              AudioMathUtils.computeIntensityAmplitude(
                  audio,
                  templateConfig.getIntensityLayers(instrument.getType()),
                  templateConfig.getIntensityThreshold(instrument.getType()),
                  false, prevIntensity.get()
              ),
              AudioMathUtils.computeIntensityAmplitude(
                  audio,
                  templateConfig.getIntensityLayers(instrument.getType()),
                  templateConfig.getIntensityThreshold(instrument.getType()),
                  false, nextIntensity.get()
              )
          ));
        }
      }
      prevIntensity.set(nextIntensity.get());

      try {
        mixer.mix(activeAudios, nextIntensity.get());
      } catch (IOException e) {
        LOG.warn("Cannot send to output because BytePipeline {}", e.getMessage());
        return;
      }

      atChainMicros = toChainMicros;
      LOG.debug("Dubbed to {}", String.format("%.1f", toChainMicros / (double) MICROS_PER_SECOND));

    } catch (Exception e) {
      didFailWhile("dubbing frame", e);
    }
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  Mixer mixerInit(TemplateConfig templateConfig) throws Exception {
    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    int sampleBits = FIXED_SAMPLE_BITS;
    int frameSize = outputChannels * sampleBits / BITS_PER_BYTE;
    AudioFormat audioFormat = new AudioFormat(encoding, (float) outputFrameRate, sampleBits, outputChannels, frameSize, (float) outputFrameRate, false);
    MixerConfig config = new MixerConfig(audioFormat)
        .setTotalSeconds(mixerLengthSeconds)
        .setTotalBuses(InstrumentType.values().length)
        .setCompressAheadSeconds((float) templateConfig.getMixerCompressAheadSeconds())
        .setCompressDecaySeconds((float) templateConfig.getMixerCompressDecaySeconds())
        .setCompressRatioMax((float) templateConfig.getMixerCompressRatioMax())
        .setCompressRatioMin((float) templateConfig.getMixerCompressRatioMin())
        .setCompressToAmplitude((float) templateConfig.getMixerCompressToAmplitude())
        .setDSPBufferSize(templateConfig.getMixerDspBufferSize())
        .setHighpassThresholdHz((float) templateConfig.getMixerHighpassThresholdHz())
        .setLowpassThresholdHz((float) templateConfig.getMixerLowpassThresholdHz())
        .setNormalizationBoostThreshold((float) templateConfig.getMixerNormalizationBoostThreshold())
        .setNormalizationCeiling((float) templateConfig.getMixerNormalizationCeiling());

    var M = mixerFactory.createMixer(config);
    LOG.info("Created mixer with config {}", config);
    for (var instrumentType : InstrumentType.values())
      M.setBusLevel(M.getBusNumber(instrumentType), (float) templateConfig.getDubMasterVolume(instrumentType));

    return M;
  }

  /**
   Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
    LOG.error("Failed while {} because {}", msgWhile, msgCause, e);

    running.set(false);
    finish();
  }
//
}
