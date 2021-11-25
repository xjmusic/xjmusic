// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.NANOS_PER_SECOND;

class MixerImpl implements Mixer {
  private static final Logger LOG = LoggerFactory.getLogger(MixerImpl.class);
  public static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final int READ_BUFFER_BYTE_SIZE = 1024;
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
  private final double compressRatioMin;
  private final double compressRatioMax;
  private final int framesAhead;
  private final int dspBufferSize;
  private final int framesDecay;
  private final List<String> busIds = Lists.newArrayList();
  private final Map<String, Double> busLevel = Maps.newHashMap();
  // fields: playback state-machine
  private MixerState state = MixerState.Ready;
  private int uniquePutId; // key for storage in map of Puts

  /**
   Note: these buffers can't be constructed until after the sources are Put, ergo defining the total buffer length.
   */
  private double[][][] busBuf; // buffer separated into busses like [bus][frame][channel]
  private double[][] outBuf; // final output buffer like [bus][frame][channel]

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
    compressRatioMax = config.getCompressRatioMax();
    compressRatioMin = config.getCompressRatioMin();
    framesAhead = config.getCompressAheadFrames();
    framesDecay = config.getCompressDecayFrames();

    try {
      outputChannels = config.getOutputFormat().getChannels();
      MathUtil.enforceMin(1, "output audio channels", outputChannels);
      MathUtil.enforceMax(2, "output audio channels", outputChannels);
      outputFrameRate = config.getOutputFormat().getFrameRate();
      outputFrameSize = config.getOutputFormat().getFrameSize();
      microsPerFrame = (float) (MICROS_PER_SECOND / outputFrameRate);

      LOG.debug(config.getLogPrefix() +
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
  public void put(String busName, String sourceId, long startAtMicros, long stopAtMicros, double velocity) throws PutException {
    puts.put(nextPutId(), factory.createPut(getAssignedBusNumber(busName), sourceId, startAtMicros, stopAtMicros, velocity));
  }

  @Override
  public void setBusLevel(String busId, double level) {
    busLevel.put(busId, level);
  }

  @Override
  public void loadSource(String sourceId, String pathToFile) throws SourceException, FormatException, IOException {
    if (sources.containsKey(sourceId)) {
      throw new SourceException(config.getLogPrefix() + "Already loaded source id '" + sourceId + "'");
    }

    Source source = factory.createSource(sourceId, pathToFile);
    sources.put(sourceId, source);
  }

  @Override
  public double mixToFile(OutputEncoder outputEncoder, String outputFilePath, Float quality) throws Exception {
    double totalSeconds = puts.values().stream()
      .map(Put::getStopAtMicros)
      .max(Long::compare)
      .orElse(0L) / MICROS_PER_SECOND;
    int totalFrames = (int) Math.floor(totalSeconds * outputFrameRate);
    int totalBytes = totalFrames * outputFrameSize;
    busBuf = new double[busIds.size()][totalFrames][outputChannels];
    outBuf = new double[totalFrames][outputChannels];

    if (!Objects.equals(state, MixerState.Ready))
      throw new MixerException(config.getLogPrefix() + "can't mix again; only one mix allowed per Mixer");

    // start the mixer
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    var numInstances = puts.size();
    var numSources = sources.size();
    LOG.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} instances of {} sources",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      puts.size(),
      numInstances,
      numSources);

    // Start with original sources summed up verbatim
    // Initial mix steps are done on individual busses
    // Multi-bus output with individual normalization REF https://www.pivotaltracker.com/story/show/179081795
    applySources();
    for (int b = 0; b < busIds.size(); b++)
      applyFinalOutputCompressor();
    mixOutputBus();

    // The dynamic range is forced into gentle logarithmic decay.
    // This alters the relative amplitudes from the previous step, implicitly normalizing them well below amplitude 1.0
    applyLogarithmicDynamicRange();

    // Compression is more predictable within the logarithmic range
    applyFinalOutputCompressor();

    // NO NORMALIZATION! See #179257872
    // applyNormalization(outBuf);

    //
    state = MixerState.Done;
    LOG.debug(config.getLogPrefix() + "Did mix {} seconds of output audio at {} Hz from {} instances of {} sources in {}s",
      String.format("%.9f", totalSeconds),
      outputFrameRate,
      numInstances,
      numSources,
      String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    if (0 == outBuf.length) {
      LOG.warn(config.getLogPrefix() + "Output buffer is empty!");
      outBuf = new double[][]{new double[]{0, 0}, new double[]{0, 0}};
    }

    startedAt = System.nanoTime();
    LOG.debug(config.getLogPrefix() + "Will write {} bytes of output audio", totalBytes);
    new AudioStreamWriter(outBuf, quality).writeToFile(outputFilePath, config.getOutputFormat(), outputEncoder, totalFrames);
    LOG.debug(config.getLogPrefix() + "Did write {} OK in {}s", outputFilePath, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));
    return totalSeconds;
  }

