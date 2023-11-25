// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

import io.xj.hub.enums.InstrumentType;
import io.xj.nexus.NexusException;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.NANOS_PER_SECOND;

class MixerImpl implements Mixer {
  static final Logger LOG = LoggerFactory.getLogger(MixerImpl.class);
  static final int NORMALIZATION_GRAIN = 20;
  static final int COMPRESSION_GRAIN = 20;
  final float microsPerFrame;
  final float outputFrameRate;
  final int outputChannels;
  final int outputFrameSize;
  final MixerFactory factory;
  private final AudioCache audioCache;
  final MixerConfig config;
  final float compressToAmplitude;
  final float compressRatioMin;
  final float compressRatioMax;
  final int framesAhead;
  final int dspBufferSize;
  final int framesDecay;
  final Map<Integer, Float> busLevel = new ConcurrentHashMap<>();
  final int framesPerMilli;
  final EnvelopeProvider envelope;

  final BytePipeline buffer;
  final int totalMixFrames;
  MixerState state = MixerState.Ready;
  final float[][][] busBuf; // buffer separated into busses like [bus][frame][channel]
  final float[][] outBuf; // final output buffer like [bus][frame][channel]
  final AudioFormat audioFormat;
  float compRatio = 0; // mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render
  float[] busCompRatio; // mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render
  final Map<InstrumentType, Integer> instrumentBusNumber = new ConcurrentHashMap<>();
  final AtomicInteger computedBusNumber = new AtomicInteger(0);

  /**
   Instantiate a single Mix instance

   @param audioCache     cache of audio
   @param mixerConfig    configuration of mixer
   @param mixerFactory   factory to of new mixer
   @param outputPipeSize capacity of output buffer
   @throws MixerException on failure
   */
  public MixerImpl(
    AudioCache audioCache,
    MixerConfig mixerConfig,
    MixerFactory mixerFactory,
    EnvelopeProvider envelopeProvider,
    int outputPipeSize
  ) throws MixerException {
    this.audioCache = audioCache;
    config = mixerConfig;
    factory = mixerFactory;
    envelope = envelopeProvider;
    compressToAmplitude = config.getCompressToAmplitude();
    dspBufferSize = config.getDSPBufferSize();
    compressRatioMax = config.getCompressRatioMax();
    compressRatioMin = config.getCompressRatioMin();
    framesAhead = config.getCompressAheadFrames();
    framesDecay = config.getCompressDecayFrames();

    try {
      audioFormat = config.getOutputFormat();
      outputChannels = audioFormat.getChannels();
      MathUtil.enforceMin(1, "output audio channels", outputChannels);
      MathUtil.enforceMax(2, "output audio channels", outputChannels);
      outputFrameRate = audioFormat.getFrameRate();
      framesPerMilli = (int) (outputFrameRate / 1000);
      microsPerFrame = MICROS_PER_SECOND / outputFrameRate;
      totalMixFrames = (int) Math.floor(config.getTotalSeconds() * outputFrameRate);
      busBuf = new float[config.getTotalBuses()][totalMixFrames][outputChannels];
      busCompRatio = new float[config.getTotalBuses()];
      outBuf = new float[totalMixFrames][outputChannels];
      outputFrameSize = audioFormat.getFrameSize();
      int totalBytes = totalMixFrames * outputFrameSize;
      buffer = new BytePipeline(outputPipeSize);

      LOG.debug(config.getLogPrefix() + "Did initialize mixer with " + "outputChannels: {}, " + "outputFrameRate: {}, " + "outputFrameSize: {}, " + "microsPerFrame: {}, " + "totalSeconds: {}, " + "totalFrames: {}, " + "totalBytes: {}", outputChannels, outputFrameRate, outputFrameSize, microsPerFrame, config.getTotalSeconds(), totalMixFrames, totalBytes);

    } catch (Exception e) {
      throw new MixerException(config.getLogPrefix() + "unable to setup internal variables from output audio format", e);
    }
  }

