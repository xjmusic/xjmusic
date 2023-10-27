// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.mixer.*;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.nexus.NexusException;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.mixer.ActiveAudio;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class DubWorkImpl implements DubWork {
  static final Logger LOG = LoggerFactory.getLogger(DubWorkImpl.class);
  static final long MICROS_PER_SECOND = 1000000L;
  static final int BITS_PER_BYTE = 8;


  @Nullable
  Mixer mixer;
  final MultiStopwatch timer;
  final AtomicBoolean running = new AtomicBoolean(true);
  final CraftWork craftWork;
  final DubAudioCache dubAudioCache;
  final Map<InstrumentType, Integer> instrumentBusNumber = new ConcurrentHashMap<>();
  final Map<UUID, ActiveAudio> mixerActiveAudio = new ConcurrentHashMap<>();
  final MixerFactory mixerFactory;
  final int mixerLengthSeconds;
  final long cycleMillis;
  final long mixerLengthMicros;
  long nowAtChainMicros = 0; // Set from downstream, for dub work to understand how far "ahead" it is
  long chunkFromChainMicros = 0; // dubbing is done up to this point
  long chunkToChainMicros = 0; // plan ahead one dub frame at a time
  long nextCycleAtSystemMillis = System.currentTimeMillis();
  @Nullable
  Float mixerOutputMicrosecondsPerByte;
  final int outputChannels;
  final double outputFrameRate;
  final int dubAheadSeconds;
  final String audioBaseUrl;
  final String contentStoragePathPrefix;

  public DubWorkImpl(
    CraftWork craftWork,
    DubAudioCache dubAudioCache,
    MixerFactory mixerFactory,
    String contentStoragePathPrefix,
    String audioBaseUrl,
    int mixerSeconds,
    long cycleMillis,
    double outputFrameRate,
    int outputChannels,
    int dubAheadSeconds
  ) {
    this.craftWork = craftWork;
    this.dubAudioCache = dubAudioCache;
    this.contentStoragePathPrefix = contentStoragePathPrefix;
    this.audioBaseUrl = audioBaseUrl;
    this.mixerLengthSeconds = mixerSeconds;
    this.outputFrameRate = outputFrameRate;
    this.outputChannels = outputChannels;
    this.dubAheadSeconds = dubAheadSeconds;
    this.mixerLengthMicros = mixerLengthSeconds * MICROS_PER_SECOND;
    this.mixerFactory = mixerFactory;
    this.cycleMillis = cycleMillis;

    timer = MultiStopwatch.start();

    var templateConfig = craftWork.getTemplateConfig();
    var chain = craftWork.getChain();
    if (templateConfig.isEmpty() || chain.isEmpty()) {
      LOG.debug("Waiting for Craft to begin");
      return;
    }
    try {
      mixer = mixerInit(templateConfig.get());
    } catch (Exception e) {
      didFailWhile("initializing mixer", e);
      return;
    }

    chunkFromChainMicros = 0;
    chunkToChainMicros = 0;

    running.set(true);
  }

  @Override
  public void finish() {
    craftWork.finish();
    if (!running.get()) return;
    running.set(false);
    LOG.info("Finished");
  }

  @Override
  public void runCycle() {
    if (!running.get()) return;

    if (System.currentTimeMillis() < nextCycleAtSystemMillis) return;

    nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;

    if (craftWork.isFinished()) {
      LOG.warn("must stop since CraftWork is no longer running");
      finish();
    }

    // Action based on state and mode
    try {
      if (isPlannedAhead()) {
        doDubFrame();
      } else {
        doPlanFrame();
      }
    } catch (
      Exception e) {
      didFailWhile("running a work cycle", e);
    }

    // End lap & do telemetry on all fabricated chains
    timer.lap();
    LOG.debug("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
    nextCycleAtSystemMillis = System.currentTimeMillis() + cycleMillis;
  }

  @Override
  public boolean isPlannedAhead() {
    return chunkToChainMicros > chunkFromChainMicros;
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
  public Optional<Float> getMixerOutputMicrosPerByte() {
    if (Objects.isNull(mixerOutputMicrosecondsPerByte)) {
      if (Objects.isNull(mixer)) {
        return Optional.empty();
      }
      mixerOutputMicrosecondsPerByte = MICROS_PER_SECOND / (mixer.getAudioFormat().getFrameSize() * mixer.getAudioFormat().getFrameRate());
    }
    return Optional.of(mixerOutputMicrosecondsPerByte);
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
    return Optional.of(chunkToChainMicros);
  }

  @Override
  public void setNowAtToChainMicros(Long micros) {
    nowAtChainMicros = micros;
  }

  void doPlanFrame() {
    if (craftWork.isFinished()) {
      LOG.warn("Craft is not running; will abort.");
      finish();
      return;
    }
    if (chunkToChainMicros > nowAtChainMicros + (dubAheadSeconds * MICROS_PER_SECOND)) {
      LOG.debug("Waiting to catch up with {} second dub-ahead", dubAheadSeconds);
      return;
    }

    var butts=123;//todo remove

    chunkToChainMicros = chunkFromChainMicros + mixerLengthMicros;
    craftWork.setAtChainMicros(chunkToChainMicros);
    LOG.debug("Planned frame {}s", String.format("%.1f", chunkToChainMicros / (double) MICROS_PER_SECOND));
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
    var segments = craftWork.getSegmentsIfReady(chunkFromChainMicros, chunkToChainMicros);
    if (segments.isEmpty()) {
      LOG.debug("Waiting for segments");
      return;
    }

    try {
      var picks = craftWork.getPicks(segments);
      List<ActiveAudio> activeAudios = segments.stream().flatMap(segment ->
        picks.stream().filter(pick -> Objects.equals(pick.getSegmentId(), segment.getId())).flatMap(pick -> {
          var audio = craftWork.getInstrumentAudio(pick);
          if (craftWork.isMuted(pick)) {
            LOG.debug("Skipping muted pick {}", pick.getId());
            return Stream.empty();
          }
          if (audio.isEmpty()) {
            LOG.warn("No InstrumentAudio for SegmentChoiceArrangementPick[{}]", pick.getId());
            return Stream.empty();
          }
          long transientMicros = Objects.nonNull(audio.get().getTransientSeconds()) ? (long) (audio.get().getTransientSeconds() * MICROS_PER_SECOND) : 0; // audio transient microseconds (to start audio before picked time)
          @Nullable Long lengthMicros = Objects.nonNull(pick.getLengthMicros()) ? pick.getLengthMicros() : null; // pick length microseconds, or empty if infinite
          long startAtChainMicros =
            segment.getBeginAtChainMicros() // segment begin at chain microseconds
              + pick.getStartAtSegmentMicros()  // plus pick start microseconds
              - transientMicros // minus transient microseconds
              - chunkFromChainMicros; // relative to beginning of this chunk
          @Nullable Long stopAtMicros =
            Objects.nonNull(lengthMicros) ?
              startAtChainMicros // from start of this active audio
                + transientMicros // revert transient microseconds from previous computation
                + lengthMicros
              : null; // add length of pick in microseconds
          if (startAtChainMicros > mixerLengthMicros || (Objects.nonNull(stopAtMicros) && stopAtMicros < 0)) {
            return Stream.empty();
          }
          var instrument = craftWork.getInstrument(audio.get());
          if (instrument.isEmpty()) {
            LOG.warn("No Instrument for SegmentChoiceArrangementPick[{}] and InstrumentAudio[{}]", pick.getId(), audio.get().getId());
            return Stream.empty();
          }
          return Stream.of(new ActiveAudio(pick, instrument.get(), audio.get(), startAtChainMicros, stopAtMicros));
        })).toList();

      mixerSetAll(activeAudios);
      try {
        mixer.mix();
      } catch (IOException e) {
        LOG.debug("Cannot send to output because BytePipeline {}", e.getMessage());
        finish();
      }

      chunkFromChainMicros = chunkToChainMicros;
      LOG.debug("Dubbed to {}", String.format("%.1f", chunkToChainMicros / (double) MICROS_PER_SECOND));

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
      .setCompressAheadSeconds(templateConfig.getMixerCompressAheadSeconds())
      .setCompressDecaySeconds(templateConfig.getMixerCompressDecaySeconds())
      .setCompressRatioMax(templateConfig.getMixerCompressRatioMax())
      .setCompressRatioMin(templateConfig.getMixerCompressRatioMin())
      .setCompressToAmplitude(templateConfig.getMixerCompressToAmplitude())
      .setDSPBufferSize(templateConfig.getMixerDspBufferSize())
      .setHighpassThresholdHz(templateConfig.getMixerHighpassThresholdHz())
      .setLowpassThresholdHz(templateConfig.getMixerLowpassThresholdHz())
      .setNormalizationBoostThreshold(templateConfig.getMixerNormalizationBoostThreshold())
      .setNormalizationCeiling(templateConfig.getMixerNormalizationCeiling());

    var M = mixerFactory.createMixer(config);
    LOG.info("Created mixer with config {}", config);
    for (var instrumentType : InstrumentType.values())
      M.setBusLevel(mixerGetBusNumber(instrumentType), templateConfig.getDubMasterVolume(instrumentType));

    return M;
  }

  /**
   Mixer set all active audios, remove any that are no longer active

   @param activeAudios to set up
   */
  void mixerSetAll(List<ActiveAudio> activeAudios) {
    for (ActiveAudio active : activeAudios) {
      LOG.debug("----------> ADD @{} {} {}", (float) active.getStartAtMicros() / MICROS_PER_SECOND, active.getInstrument().getName(), active.getAudio().getName());
      mixerSetupTarget(active);
      mixerActiveAudio.put(active.getId(), active);
    }
    // garbage collect any active audios that are no longer active
    var activeIds = activeAudios.stream().map(ActiveAudio::getId).collect(Collectors.toSet());
    for (ActiveAudio active : mixerActiveAudio.values().stream().filter(aa -> !activeIds.contains(aa.getId())).toList()) {
      LOG.debug("----------> DEL {} {}", active.getInstrument().getName(), active.getAudio().getName());
      mixerRemoveTarget(active);
      mixerActiveAudio.remove(active.getId());
    }
  }

  final AtomicInteger computedBusNumber = new AtomicInteger(0);

  /**
   Set playback for a pick
   <p>
   [#341] Dub process takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter
   Dubbed audio can begin before segment start https://www.pivotaltracker.com/story/show/165799913
   - During dub work, output audio includes the head start, and `waveform_preroll` value is persisted to segment
   Duration of events should include segment preroll https://www.pivotaltracker.com/story/show/171224848

   @param active audio to setup
   */
  private void mixerSetupTarget(ActiveAudio active) {
    if (Objects.isNull(mixer)) return;
    try {
      String key = active.getAudio().getWaveformKey();
      if (StringUtils.isNullOrEmpty(key)) return;
      if (!mixer.hasLoadedSource(active.getAudio().getId())) {
        mixer.loadSource(
          active.getAudio().getId(),
          dubAudioCache.load(
            contentStoragePathPrefix,
            audioBaseUrl,
            active.getInstrument().getId(),
            key,
            (int) mixer.getAudioFormat().getFrameRate(),
            mixer.getAudioFormat().getSampleSizeInBits(),
            mixer.getAudioFormat().getChannels()),
          active.getAudio().getName()
        );
      }
      mixer.put(
        active.getId(),
        active.getAudio().getId(),
        mixerGetBusNumber(active.getInstrument().getType()),
        active.getStartAtMicros(),
        active.getStopAtMicros().orElse(active.getStartAtMicros() + mixer.getSource(active.getAudio().getId()).getLengthMicros()),
        active.getPick().getAmplitude() * active.getAudioVolume(),
        active.getAttackMillis(),
        active.getReleaseMillis());

    } catch (FormatException | NexusException | FileStoreException | SourceException | IOException | PutException e) {
      LOG.error("Failed to setup mixer target for {} because {}", active, e.getCause().getMessage());
    }
  }

  /**
   Remove a source put from the mixer

   @param active audio to remove
   */
  void mixerRemoveTarget(ActiveAudio active) {
    if (Objects.isNull(mixer)) return;
    mixer.del(active.getId());
  }

  /**
   Assign a bus number to an instrument type, in no particular order

   @param instrumentType for which to get bus number
   @return bus number
   */
  int mixerGetBusNumber(InstrumentType instrumentType) {
    if (!instrumentBusNumber.containsKey(instrumentType))
      instrumentBusNumber.put(instrumentType, computedBusNumber.getAndIncrement());
    return instrumentBusNumber.get(instrumentType);
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
