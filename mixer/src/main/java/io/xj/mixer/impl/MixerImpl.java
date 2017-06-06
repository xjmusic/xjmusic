// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.mixer.impl;

import io.xj.mixer.Mixer;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.OutputContainer;
import io.xj.mixer.Put;
import io.xj.mixer.Source;
import io.xj.mixer.impl.audio.AudioSample;
import io.xj.mixer.impl.audio.AudioStreamWriter;
import io.xj.mixer.impl.exception.FormatException;
import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.impl.exception.PutException;
import io.xj.mixer.impl.exception.SourceException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class MixerImpl implements Mixer {
  private final static Logger log = LoggerFactory.getLogger(MixerImpl.class);
  private final static float microsInASecond = 1000000;
  private final static float nanosInASecond = 1000 * microsInASecond;

  // field: debugging
  private boolean debugging;

  // fields: output file
  private final double totalSeconds;
  private final float microsPerFrame;
  private final float outputFrameRate;
  private final int outputChannels;
  private final int outputFrameSize;
  private final int totalBytes;
  private final int totalFrames;
  private final Duration outputLength;
  private final String outputSampleFormat;

  // fields: playback state-machine
  private long nextCycleFrame = 0;
  private long cycleDurFrames = 1000;
  private String state;

  // fields: in-memory storage via concurrent maps
  private Map<String, Source> sources = Maps.newConcurrentMap();
  private Map<Long, Put> readyPuts = Maps.newConcurrentMap();
  private Map<Long, Put> livePuts = Maps.newConcurrentMap();
  private Map<Long, Put> donePuts = Maps.newConcurrentMap();
  private int uniquePutId = 0; // key for storage in map of Puts

  // fields : mix factory and audio format
  private final AudioFormat outputFormat;
  private final OutputContainer outputContainer;
  private MixerFactory mixerFactory;

  /**
   Instantiate a single Mix instance

   @param outputFormat including the final output outputLength
   */
  @Inject
  public MixerImpl(
    @Assisted("outputContainer") OutputContainer outputContainer,
    @Assisted("outputFormat") AudioFormat outputFormat,
    @Assisted("outputLength") Duration outputLength,
    MixerFactory mixerFactory
  ) throws MixerException {
    this.outputContainer = outputContainer;
    this.outputFormat = outputFormat;
    this.outputLength = outputLength;
    this.mixerFactory = mixerFactory;

    try {
      outputChannels = outputFormat.getChannels();
      enforceMin(1, "output audio channels", outputChannels);
      enforceMax(2, "output audio channels", outputChannels);
      outputFrameRate = outputFormat.getFrameRate();
      outputFrameSize = outputFormat.getFrameSize();
      outputSampleFormat = AudioSample.typeOfOutput(outputFormat);
      microsPerFrame = microsInASecond / outputFrameRate;
      totalSeconds = this.outputLength.toNanos() / nanosInASecond;
      totalFrames = (int) Math.floor(totalSeconds * outputFrameRate);
      totalBytes = totalFrames * outputFrameSize;

      log.info("Did initialize mixer: " + this);
    } catch (Exception e) {
      throw new MixerException("unable to setup internal variables from output audio format (" + e.getClass().getName() + "): " + e.getMessage());
    }

    state = READY;
  }

  @Override
  public void put(String sourceId, long startAtMicros, long stopAtMicros, double velocity, double pitchRatio, double pan) throws PutException {
    readyPuts.put(nextPutId(), mixerFactory.createPut(sourceId, startAtMicros, stopAtMicros, velocity, pitchRatio, pan));
  }

  @Override
  public void loadSource(String sourceId, BufferedInputStream inputStream) throws SourceException, FormatException, IOException {
    if (sources.containsKey(sourceId)) {
      throw new SourceException("Already loaded source id '" + sourceId + "'");
    }

    Source source = mixerFactory.createSource(sourceId, inputStream);
    sources.put(sourceId, source);
  }

  @Override
  public void mixToFile(String outputFilePath) throws Exception {
    // the big show
    ByteBuffer outputBytes = byteBufferOf(mix());

    state = WRITING;
    long startedAt = System.nanoTime();
    log.info("Will write {} bytes of output audio", totalBytes);
    new AudioStreamWriter(outputBytes).writeToFile(outputFilePath, outputFormat, outputContainer, totalFrames);

    state = DONE;
    log.info("Did write {} OK in {}s", outputFilePath, String.format("%.9f", (double) (System.nanoTime() - startedAt) / nanosInASecond));
  }

  @Override
  public String toString() {
    return "{ " +
      "outputLength:" + outputLength + ", " +
      "outputChannels:" + outputChannels + ", " +
      "outputFrameRate:" + outputFrameRate + ", " +
      "outputSampleFormat:" + outputSampleFormat + ", " +
      "outputFrameSize:" + outputFrameSize + ", " +
      "microsPerFrame:" + microsPerFrame + ", " +
      "microsPerFrame:" + microsPerFrame + ", " +
      "totalSeconds:" + totalSeconds + ", " +
      "totalFrames:" + totalFrames + ", " +
      "totalBytes:" + totalBytes +
      " }";
  }

  @Override
  public void setCycleMicros(long micros) throws MixerException {
    if (microsPerFrame == 0) {
      throw new MixerException("Must specify mixing frequency before setting cycle duration!");
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
  public String getState() {
    return state;
  }

  @Override
  public float getFrameRate() {
    return outputFrameRate;
  }

  @Override
  public AudioFormat getOutputFormat() {
    return outputFormat;
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
   Mix
   runs once per Mixer

   @return mixed output values
   @throws MixerException if unable to mix
   */
  private double[][] mix() throws Exception {
    if (!Objects.equals(state, READY))
      throw new MixerException("can't mix again; only one mix allowed per Mixer");

    state = MIXING;
    log.info("Will mix {} seconds of output audio at {} Hz frame rate", String.format("%.9f", totalSeconds), outputFrameRate);
    double[][] outputFrames = new double[totalFrames][outputChannels];
    long startedAt = System.nanoTime();
    for (int offsetFrame = 0; offsetFrame < totalFrames; offsetFrame++)
      outputFrames[offsetFrame] = mixFrame(offsetFrame);

    state = MIXED;
    log.info("Did mix {} frames in {}s", totalFrames, String.format("%.9f", (double) (System.nanoTime() - startedAt) / nanosInASecond));
    return outputFrames;
  }

  /**
   Convert output values into a ByteBuffer

   @param mix output to convert
   @return output as byte buffer
   */
  private ByteBuffer byteBufferOf(double[][] mix) {
    ByteBuffer outputBytes = ByteBuffer.allocate(totalBytes);
    for (int offsetFrame = 0; offsetFrame < totalFrames; offsetFrame++)
      for (int channel = 0; channel < mix[offsetFrame].length; channel++)
        outputBytes.put(AudioSample.toBytes(mix[offsetFrame][channel], outputSampleFormat));

    return outputBytes;
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
  private double[] mixFrame(long offsetFrame) {
    mixCycleBeforeEveryNthFrame(offsetFrame);

    double[] frame = new double[outputChannels];
    livePuts.forEach((id, livePut) -> {
      long sourceOffsetMicros = livePut.sourceOffsetMicros(getMicros(offsetFrame));
      if (sourceOffsetMicros > 0) {
        double[] inSamples;
        inSamples = mixSourceFrameAtMicros(livePut.getSourceId(), livePut.getVelocity(), livePut.getPan(), sourceOffsetMicros);
        for (int c = 0; c < outputChannels; c++)
          frame[c] = frame[c] + inSamples[c];
      }
    });

    return mixDynamicLogarithmicRangeCompression(frame);
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
    if (source == null)
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
    Map<String, Boolean> sourceUsage = buildMapAllSourceIdTo(false);

    // iterate through ready Puts
    readyPuts.forEach((putId, readyPut) -> {

      // keep track of sources in-use by ready Puts
      sourceUsage.put(readyPut.getSourceId(), true);

      // if a put is near-to-playback, move it to the live fire queue
      // double a mix cycle is considered near-playback enough to move a put from "ready" to "live"
      if (readyPut.getStartAtMicros() < offsetMicros + cycleDurFrames * microsPerFrame * 2) {
        readyPuts.remove(putId);
        livePuts.put(putId, readyPut);
        log.debug("READY -> LIVE [{}] Put {}", putId, readyPut);
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
        log.debug("LIVE -> DONE [{}] Put {}", putId, livePut);
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
    if (debugging && getSourceCount() > 0) {
      log.debug("mix [{}ns] puts-ready:{} puts-live:{} sources:{}\n", offsetMicros, getPutReadyCount(), getPutLiveCount(), getSourceCount());
    }
  }

  /**
   Get microsecond value of a frame offset

   @param frameOffset offset
   @return microseconds
   */
  private long getMicros(long frameOffset) {
    return (long) Math.floor(microsInASecond * (float) frameOffset / outputFrameRate);
  }

  /**
   build a map of all source id to boolean value

   @param value to set all values to
   @return map
   */
  private Map<String, Boolean> buildMapAllSourceIdTo(boolean value) {
    Map<String, Boolean> result = Maps.newHashMap();
    sources.keySet().forEach((sourceId) -> result.put(sourceId, value));
    return result;
  }

  /**
   Quick implementation of "Mixing two digital audio streams
   with on the fly Loudness Normalization
   by Logarithmic Dynamic Range Compression" by Paul Vögler

   @param i input value
   @return output value
   */
  private double mixDynamicLogarithmicRangeCompression(double i) {
    if (i < -1)
      return -Math.log(-i - 0.85) / 14 - 0.75;
    else if (i > 1)
      return Math.log(i - 0.85) / 14 + 0.75;
    else
      return i / 1.61803398875;
  }

  /**
   Apply logarithmic dynamic range compression to each channels of a frame

   @param frame to operate on
   @return compressed frame
   */
  private double[] mixDynamicLogarithmicRangeCompression(double[] frame) {
    double[] out = new double[outputChannels];
    for (int c = 0; c < outputChannels; c++)
      out[c] = mixDynamicLogarithmicRangeCompression(frame[c]);

    return out;
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
   Enforce a maximum

   @param valueMax   maximum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws MixerException if value greater than allowable
   */
  private void enforceMax(int valueMax, String entityName, int value) throws MixerException {
    if (value > valueMax)
      throw new MixerException("more than " + valueMax + " " + entityName + " not allowed");
  }

  /**
   Enforce a minimum

   @param valueMin   minimum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws MixerException if value less than allowable
   */
  private void enforceMin(int valueMin, String entityName, int value) throws MixerException {
    if (value < valueMin)
      throw new MixerException("less than " + valueMin + " " + entityName + " not allowed");
  }

}