  @Override
  public void setBusLevel(int busId, float level) {
    busLevel.put(busId, level);
  }

  @Override
  public float mix(List<ActiveAudio> active) throws MixerException, FormatException, IOException {
    // clear the mixing buffer and start the mixing process
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    clearBuffers();
    LOG.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} active audio instances", config.getTotalSeconds(), outputFrameRate, active.size());

    // Start with original sources summed up verbatim
    // Initial mix steps are done on individual bus
    // Multi-bus output with individual normalization REF https://www.pivotaltracker.com/story/show/179081795
    for (var a : active) addToMix(a);
/*
  FUTURE: apply compression to each bus
    for (int b = 0; b < busBuf.length; b++)
      applyBusCompressor(b);
*/
    mixOutputBus();

    // The dynamic range is forced into gentle logarithmic decay.
    // This alters the relative amplitudes from the previous step, implicitly normalizing them well below amplitude 1.0
    applyLogarithmicDynamicRange();

    // Compression is more predictable within the logarithmic range
    applyFinalOutputCompressor();

    // NO NORMALIZATION! See https://www.pivotaltracker.com/story/show/179257872
    // applyNormalization(outBuf);

    //
    state = MixerState.Done;
    LOG.debug(config.getLogPrefix() + "Did mix {} seconds of output audio at {} Hz from {} audio instances in {}s", config.getTotalSeconds(), outputFrameRate, active.size(), String.format("%.9f", (float) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    // Write the output bytes to the shared buffer
    buffer.produce(byteBufferOf(audioFormat, outBuf).array());
    return config.getTotalSeconds();
  }

  /**
   Zero all buffers
   */
  void clearBuffers() {
    for (int b = 0; b < busBuf.length; b++)
      for (int f = 0; f < busBuf[0].length; f++)
        for (int c = 0; c < busBuf[0][0].length; c++)
          busBuf[b][f][c] = 0.0f;
    for (int f = 0; f < outBuf.length; f++)
      for (int c = 0; c < outBuf[0].length; c++)
        outBuf[f][c] = 0.0f;
  }

  /**
   Mix input buffers to output buffer
   */
  void mixOutputBus() {
    double[] level = Stream.iterate(0, i -> i + 1).limit(busBuf.length).mapToDouble(i -> busLevel.getOrDefault(i, 1.0f)).toArray();
    int b, f, c;
    for (b = 0; b < busBuf.length; b++)
      for (f = 0; f < busBuf[0].length; f++)
        for (c = 0; c < busBuf[0][0].length; c++) {
          if (b > level.length - 1) {
            LOG.error(config.getLogPrefix() + "b > level.length - 1: " + b + " > " + (level.length - 1));
          }
          outBuf[f][c] += (float) (busBuf[b][f][c] * level[b]);
        }

  }

  @Override
  public String toString() {
    return config.getLogPrefix() + "{ " + "outputChannels:" + outputChannels + ", " + "outputFrameRate:" + outputFrameRate + ", " + "outputFrameSize:" + outputFrameSize + ", " + "microsPerFrame:" + microsPerFrame + ", " + "microsPerFrame:" + microsPerFrame + " }";
  }

  @Override
  public MixerState getState() {
    return state;
  }

  @Override
  public BytePipeline getBuffer() {
    return buffer;
  }

  @Override
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }

  @Override
  public int getBusNumber(InstrumentType instrumentType) {
    if (!instrumentBusNumber.containsKey(instrumentType))
      instrumentBusNumber.put(instrumentType, computedBusNumber.getAndIncrement());
    return instrumentBusNumber.get(instrumentType);
  }

