// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

class MixerImpl implements Mixer {
  private static final Logger log = LoggerFactory.getLogger(MixerImpl.class);
  private static final float MICROS_PER_SECOND = 1000000;
  private static final float NANOS_PER_SECOND = 1000 * MICROS_PER_SECOND;
  private static final int NORMALIZATION_GRAIN = 20;
  private static final int COMPRESSION_GRAIN = 20;
  // fields: output file
  private final float microsPerFrame;
  private final float outputFrameRate;
  private final int outputChannels;
  private final int outputFrameSize;
  // fields: in-memory storage via concurrent maps
  private final Map<String, Source> sources = Maps.newConcurrentMap(); // concurrency required
  private final Map<Long, Put> puts = Maps.newConcurrentMap(); // concurrency required
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
  // fields: playback state-machine
  private MixerState state = MixerState.Ready;
  private int uniquePutId; // key for storage in map of Puts
  private final List<String> busIds = Lists.newArrayList();
  private final Map<String, Double> busLevel = Maps.newHashMap();

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
  public void put(String busId, String sourceId, long startAtMicros, long stopAtMicros, double velocity) throws PutException {
    puts.put(nextPutId(), factory.createPut(getBusNumber(busId), sourceId, startAtMicros, stopAtMicros, velocity));
  }

  @Override
  public void setBusLevel(String busId, double level) {
    busLevel.put(busId, level);
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
    double totalSeconds = puts.values().stream()
      .map(Put::getStopAtMicros)
      .max(Long::compare)
      .orElse(0L) / MICROS_PER_SECOND;
    int totalFrames = (int) Math.floor(totalSeconds * outputFrameRate);
    int totalBytes = totalFrames * outputFrameSize;
    double[][][] busBuf = new double[busIds.size()][totalFrames][outputChannels];
    double[][] outBuf = new double[totalFrames][outputChannels];

    if (!Objects.equals(state, MixerState.Ready))
      throw new MixerException(config.getLogPrefix() + "can't mix again; only one mix allowed per Mixer");

    // start the mixer
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    var numInstances = puts.size();
    var numSources = sources.size();
    log.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} instances of {} sources",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      puts.size(),
      numInstances,
      numSources);

    // Start with original sources summed up verbatim
    // Initial mix steps are done on individual busses
    // Multi-bus output with individual normalization REF https://www.pivotaltracker.com/story/show/179081795
    applySources(busBuf);
    for (int b = 0; b < busIds.size(); b++)
      applyCompressor(outBuf);
    mixOutputBus(busBuf, outBuf);

    // The dynamic range is forced into gentle logarithmic decay.
    // This alters the relative amplitudes from the previous step, implicitly normalizing them well below amplitude 1.0
    applyLogarithmicDynamicRange(outBuf);

    // Compression is more predictable within the logarithmic range
    applyCompressor(outBuf);

    // Final step ensures the broadcast signal has an exact constant maximum amplitude
    applyNormalization(outBuf);

    //
    state = MixerState.Done;
    log.debug(config.getLogPrefix() + "Did mix {} seconds of output audio at {} Hz from {} instances of {} sources in {}s",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      numInstances,
      numSources,
      String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    if (0 == outBuf.length)
      throw new MixerException(config.getLogPrefix() + "Output buffer is empty!");

    startedAt = System.nanoTime();
    log.debug(config.getLogPrefix() + "Will write {} bytes of output audio", totalBytes);
    new AudioStreamWriter(outBuf, quality).writeToFile(outputFilePath, config.getOutputFormat(), outputEncoder, totalFrames);
    log.debug(config.getLogPrefix() + "Did write {} OK in {}s", outputFilePath, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));
  }

  /**
   Mix input buffers to output buffer@param inBufs buffers from which to read input

   @param outBuf to which summed output will be written
   */
  private void mixOutputBus(final double[][][] inBufs, double[][] outBuf) {
    double[] level = busIds.stream().mapToDouble(b -> busLevel.getOrDefault(b, 1.0)).toArray();
    IntStream.range(0, inBufs.length).forEach(b ->
      IntStream.range(0, inBufs[0].length).forEach(f ->
        IntStream.range(0, inBufs[0][0].length).forEach(c ->
          outBuf[f][c] += inBufs[b][f][c] * level[b]
        )));
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
  public int getSourceCount() {
    return sources.size();
  }

  @Override
  public MixerState getState() {
    return state;
  }

  @Override
  public boolean hasLoadedSource(String sourceId) {
    return sources.containsKey(sourceId);
  }

  /**
   apply original sources to mixing buffer
   addition of all sources into initial mixed source frames
   <p>
   mix the 64-bit floating-point sample values for the next frame across all output channels.
   <p>
   the Put only has a reference to the source--
   so the Mixer has to use that reference source id along with other variables from the Put,
   in order to arrive at the final source output value at any given microsecond

   @param busBuf is array[bus][frame][channel]
   */
  private void applySources(double[][][] busBuf) {
    puts.values().forEach(put -> {
      int from = (int) (put.getStartAtMicros() / microsPerFrame);
      var span = (int) (put.getStopAtMicros() / microsPerFrame) - from;
      IntStream.range(0, span).forEach(f ->
        IntStream.range(0, busBuf[0][0].length).forEach(c -> {
          if (from + f < busBuf[0].length)
            busBuf[put.getBus()][from + f][c] +=
              Envelope.at(Math.min(f, span - f),
                sources.get(put.getSourceId()).getValue((long) (f * microsPerFrame), c) * put.getVelocity());
        }));
    });
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

  /*
   [#161670248] Engineer wants high-pass and low-pass filters with gradual thresholds, in order to be optimally heard but not listened to.
   The lowpass filter ensures there are no screeching extra-high tones in the mix.
   The highpass filter ensures there are no distorting ultra-low tones in the mix.
   *
  private void applyBandpass(double[][] buf) throws MixerException {
    FrequencyRangeLimiter.filter(buf, outputFrameRate, dspBufferSize, (float) highpassThresholdHz, (float) lowpassThresholdHz);
  }
   */

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
    double normRatio = config.getNormalizationMax() / MathUtil.maxAbs(buf, NORMALIZATION_GRAIN);
    for (int i = 0; i < buf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        buf[i][k] *= normRatio;
  }

  /**
   [#154112129] lookahead-attack compressor compresses entire buffer towards target amplitude

   @return target amplitude
   */
  private double computeCompressorTarget(double[][] input, int iFr, int iTo) {
    double currentAmplitude = MathUtil.maxAbs(input, iFr, iTo, COMPRESSION_GRAIN);
    return MathUtil.limit(minRatio, maxRatio, compressToAmplitude / currentAmplitude);
  }

  /**
   generate unique ids for storage of Puts

   @return next unique put id
   */
  private long nextPutId() {
    uniquePutId++;
    return uniquePutId;
  }

  /**
   Each new bus ID maps to a number

   @param busId to get number for
   @return number of bus id
   */
  private int getBusNumber(String busId) {
    if (!busIds.contains(busId))
      busIds.add(busId);
    return busIds.indexOf(busId);
  }
}