  /**
   Mix input buffers to output buffer
   */
  private void mixOutputBus() {
    double[] level = busIds.stream().mapToDouble(b -> busLevel.getOrDefault(b, 1.0)).toArray();
    int b, f, c;
    for (b = 0; b < busBuf.length; b++)
      for (f = 0; f < busBuf[0].length; f++)
        for (c = 0; c < busBuf[0][0].length; c++)
          outBuf[f][c] += busBuf[b][f][c] * level[b];
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
  public boolean hasLoadedSource(String sourceKey) {
    return sources.containsKey(sourceKey);
  }

  @Override
  public Source getSource(String sourceId) {
    return sources.get(sourceId);
  }

  /**
   apply original sources to mixing buffer
   addition of all sources into initial mixed source frames
   */
  private void applySources() throws MixerException {
    for (var source : sources.values()) applySource(source);
  }

  /**
   apply one source to the mixing buffer

   @param source to apply
   */
  private void applySource(Source source) throws MixerException {
    int i; // iterating on frames
    int tf, otf = -1; // target output buffer frame, and cache the old value in order to skip frames, init at -1 to force initial frame
    int ptf; // put target frame
    int b, p; // iterators: byte, put
    int tc; // iterators: source channel, target channel
    double v, ev; // a single sample value, and the enveloped value

    // steps to get requisite items stored plain arrays, for access speed
    var srcPutList = puts.values().stream()
      .filter(put -> source.getSourceId().equals(put.getSourceId()))
      .collect(Collectors.toList());
    Put[] srcPut = new Put[srcPutList.size()];
    int[] srcPutSpan = new int[srcPut.length];
    int[] srcPutFrom = new int[srcPut.length];
    for (p = 0; p < srcPut.length; p++) {
      srcPut[p] = srcPutList.get(p);
      srcPutFrom[p] = (int) (srcPut[p].getStartAtMicros() / microsPerFrame);
      srcPutSpan[p] = (int) ((srcPut[p].getStopAtMicros() - srcPut[p].getStartAtMicros()) / microsPerFrame);
    }

    // ratio of target frame rate to source frame rate
    // e.g. mixing from 96hz source to 48hz target = 0.5
    var fr = outputFrameRate / source.getFrameRate();

    try (
      var fileInputStream = FileUtils.openInputStream(new File(source.getAbsolutePath()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = source.getAudioFormat().getFrameSize();
      var channels = source.getAudioFormat().getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE <= expectBytes)
        throw new MixerException("loading audio steams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == source.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / source.getAudioFormat().getFrameSize();
      } else {
        expectFrames = (int) source.getFrameLength();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new MixerException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(source.getAudioFormat());

      int sf = 0; // current source frame
      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[source.getSampleSize()];
      byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer; b += frameSize) {
          tf = (int) Math.floor(sf * fr); // compute the target frame (converted from source rate to target rate)
          // FUTURE: skip frame if unnecessary (source rate higher than target rate)
          for (tc = 0; tc < outputChannels; tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            v = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            for (p = 0; p < srcPut.length; p++) {
              ev = Envelope.at((int) Math.min(sf, srcPutSpan[p] - sf * fr), v * srcPut[p].getVelocity());
              for (i = otf + 1; i <= tf; i++) {
                ptf = srcPutFrom[p] + i;
                if (ptf < 0 || ptf >= busBuf[0].length) continue;
                busBuf[srcPut[p].getBus()][ptf][tc] += ev;
              }
            }
          }
          otf = tf;
          sf++;
        }
      }
    } catch (UnsupportedAudioFileException | IOException | FormatException e) {
      throw new MixerException(String.format("Failed to apply Source[%s]", source.getSourceId()), e);
    }
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
  private void applyFinalOutputCompressor() {
    //
    double compRatio = computeCompressorTarget(outBuf, 0, framesAhead);
    double compRatioDelta = 0; // rate of change
    double targetCompRatio = compRatio;
    for (int i = 0; i < outBuf.length; i++) {
      if (0 == i % dspBufferSize) {
        targetCompRatio = computeCompressorTarget(outBuf, i, i + framesAhead);
      }
      double compRatioDeltaDelta = MathUtil.delta(compRatio, targetCompRatio) / framesDecay;
      compRatioDelta += MathUtil.delta(compRatioDelta, compRatioDeltaDelta) / dspBufferSize;
      compRatio += compRatioDelta;
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= compRatio;
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
  private void applyLogarithmicDynamicRange() {
    for (int i = 0; i < outBuf.length; i++)
      outBuf[i] = MathUtil.logarithmicCompression(outBuf[i]);
  }

  /**
   NO NORMALIZATION! See #179257872
   <p>
   Previously: apply normalization to mixing buffer
   [#154112129] normalize final buffer to normalization threshold
   */
  @SuppressWarnings("unused")
  private void applyNormalization() {
    double normRatio = Math.min(config.getNormalizationBoostThreshold(), config.getNormalizationCeiling() / MathUtil.maxAbs(outBuf, NORMALIZATION_GRAIN));
    for (int i = 0; i < outBuf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= normRatio;
  }

  /**
   [#154112129] lookahead-attack compressor compresses entire buffer towards target amplitude

   @return target amplitude
   */
  private double computeCompressorTarget(double[][] input, int iFr, int iTo) {
    double currentAmplitude = MathUtil.maxAbs(input, iFr, iTo, COMPRESSION_GRAIN);
    return MathUtil.limit(compressRatioMin, compressRatioMax, compressToAmplitude / currentAmplitude);
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
   Each new bus name maps to a number

   @param busName to get number for
   @return number of bus id
   */
  private int getAssignedBusNumber(String busName) {
    if (!busIds.contains(busName))
      busIds.add(busName);
    return busIds.indexOf(busName);
  }
}


