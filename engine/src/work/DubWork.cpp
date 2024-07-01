// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#include "xjmusic/work/DubWork.h"
#include "xjmusic/util/ValueUtils.h"

using namespace XJ;

  DubWork::DubWork(
      CraftWork craftWork,
      int mixerSeconds,
      int dubAheadSeconds,
      double outputFrameRate,
      int outputChannels
  ) {
    this->craftWork = craftWork;

    dubAheadMicros = dubAheadSeconds * ValueUtils::MICROS_PER_SECOND;

    templateConfig = craftWork.getTemplateConfig();
    auto chain = craftWork.getChain();
    if (chain.empty()) {
      throw std::exception("Cannot initialize DubWork without TemplateConfig and Chain");
    }

    atChainMicros = 0;

    running = true;
  }

  @Override
  public void finish() {
    if (!running) return;
    running = false;
    craftWork.finish();
    spdlog::info("Finished");
  }

  @Override
  public void runCycle(long shippedToChainMicros) {
    if (!running) return;

    // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
    // live performance modulation https://github.com/xjmusic/xjmusic/issues/197
    if (!craftWork.isReady()) {
      spdlog::debug("Waiting for Craft readiness...");
      return;
    }

    if (craftWork.isFinished()) {
      spdlog::info("Craft is finished. Dub will finish.");
      finish();
      return;
    }

    if (atChainMicros >= shippedToChainMicros + dubAheadMicros) {
      spdlog::debug("Waiting to catch up with dub-ahead");
      return;
    }

    // Action based on state and mode
    try {
      long startedAtMillis = System.currentTimeMillis();
      doDubFrame();
      telemetry.record(TIMER_SECTION_DUB, System.currentTimeMillis() - startedAtMillis);

    } catch (
        std::exception e) {
      didFailWhile("running dub work", e);
    }
  }

  @Override
  public boolean isFinished() {
    return !running;
  }

  @Override
  @Nullable
  public std::optional<BytePipeline> getMixerBuffer() {
    return Objects.nonNull(mixer) ? std::optional.of(mixer.getBuffer()) : std::nullopt;
  }

  @Override
  public int getMixerBufferAvailableBytesCount() throws IOException {
    return Objects.nonNull(mixer) ? mixer.getBuffer().getAvailableByteCount() : 0;
  }

  @Override
  public std::optional<AudioFormat> getAudioFormat() {
    return Objects.nonNull(mixer) ? std::optional.of(mixer.getAudioFormat()) : std::nullopt;
  }

  @Override
  public Float getMixerOutputMicrosPerByte() {
    return mixerOutputMicrosecondsPerByte;
  }

  @Override
  public std::optional<Chain> getChain() {
    return craftWork.getChain();
  }

  @Override
  public std::optional<Segment> getSegmentAtChainMicros(long atChainMicros) {
    return craftWork.getSegmentAtChainMicros(atChainMicros);
  }

  @Override
  public std::optional<Segment> getSegmentAtOffset(int offset) {
    return craftWork.getSegmentAtOffset(offset);
  }

  @Override
  public std::optional<Program> getMainProgram(Segment segment) {
    return craftWork.getMainProgram(segment);
  }

  @Override
  public std::optional<Program> getMacroProgram(Segment segment) {
    return craftWork.getMacroProgram(segment);
  }

  @Override
  public std::optional<Long> getDubbedToChainMicros() {
    return std::optional.of(atChainMicros);
  }

  @Override
  public int getMixerLengthSeconds() {
    return mixerLengthSeconds;
  }

  @Override
  public void setIntensityOverride(@Nullable Double intensity) {
    this->intensityOverride.set(intensity);
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

    auto toChainMicros = atChainMicros + mixerLengthMicros;
    List<Segment> segments = craftWork.getSegmentsIfReady(atChainMicros, toChainMicros);
    Map<Integer, Segment> segmentById = segments.stream().collect(Collectors.toMap(Segment::getId, segment -> segment));
    if (segments.empty()) {
      spdlog::debug("Waiting for segments");
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
          auto instrument = craftWork.getInstrument(audio);
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
        spdlog::warn("Cannot send to output because BytePipeline {}", e.what());
        return;
      }

      atChainMicros = toChainMicros;
      spdlog::debug("Dubbed to {}", String.format("%.1f", toChainMicros / (double) MICROS_PER_SECOND));

    } catch (std::exception e) {
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

    auto M = mixerFactory.createMixer(config);
    spdlog::info("Created mixer with config {}", config);
    for (auto instrumentType : InstrumentType.values())
      M.setBusLevel(M.getBusNumber(instrumentType), (float) templateConfig.getDubMasterVolume(instrumentType));

    return M;
  }

  /**
   Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, std::exception e) {
    auto msgCause = StringUtils.isNullOrEmpty(e.what()) ? e.getClass().getSimpleName() : e.what();
    spdlog::error("Failed while {} because {}", msgWhile, msgCause, e);

    running = false;
    finish();
  }