  /**
   apply one source to the mixing buffer

   @param active to apply
   */
  void addToMix(ActiveAudio active) throws MixerException {
    try {
      var cached = audioCache.load(
        config.getContentStoragePathPrefix(),
        config.getAudioBaseUrl(),
        active.getInstrument().getId(),
        active.getAudio().getWaveformKey(),
        (int) outputFrameRate,
        audioFormat.getSampleSizeInBits(),
        audioFormat.getChannels());

      // determine the bus number for this instrument type
      int bus = getBusNumber(active.getInstrument().getType());

      // create the release envelope
      var releaseEnvelope = envelope.length(active.getReleaseMillis() * framesPerMilli);

      // determine the theoretical frame in the mixing buffer at which the source audio will be added
      // these numbers may be below zero or past the limit of the mixing buffer
      int sourceBeginsAtMixerFrame = (int) (active.getStartAtMixerMicros() / microsPerFrame);
      int sourceEndsAtMixerFrame = active.getStopAtMixerMicros().isPresent() ?
        (int) (active.getStopAtMixerMicros().get() / microsPerFrame) :
        sourceBeginsAtMixerFrame + cached.audio().length;

      // reusable variables
      int c; // channel
      int tf; // target frame (in mix buffer)
      int sf; // source frame (from source audio)
      int rf = 0; // release envelope frame (start counting at end of source)

      // determine the actual start and end frames in the mixing buffer and source audio
      int tf_min = bufferIndexLimit(sourceBeginsAtMixerFrame); // initial target frame (in mix buffer)
      int tf_max = bufferIndexLimit(sourceEndsAtMixerFrame + releaseEnvelope.exponential.length); // final target frame (in mix buffer)
      sf = tf_min - sourceBeginsAtMixerFrame; // initial source frame (from source audio)

      // iterate over all frames overlapping from the source audio and the target mixing buffer
      for (tf = tf_min; tf < tf_max; tf++) {
        for (c = 0; c < outputChannels; c++) {
          if (sf < cached.audio().length) {
            if (tf < sourceEndsAtMixerFrame) {
              busBuf[bus][tf][c] += cached.audio()[sf][c] * active.getAmplitude();
            } else {
              // release envelope
              busBuf[bus][tf][c] += releaseEnvelope.out(rf, cached.audio()[sf][c] * active.getAmplitude());
              rf++;
            }
          }
        }
        sf++;
      }

    } catch (IOException | NexusException | AudioCacheException e) {
      throw new MixerException(String.format("Failed to apply Source[%s]", active.getAudio().getId()), e);
    }
  }

  /**
   Limit a frame number between 0 and the actual final mix buffer index

   @param frame to limit
   @return limited frame
   */
  private int bufferIndexLimit(float frame) {
    return (int) Math.min(Math.max(0, frame), totalMixFrames - 1);
  }

  /**
   apply compressor to mixing buffer
   <p>
   lookahead-attack compressor compresses entire buffer towards target amplitude https://www.pivotaltracker.com/story/show/154112129
   <p>
   only each major cycle, compute the new target compression ratio,
   but modify the compression ratio *every* frame for max smoothness
   <p>
   compression target uses a rate of change of rate of change
   to maintain inertia over time, required to preserve audio signal
   */
  void applyFinalOutputCompressor() {
    // only set the comp ratio directly if it's never been set before, otherwise mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render
    if (0 == compRatio) {
      compRatio = computeCompressorTarget(outBuf, 0, framesAhead);
    }
    float compRatioDelta = 0; // rate of change
    float targetCompRatio = compRatio;
    for (int i = 0; i < outBuf.length; i++) {
      if (0 == i % dspBufferSize) {
        targetCompRatio = computeCompressorTarget(outBuf, i, i + framesAhead);
      }
      float compRatioDeltaDelta = MathUtil.delta(compRatio, targetCompRatio) / framesDecay;
      compRatioDelta += MathUtil.delta(compRatioDelta, compRatioDeltaDelta) / dspBufferSize;
      compRatio += compRatioDelta;
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= compRatio;
    }
  }

