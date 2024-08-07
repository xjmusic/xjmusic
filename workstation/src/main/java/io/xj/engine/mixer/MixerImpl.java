// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.mixer;

import io.xj.model.enums.InstrumentType;
import io.xj.model.util.StringUtils;
import io.xj.engine.FabricationException;
import io.xj.engine.audio.AudioCache;
import io.xj.engine.audio.AudioCacheException;
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

import static io.xj.model.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.model.util.ValueUtils.NANOS_PER_SECOND;

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
  public float mix(List<ActiveAudio> active, double intensity) throws MixerException, FormatException, IOException {
    // clear the mixing buffer and start the mixing process
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    clearBuffers();
    LOG.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} active audio instances", config.getTotalSeconds(), outputFrameRate, active.size());

    // Start with original sources summed up verbatim
    // Initial mix steps are done on individual bus
    // Multi-bus output with individual normalization REF https://github.com/xjmusic/xjmusic/issues/275
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

    // NO NORMALIZATION! See https://github.com/xjmusic/xjmusic/issues/275
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
    for (b = 0; b < busBuf.length; b++) {
      if (b > level.length - 1) {
        LOG.warn(config.getLogPrefix() + "b > level.length - 1: " + b + " > " + (level.length - 1));
        continue;
      }
      for (f = 0; f < busBuf[0].length; f++)
        for (c = 0; c < busBuf[0][0].length; c++) {
          outBuf[f][c] += (float) (busBuf[b][f][c] * level[b]);
        }
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
   apply one source to the mixing buffer@param active    to apply
   */
  void addToMix(ActiveAudio active) throws MixerException {
    try {
      if (StringUtils.isNullOrEmpty(active.getAudio().getWaveformKey())) {
        LOG.warn("Active audio has empty waveform key! instrumentId: {}, audioId: {}", active.getInstrument().getId(), active.getAudio().getId());
        return;
      }
      var cached = audioCache.load(active.getAudio());

      // determine the bus number for this instrument type
      int bus = getBusNumber(active.getInstrument().getType());

      // create the release envelope
      var releaseEnvelope = envelope.length(active.getReleaseMillis() * framesPerMilli);

      // determine the theoretical frame in the mixing buffer at which the source audio will be added
      // these numbers may be below zero or past the limit of the mixing buffer
      int sourceBeginsAtMixerFrame = (int) (active.getStartAtMixerMicros() / microsPerFrame);
      int sourceEndsAtMixerFrame = (int) (active.getStopAtMixerMicros() / microsPerFrame);

      // reusable variables
      int tc; // target channel
      int tf; // target frame (in mix buffer)
      int sc; // source channel
      int sf; // source frame (from source audio)

      // determine the actual start and end frames in the mixing buffer and source audio
      int tf_min = bufferIndexLimit(sourceBeginsAtMixerFrame); // initial target frame (in mix buffer)
      int tf_max = bufferIndexLimit(sourceEndsAtMixerFrame + releaseEnvelope.exponential.length); // final target frame (in mix buffer)

      // amplitude position moves from 0 to 1 over the source audio
      float ap; // amplitude position moves from 0 to 1
      float ap_d = 1.0f / (tf_max - tf_min); // amplitude delta per frame

      // iterate over all frames overlapping from the source audio and the target mixing buffer
      for (tc = 0; tc < outputChannels; tc++) {
        int rf = 0; // release envelope frame (start counting at end of source)
        sf = tf_min - sourceBeginsAtMixerFrame; // initial source frame (from source audio)
        sc = tc % cached.data()[0].length; // source channel (from source audio)
        while (sf < 0) {
          sf++; // skip source frames before the start of the source audio
          tf_min++;
        }
        ap = 0; // reset amplitude position
        for (tf = tf_min; tf <= tf_max; tf++) {
          if (sf < cached.data().length) {
            if (tf < sourceEndsAtMixerFrame) {
              busBuf[bus][tf][tc] += cached.data()[sf][sc] * active.getAmplitude(ap);
            } else {
              // release envelope
              busBuf[bus][tf][tc] += releaseEnvelope.out(rf, cached.data()[sf][sc] * active.getAmplitude(ap));
              rf++;
            }
          }
          sf++;
          ap += ap_d;
        }
      }

    } catch (IOException | FabricationException | AudioCacheException e) {
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
   lookahead-attack compressor compresses entire buffer towards target amplitude https://github.com/xjmusic/xjmusic/issues/274
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
   lookahead-attack compressor compresses entire buffer towards target amplitude https://github.com/xjmusic/xjmusic/issues/274
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


  /**
   apply logarithmic dynamic range to mixing buffer
   */
  void applyLogarithmicDynamicRange() {
    for (int i = 0; i < outBuf.length; i++)
      outBuf[i] = MathUtil.logarithmicCompression(outBuf[i]);
  }

  /**
   NO NORMALIZATION! See https://github.com/xjmusic/xjmusic/issues/275
   <p>
   Previously: apply normalization to mixing buffer
   normalize final buffer to normalization threshold https://github.com/xjmusic/xjmusic/issues/274
   */
  @SuppressWarnings("unused")
  void applyNormalization() {
    float normRatio = Math.min(config.getNormalizationBoostThreshold(), config.getNormalizationCeiling() / MathUtil.maxAbs(outBuf, NORMALIZATION_GRAIN));
    for (int i = 0; i < outBuf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= normRatio;
  }

  /**
   lookahead-attack compressor compresses entire buffer towards target amplitude https://github.com/xjmusic/xjmusic/issues/274

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


