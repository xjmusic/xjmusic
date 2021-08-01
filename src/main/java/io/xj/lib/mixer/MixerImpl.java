// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

class MixerImpl implements Mixer {
  private static final Logger log = LoggerFactory.getLogger(MixerImpl.class);
  private static final float MICROS_PER_SECOND = 1000000;
  private static final float NANOS_PER_SECOND = 1000 * MICROS_PER_SECOND;
  // fields: output file
  private final float microsPerFrame;
  private final float outputFrameRate;
  private final int outputChannels;
  private final int outputFrameSize;
  // fields: in-memory storage via concurrent maps
  private final Map<String, Source> sources = Maps.newConcurrentMap(); // concurrency required
  private final Map<Long, Put> readyPuts = Maps.newConcurrentMap(); // concurrency required
  private final Map<Long, Put> livePuts = Maps.newConcurrentMap(); // concurrency required
  private final Map<Long, Put> donePuts = Maps.newConcurrentMap(); // concurrency required
  // fields: mix macro and audio format
  private final MixerFactory factory;
  private final MixerConfig config;
  // fields: from mixer config
  private final double compressToAmplitude;
  private final double minRatio;
  private final double maxRatio;
  private final int framesAhead;
  private final int dspBufferSize;
  private final int framesDecay;
  private final double highpassThresholdHz;
  private final double lowpassThresholdHz;
  // field: debugging
  private boolean debugging;
  // fields: playback state-machine
  private long nextCycleFrame;
  private long cycleDurFrames = 1000;
  private MixerState state = MixerState.Ready;
  private int uniquePutId; // key for storage in map of Puts

  /**
   Instantiate a single Mix instance

   @param mixerConfig  configuration of mixer
   @param mixerFactory factory to of new mixer
   @throws MixerException on failure
   */
  @Inject
  public MixerImpl(
    @Assisted("mixerConfig") MixerConfig mixerConfig,
    MixerFactory mixerFactory
  ) throws MixerException {
    config = mixerConfig;
    factory = mixerFactory;
    compressToAmplitude = config.getCompressToAmplitude();
    dspBufferSize = config.getDSPBufferSize();
    maxRatio = config.getCompressRatioMax();
    minRatio = config.getCompressRatioMin();
    framesAhead = config.getCompressAheadFrames();
    framesDecay = config.getCompressDecayFrames();
    highpassThresholdHz = config.getHighpassThresholdHz();
    lowpassThresholdHz = config.getLowpassThresholdHz();

    try {
      outputChannels = config.getOutputFormat().getChannels();
      MathUtil.enforceMin(1, "output audio channels", outputChannels);
      MathUtil.enforceMax(2, "output audio channels", outputChannels);
      outputFrameRate = config.getOutputFormat().getFrameRate();
      outputFrameSize = config.getOutputFormat().getFrameSize();
      microsPerFrame = MICROS_PER_SECOND / outputFrameRate;

      log.debug(config.getLogPrefix() +
          "Did initialize mixer with " +
          "outputChannels: {}, " +
          "outputFrameRate: {}, " +
          "outputFrameSize: {}, " +
          "microsPerFrame: {}, " +
          "totalSeconds: {}, " +
          "totalFrames: {}, " +
          "totalBytes: {}",
        outputChannels,
        outputFrameRate,
        outputFrameSize,
        microsPerFrame);

    } catch (Exception e) {
      throw new MixerException(config.getLogPrefix() + "unable to setup internal variables from output audio format", e);
    }
  }

  @Override
  public void put(String sourceId, long startAtMicros, long stopAtMicros, long attackMicros, long releaseMicros, double velocity, double pan) throws PutException {
    readyPuts.put(nextPutId(), factory.createPut(sourceId, startAtMicros, stopAtMicros, attackMicros, releaseMicros, velocity, pan));
  }

  @Override
  public void loadSource(String sourceId, BufferedInputStream inputStream) throws SourceException, FormatException, IOException {
    if (sources.containsKey(sourceId)) {
      throw new SourceException(config.getLogPrefix() + "Already loaded source id '" + sourceId + "'");
    }

    Source source = factory.createSource(sourceId, inputStream);
    sources.put(sourceId, source);
  }