  /*
   FUTURE: apply compressor to individual bus buffers
   <p>
   lookahead-attack compressor compresses entire buffer towards target amplitude https://www.pivotaltracker.com/story/show/154112129
   <p>
   only each major cycle, compute the new target compression ratio,
   but modify the compression ratio *every* frame for max smoothness
   <p>
   compression target uses a rate of change of rate of change
   to maintain inertia over time, required to preserve audio signal
   *
  private void applyBusCompressor(int b) {
    // only set the comp ratio directly if it's never been set before, otherwise mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render
    if (0 == busCompRatio[b]) {
      busCompRatio[b] = computeCompressorTarget(busBuf[b], 0, framesAhead);
    }
    float compRatioDelta = 0; // rate of change
    float targetCompRatio = busCompRatio[b];
    for (int i = 0; i < busBuf[b].length; i++) {
      if (0 == i % dspBufferSize) {
        targetCompRatio = computeCompressorTarget(busBuf[b], i, i + framesAhead);
      }
      float compRatioDeltaDelta = MathUtil.delta(busCompRatio[b], targetCompRatio) / framesDecay;
      compRatioDelta += MathUtil.delta(compRatioDelta, compRatioDeltaDelta) / dspBufferSize;
      busCompRatio[b] += compRatioDelta;
      for (int k = 0; k < outputChannels; k++)
        busBuf[b][i][k] *= busCompRatio[b];
    }
  }
   */


  /*
   FUTURE Engineer wants high-pass and low-pass filters with gradual thresholds, in order to be optimally heard but not listened to. https://www.pivotaltracker.com/story/show/161670248
   The lowpass filter ensures there are no screeching extra-high tones in the mix.
   The highpass filter ensures there are no distorting ultra-low tones in the mix.
   *
  void applyBandpass(float[][] buf) throws MixerException {
    FrequencyRangeLimiter.filter(buf, outputFrameRate, dspBufferSize, (float) highpassThresholdHz, (float) lowpassThresholdHz);
  }
   */

  /**
   apply logarithmic dynamic range to mixing buffer
   */
  void applyLogarithmicDynamicRange() {
    for (int i = 0; i < outBuf.length; i++)
      outBuf[i] = MathUtil.logarithmicCompression(outBuf[i]);
  }

  /**
   NO NORMALIZATION! See https://www.pivotaltracker.com/story/show/179257872
   <p>
   Previously: apply normalization to mixing buffer
   normalize final buffer to normalization threshold https://www.pivotaltracker.com/story/show/154112129
   */
  @SuppressWarnings("unused")
  void applyNormalization() {
    float normRatio = Math.min(config.getNormalizationBoostThreshold(), config.getNormalizationCeiling() / MathUtil.maxAbs(outBuf, NORMALIZATION_GRAIN));
    for (int i = 0; i < outBuf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= normRatio;
  }

  /**
   lookahead-attack compressor compresses entire buffer towards target amplitude https://www.pivotaltracker.com/story/show/154112129

   @return target amplitude
   */
  float computeCompressorTarget(float[][] input, int iFr, int iTo) {
    float currentAmplitude = MathUtil.maxAbs(input, iFr, iTo, COMPRESSION_GRAIN);
    return MathUtil.limit(compressRatioMin, compressRatioMax, compressToAmplitude / currentAmplitude);
  }

  /**
   Convert output values into a ByteBuffer

   @param fmt     to write
   @param samples [frame][channel] output to convert
   @return byte buffer of stream
   */
  static ByteBuffer byteBufferOf(AudioFormat fmt, float[][] samples) throws FormatException {
    ByteBuffer outputBytes = ByteBuffer.allocate(samples.length * fmt.getFrameSize());
    for (float[] sample : samples)
      for (float v : sample)
        outputBytes.put(AudioSampleFormat.toBytes(v, AudioSampleFormat.typeOfOutput(fmt)));

    return outputBytes;
  }
}


