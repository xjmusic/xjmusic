// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.NANOS_PER_SECOND;

class MixerImpl implements Mixer {
  public static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  static final Logger LOG = LoggerFactory.getLogger(MixerImpl.class);
  static final int READ_BUFFER_BYTE_SIZE = 1024;
  static final int NORMALIZATION_GRAIN = 20;
  static final int COMPRESSION_GRAIN = 20;
  final float microsPerFrame;
  final float outputFrameRate;
  final int outputChannels;
  final int outputFrameSize;
  final Map<UUID, Source> sources = new ConcurrentHashMap<>(); // concurrency required
  final Map<UUID, Put> activePuts = new ConcurrentHashMap<>(); // concurrency required
  final MixerFactory factory;
  final MixerConfig config;
  final double compressToAmplitude;
  final double compressRatioMin;
  final double compressRatioMax;
  final int framesAhead;
  final int dspBufferSize;
  final int framesDecay;
  final Map<Integer, Double> busLevel = new ConcurrentHashMap<>();
  final int framesPerMilli;
  final EnvelopeProvider envelope;

  final BytePipeline buffer;
  MixerState state = MixerState.Ready;
  final double[][][] busBuf; // buffer separated into busses like [bus][frame][channel]
  final double[][] outBuf; // final output buffer like [bus][frame][channel]
  final AudioFormat audioFormat;
  double compRatio = 0; // mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render
  double[] busCompRatio; // mixer effects are continuous between renders, so these ending values make their way back to the beginning of the next render