  @Override
  public void mixToFile(OutputEncoder outputEncoder, String outputFilePath, Float quality) throws Exception {
    double totalSeconds = readyPuts.values().stream()
      .map(Put::getStopAtMicros)
      .max(Long::compare)
      .orElse(0L) / MICROS_PER_SECOND;
    int totalFrames = (int) Math.floor(totalSeconds * outputFrameRate);
    int totalBytes = totalFrames * outputFrameSize;
    double[][] buf = new double[totalFrames][outputChannels];

    if (!Objects.equals(state, MixerState.Ready))
      throw new MixerException(config.getLogPrefix() + "can't mix again; only one mix allowed per Mixer");

    // start the mixer
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    var numInstances = readyPuts.size();
    var numSources = sources.size();
    log.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} instances of {} sources",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      readyPuts.size(),
      numInstances,
      numSources);

    // Start with original sources summed up verbatim
    applySources(buf);

    // The dynamic range is forced into gentle logarithmic decay.
    // This alters the relative amplitudes from the previous step, implicitly normalizing them well below amplitude 1.0
    applyLogarithmicDynamicRange(buf);

    // The low-pass filter ensures there are no screeching extra-high tones in the mix.
    // The high-pass filter ensures there are no distorting ultra-low tones in the mix.
    applyBandpass(buf);

    // Compression is more predictable within the logarithmic range
    // FUTURE: multi-band compressor, but for now skip: applyCompressor(buf);

    // Final step ensures the broadcast signal has an exact constant maximum amplitude
    applyNormalization(buf);

    //
    state = MixerState.Done;
    log.debug(config.getLogPrefix() + "Did mix {} seconds of output audio at {} Hz from {} instances of {} sources in {}s",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      numInstances,
      numSources,
      String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    startedAt = System.nanoTime();
    log.debug(config.getLogPrefix() + "Will write {} bytes of output audio", totalBytes);
    new AudioStreamWriter(buf, quality).writeToFile(outputFilePath, config.getOutputFormat(), outputEncoder, totalFrames);
    log.debug(config.getLogPrefix() + "Did write {} OK in {}s", outputFilePath, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));
  }

  @Override
  public String toString() {
    return config.getLogPrefix() + "{ " +
      "outputChannels:" + outputChannels + ", " +
      "outputFrameRate:" + outputFrameRate + ", " +
      "outputFrameSize:" + outputFrameSize + ", " +
      "microsPerFrame:" + microsPerFrame + ", " +
      "microsPerFrame:" + microsPerFrame +
      " }";
  }

  @Override
  public void setCycleMicros(long micros) throws MixerException {
    if (0 == microsPerFrame) {
      throw new MixerException(config.getLogPrefix() + "Must specify mixing frequency before setting cycle duration!");
    }
    cycleDurFrames = (long) Math.floor(micros / microsPerFrame);
  }

  @Override
  public int getSourceCount() {
    return sources.size();
  }

  @Override
  public int getPutCount() {
    return readyPuts.size() + livePuts.size();
  }

  @Override
  public int getPutReadyCount() {
    return readyPuts.size();
  }

  @Override
  public int getPutLiveCount() {
    return livePuts.size();
  }

  @Override
  public int getPutDoneCount() {
    return donePuts.size();
  }

  @Override
  public MixerState getState() {
    return state;
  }

  @Override
  public float getFrameRate() {
    return outputFrameRate;
  }

  @Override
  public AudioFormat getOutputFormat() {
    return config.getOutputFormat();
  }

  @Override
  public boolean hasLoadedSource(String sourceId) {
    return sources.containsKey(sourceId);
  }

  @Override
  public boolean isDebugging() {
    return debugging;
  }

  @Override
  public void setDebugging(boolean debugging) {
    this.debugging = debugging;
  }

  /**
   apply original sources to mixing buffer
   addition of all sources into initial mixed source frames@param buf
   */
  private void applySources(double[][] buf) {
    for (int i = 0; i < buf.length; i++)
      buf[i] = mixSourceFrame(i);
  }

  /**
   apply compressor to mixing buffer
   <p>
   [#154112129] lookahead-attack compressor compresses entire buffer towards target amplitude
   <p>
   only each major cycle, compute the new target compression ratio,
   but modify the compression ratio *every* frame for max smoothness
   <p>
   compression target uses a rate of change of rate of change
   to maintain inertia over time, required to preserve audio signal
   */
  private void applyCompressor(double[][] buf) {
    //
    double compRatio = computeCompressorTarget(buf, 0, framesAhead);
    double compRatioDelta = 0; // rate of change
    double targetCompRatio = compRatio;
    for (int i = 0; i < buf.length; i++) {
      if (0 == i % dspBufferSize) {
        targetCompRatio = computeCompressorTarget(buf, i, i + framesAhead);
      }
      double compRatioDeltaDelta = MathUtil.delta(compRatio, targetCompRatio) / framesDecay;
      compRatioDelta += MathUtil.delta(compRatioDelta, compRatioDeltaDelta) / dspBufferSize;
      compRatio += compRatioDelta;
      for (int k = 0; k < outputChannels; k++)
        buf[i][k] *= compRatio;
    }
  }

  /**
   [#161670248] Engineer wants high-pass and low-pass filters with gradual thresholds, in order to be optimally heard but not listened to.
   The lowpass filter ensures there are no screeching extra-high tones in the mix.
   The highpass filter ensures there are no distorting ultra-low tones in the mix.
   */
  private void applyBandpass(double[][] buf) throws MixerException {
    FrequencyRangeLimiter.filter(buf, outputFrameRate, dspBufferSize, (float) highpassThresholdHz, (float) lowpassThresholdHz);
  }

  /**
   apply logarithmic dynamic range to mixing buffer
   */
  private void applyLogarithmicDynamicRange(double[][] buf) {
    for (int i = 0; i < buf.length; i++)
      buf[i] = MathUtil.logarithmicCompression(buf[i]);
  }

  /**
   apply normalization to mixing buffer
   [#154112129] normalize final buffer to normalization threshold@param buf
   */
  private void applyNormalization(double[][] buf) {
    double normRatio = config.getNormalizationMax() / MathUtil.maxAbs(buf);
    for (int i = 0; i < buf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        buf[i][k] *= normRatio;
  }

  /**
   Compute the target amplitude for the compressor
   [#154112129] lookahead-attack compressor compresses entire buffer towards target amplitude

   @return target amplitude
   */
  private double computeCompressorTarget(double[][] input, int iFr, int iTo) {
    double currentAmplitude = MathUtil.maxAbs(input, iFr, iTo);
    return MathUtil.limit(minRatio, maxRatio, compressToAmplitude / currentAmplitude);
  }

  /**
   mix the 64-bit floating-point sample values for the next frame across all output channels.
   <p>
   the Put only has a reference to the source--
   so the Mixer has to use that reference source id along with other variables from the Put,
   in order to arrive at the final source output value at any given microsecond

   @param offsetFrame of frame from start of mix
   @return array of samples (one per channel) constituting a frame of audio
   */
  private double[] mixSourceFrame(long offsetFrame) {
    mixCycleBeforeEveryNthFrame(offsetFrame);
    long atMicros = getMicros(offsetFrame);

    double[] frame = new double[outputChannels];
    livePuts.forEach((id, livePut) -> {
      long sourceOffsetMicros = livePut.sourceOffsetMicros(atMicros);
      if (0 < sourceOffsetMicros) {
        double[] inSamples = mixSourceFrameAtMicros(livePut.getSourceId(), livePut.getVelocity(), livePut.getPan(), sourceOffsetMicros);
        double envelope = livePut.envelope(atMicros);
        for (int i = 0; i < outputChannels; i++)
          frame[i] += inSamples[i] * envelope;
      }
    });

    return frame;
  }

  /**
   mix a particular source frame to the output specifications, including volume & pan

   @param sourceId of source
   @param volume   to mix output to
   @param pan      to mix output to
   @param atMicros at which to get source frame
   @return mixed source frame
   */
  private double[] mixSourceFrameAtMicros(String sourceId, double volume, double pan, long atMicros) {
    Source source = sources.get(sourceId);
    if (null == source)
      return new double[outputChannels];

    return source.frameAt(atMicros, volume, pan, outputChannels);
  }

  /**
   THE "MIX CYCLE"
   <p>
   move puts from ready -> live -> done
   <p>
   get rid of sources not used by ready/live puts

   @param frameOffset of frame from start of mix
   */
  private void mixCycleBeforeEveryNthFrame(long frameOffset) {
    if (frameOffset < nextCycleFrame) {
      return;
    }
    long offsetMicros = getMicros(frameOffset);

    // for garbage collection of unused sources:
    Map<String, Boolean> sourceUsage = buildMapAllSourceIdToFalse();

    // iterate through ready Puts
    readyPuts.forEach((putId, readyPut) -> {

      // keep track of sources in-use by ready Puts
      sourceUsage.put(readyPut.getSourceId(), true);

      // if a put is near-to-playback, move it to the live fire queue
      // double a mix cycle is considered near-playback enough to move a put from "ready" to "live"
      if (readyPut.getStartAtMicros() < offsetMicros + cycleDurFrames * microsPerFrame * 2) {
        readyPuts.remove(putId);
        livePuts.put(putId, readyPut);
      }
    });

    // iterate through live Puts
    livePuts.forEach((putId, livePut) -> {

      // keep track of sources in-use by live Puts
      sourceUsage.put(livePut.getSourceId(), true);

      // if a put is no longer alive, move it to the done queue
      if (!livePut.isAlive()) {
        livePuts.remove(putId);
        donePuts.put(putId, livePut);
      }

    });

    // iterate through not-used sources and destroy them
    sourceUsage.forEach((sourceId, used) -> {
      if (!used) {
        sources.remove(sourceId);
      }
    });

    // advance to next cycle
    nextCycleFrame = frameOffset + cycleDurFrames;

    // if debug mode
    if (debugging && 0 < getSourceCount()) {
      log.debug(config.getLogPrefix() + "mix [{}ns] puts-ready:{} puts-live:{} sources:{}", offsetMicros, getPutReadyCount(), getPutLiveCount(), getSourceCount());
    }
  }

  /**
   Get microsecond value of a frame offset

   @param frameOffset offset
   @return microseconds
   */
  private long getMicros(long frameOffset) {
    return (long) Math.floor(MICROS_PER_SECOND * frameOffset / outputFrameRate);
  }

  /**
   build a map of all source id to boolean value

   @return map
   */
  private Map<String, Boolean> buildMapAllSourceIdToFalse() {
    Map<String, Boolean> result = Maps.newHashMap();
    sources.keySet().forEach((sourceId) -> result.put(sourceId, false));
    return result;
  }

  /**
   generate unique ids for storage of Puts

   @return next unique put id
   */
  private long nextPutId() {
    uniquePutId++;
    return uniquePutId;
  }

}


