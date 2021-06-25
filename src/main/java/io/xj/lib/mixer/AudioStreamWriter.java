// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import org.sheinbergon.aac.sound.AACFileTypes;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioStreamWriter {
  private static final float DEFAULT_QUALITY = 0.618f;
  private final double[][] stream;
  private final float quality;

  /**
   of a new audio stream writer instance

   @param stream to output
   */
  public AudioStreamWriter(double[][] stream) {
    this.stream = stream;
    this.quality = DEFAULT_QUALITY;
  }

  /**
   of a new audio stream writer instance, with a specific quality setting

   @param stream to output
   */
  public AudioStreamWriter(double[][] stream, float quality) {
    this.stream = stream;
    this.quality = quality;
  }

  /**
   Convert output values into a ByteBuffer

   @param stream       output to convert
   @param totalFrames  to output
   @param outputFormat to wrote
   @return byte buffer of stream
   */
  private static ByteBuffer byteBufferOf(double[][] stream, int totalFrames, AudioFormat outputFormat) throws FormatException {
    ByteBuffer outputBytes = ByteBuffer.allocate(totalFrames * outputFormat.getFrameSize());
    for (int offsetFrame = 0; offsetFrame < totalFrames; offsetFrame++) {
      int streamLength = stream[offsetFrame].length;
      for (int channel = 0; channel < streamLength; channel++) {
        outputBytes.put(AudioSampleFormat.toBytes(stream[offsetFrame][channel], AudioSampleFormat.typeOfOutput(outputFormat)));
      }
    }

    return outputBytes;
  }

  /**
   write output bytes to file

   @param outputFilePath path
   @param specs          format
   @param outputEncoder  container, e.g. WAV or OGG
   @param totalFrames    frames
   @throws IOException on failure
   */
  public void writeToFile(String outputFilePath, AudioFormat specs, OutputEncoder outputEncoder, int totalFrames) throws Exception {
    File outputFile = new File(outputFilePath);

    switch (outputEncoder) {
      case WAV:
        writeWAV(outputFile, specs, totalFrames);
        break;

      case OGG:
        writeOggVorbis(outputFile, specs, quality);
        break;

      case AAC:
        writeAAC(outputFile, specs, totalFrames);
        break;

      default:
        throw new IOException("Invalid Output Container!");
    }
  }

  /**
   Write output bytes to WAV container

   @param outputFile  to write output to
   @param specs       of output
   @param totalFrames to write
   @throws IOException on failure
   */
  private void writeWAV(File outputFile, AudioFormat specs, int totalFrames) throws IOException, FormatException {
    switch (specs.getEncoding().toString()) {
      case "PCM_SIGNED":
      case "PCM_UNSIGNED":
        break;
      case "PCM_FLOAT":
        throw new FormatException("floating-point .WAV output is not currently supported!");
      default:
        throw new FormatException("unsupported .WAV encoding \"" + specs.getEncoding().toString() + "\" for AudioStreamWriter.writeToFile(...)");
    }

    ByteBuffer outputBytes = byteBufferOf(stream, totalFrames, specs);
    AudioInputStream ais = new AudioInputStream(
      new ByteArrayInputStream(outputBytes.array()), specs,
      totalFrames
    );
    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
  }

  /**
   Write output bytes to OGG-compressed container

   @param outputFile to write output to
   @param specs      of output
   @throws IOException on failure
   */
  private void writeOggVorbis(File outputFile, AudioFormat specs, float quality) throws IOException {
    new VorbisEncoder(stream, (int) Math.floor(specs.getFrameRate()), quality).encode(new FileOutputStream(outputFile));
  }

  /**
   [#162361712] Write output bytes to AAC-compressed container

   @param outputFile to write output to
   @param specs      of output
   @throws IOException on failure
   */
  private void writeAAC(File outputFile, AudioFormat specs, int totalFrames) throws IOException, FormatException {
    ByteBuffer outputBytes = byteBufferOf(stream, totalFrames, specs);
    AudioInputStream ais = new AudioInputStream(
      new ByteArrayInputStream(outputBytes.array()), specs,
      totalFrames
    );
    AudioSystem.write(ais, AACFileTypes.AAC_HE_V2, outputFile);
  }

}