  /**
   Instantiate a single Mix instance

   @param mixerConfig    configuration of mixer
   @param mixerFactory   factory to of new mixer
   @param outputPipeSize capacity of output buffer
   @throws MixerException on failure
   */
  public MixerImpl(
    MixerConfig mixerConfig,
    MixerFactory mixerFactory,
    EnvelopeProvider envelopeProvider,
    int outputPipeSize
  ) throws MixerException {
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
      int totalFrames = (int) Math.floor(config.getTotalSeconds() * outputFrameRate);
      busBuf = new double[config.getTotalBuses()][totalFrames][outputChannels];
      busCompRatio = new double[config.getTotalBuses()];
      outBuf = new double[totalFrames][outputChannels];
      outputFrameSize = audioFormat.getFrameSize();
      int totalBytes = totalFrames * outputFrameSize;
      buffer = new BytePipeline(outputPipeSize);

      LOG.debug(config.getLogPrefix() + "Did initialize mixer with " + "outputChannels: {}, " + "outputFrameRate: {}, " + "outputFrameSize: {}, " + "microsPerFrame: {}, " + "totalSeconds: {}, " + "totalFrames: {}, " + "totalBytes: {}", outputChannels, outputFrameRate, outputFrameSize, microsPerFrame, config.getTotalSeconds(), totalFrames, totalBytes);

    } catch (Exception e) {
      throw new MixerException(config.getLogPrefix() + "unable to setup internal variables from output audio format", e);
    }
  }

  @Override
  public void put(UUID id, UUID audioId, int busId, long startAtMicros, long stopAtMicros, double velocity, int attackMillis, int releaseMillis) throws PutException {
    activePuts.put(id, factory.createPut(id, audioId, busId, startAtMicros, stopAtMicros, velocity, attackMillis, releaseMillis));
  }

  @Override
  public void del(UUID id) {
    activePuts.remove(id);
  }

  @Override
  public void setBusLevel(int busId, double level) {
    busLevel.put(busId, level);
  }

  @Override
  public void loadSource(UUID audioId, String pathToFile, String description) throws SourceException, FormatException, IOException {
    if (sources.containsKey(audioId)) {
      LOG.debug(config.getLogPrefix() + "Already loaded Source[" + audioId + "] \"" + description + "\"");
      return;
    }

    Source source = factory.createSource(audioId, pathToFile, description);
    sources.put(audioId, source);
  }

  @Override
  public double mix() throws MixerException, FormatException, IOException {
    // clear the mixing buffer and start the mixing process
    state = MixerState.Mixing;
    long startedAt = System.nanoTime();
    clearBuffers();
    LOG.debug(config.getLogPrefix() + "Will mix {} seconds of output audio at {} Hz frame rate from {} instances of {} sources", config.getTotalSeconds(), outputFrameRate, activePuts.size(), sources.size());

    // Start with original sources summed up verbatim
    // Initial mix steps are done on individual bus
    // Multi-bus output with individual normalization REF https://www.pivotaltracker.com/story/show/179081795
    applySources();
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
    LOG.debug(config.getLogPrefix() + "Did mix {} seconds of output audio at {} Hz from {} instances of {} sources in {}s", config.getTotalSeconds(), outputFrameRate, activePuts.size(), sources.size(), String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

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
          busBuf[b][f][c] = 0.0;
    for (int f = 0; f < outBuf.length; f++)
      for (int c = 0; c < outBuf[0].length; c++)
        outBuf[f][c] = 0.0;
  }

  /**
   Mix input buffers to output buffer
   */
  void mixOutputBus() {
    double[] level = Stream.iterate(0, i -> i + 1).limit(busBuf.length).mapToDouble(i -> busLevel.getOrDefault(i, 1.0)).toArray();
    int b, f, c;
    for (b = 0; b < busBuf.length; b++)
      for (f = 0; f < busBuf[0].length; f++)
        for (c = 0; c < busBuf[0][0].length; c++) {
          if (b > level.length - 1) {
            LOG.error(config.getLogPrefix() + "b > level.length - 1: " + b + " > " + (level.length - 1));
          }
          outBuf[f][c] += busBuf[b][f][c] * level[b];
        }

  }

  @Override
  public String toString() {
    return config.getLogPrefix() + "{ " + "outputChannels:" + outputChannels + ", " + "outputFrameRate:" + outputFrameRate + ", " + "outputFrameSize:" + outputFrameSize + ", " + "microsPerFrame:" + microsPerFrame + ", " + "microsPerFrame:" + microsPerFrame + " }";
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
  public boolean hasLoadedSource(UUID audioId) {
    return sources.containsKey(audioId);
  }

  @Override
  public Source getSource(UUID audioId) {
    return sources.get(audioId);
  }

  @Override
  public BytePipeline getBuffer() {
    return buffer;
  }

  @Override
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }

  /**
   apply original sources to mixing buffer
   addition of all sources into initial mixed source frames
   */
  void applySources() throws MixerException {
    for (var source : sources.values()) applySource(source);
  }

  /**
   apply one source to the mixing buffer

   @param source to apply
   */
  void applySource(Source source) throws MixerException {
    if (source.getAudioFormat().isEmpty()) return;
    AudioFormat fmt = source.getAudioFormat().get();

    int ptf; // put target frame
    int b, p; // iterators: byte, put
    int tc; // iterators: source channel, target channel
    double v, ev; // a single sample value, and the enveloped value

    // steps to get requisite items stored plain arrays, for access speed
    var srcPutList = activePuts.values().stream().filter(put -> source.getAudioId().equals(put.getAudioId())).toList();
    Put[] srcPut = new Put[srcPutList.size()];
    int[] srcPutSpan = new int[srcPut.length];
    int[] srcPutFrom = new int[srcPut.length];
    for (p = 0; p < srcPut.length; p++) {
      srcPut[p] = srcPutList.get(p);
      srcPutFrom[p] = (int) (srcPut[p].getStartAtMicros() / microsPerFrame);
      srcPutSpan[p] = (int) ((srcPut[p].getStopAtMicros() - srcPut[p].getStartAtMicros()) / microsPerFrame);
    }

/*
TODO refactor the following to use the in-memory dub audio cache
    try (
      var fileInputStream = FileUtils.openInputStream(new File(source.getAbsolutePath()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = fmt.getFrameSize();
      var channels = fmt.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE == expectBytes)
        throw new MixerException("loading audio streams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == source.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / fmt.getFrameSize();
      } else {
        expectFrames = (int) source.getFrameLength();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new MixerException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(fmt);

      int sf = 0; // current source frame
      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[source.getSampleSize()];
      byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer; b += frameSize) {
          // FUTURE: skip frame if unnecessary (source rate higher than target rate)
          for (tc = 0; tc < outputChannels; tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            v = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            for (p = 0; p < srcPut.length; p++) {
              if (sf < srcPutSpan[p]) // attack phase
                ev = envelope.length(srcPut[p].getAttackMillis() * framesPerMilli).in(sf, v * srcPut[p].getVelocity());
              else // release phase
                ev = envelope.length(srcPut[p].getReleaseMillis() * framesPerMilli).out(sf - srcPutSpan[p], v * srcPut[p].getVelocity());

              ptf = srcPutFrom[p] + sf;
              if (ptf < 0 || ptf >= busBuf[0].length) continue;
              busBuf[srcPut[p].getBus()][ptf][tc] += ev;
            }
          }
          sf++;
        }
      }
    } catch (UnsupportedAudioFileException | IOException | FormatException e) {
      throw new MixerException(String.format("Failed to apply Source[%s]", source.getAudioId()), e);
    }
*/
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
    double compRatioDelta = 0; // rate of change
    double targetCompRatio = busCompRatio[b];
    for (int i = 0; i < busBuf[b].length; i++) {
      if (0 == i % dspBufferSize) {
        targetCompRatio = computeCompressorTarget(busBuf[b], i, i + framesAhead);
      }
      double compRatioDeltaDelta = MathUtil.delta(busCompRatio[b], targetCompRatio) / framesDecay;
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
  void applyBandpass(double[][] buf) throws MixerException {
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
    double normRatio = Math.min(config.getNormalizationBoostThreshold(), config.getNormalizationCeiling() / MathUtil.maxAbs(outBuf, NORMALIZATION_GRAIN));
    for (int i = 0; i < outBuf.length; i++)
      for (int k = 0; k < outputChannels; k++)
        outBuf[i][k] *= normRatio;
  }

  /**
   lookahead-attack compressor compresses entire buffer towards target amplitude https://www.pivotaltracker.com/story/show/154112129

   @return target amplitude
   */
  double computeCompressorTarget(double[][] input, int iFr, int iTo) {
    double currentAmplitude = MathUtil.maxAbs(input, iFr, iTo, COMPRESSION_GRAIN);
    return MathUtil.limit(compressRatioMin, compressRatioMax, compressToAmplitude / currentAmplitude);
  }

  /**
   Convert output values into a ByteBuffer

   @param fmt     to write
   @param samples [frame][channel] output to convert
   @return byte buffer of stream
   */
  static ByteBuffer byteBufferOf(AudioFormat fmt, double[][] samples) throws FormatException {
    ByteBuffer outputBytes = ByteBuffer.allocate(samples.length * fmt.getFrameSize());
    for (double[] sample : samples)
      for (double v : sample)
        outputBytes.put(AudioSampleFormat.toBytes(v, AudioSampleFormat.typeOfOutput(fmt)));

    return outputBytes;
  }
}


