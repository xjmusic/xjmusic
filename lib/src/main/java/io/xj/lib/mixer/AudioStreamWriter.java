// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record AudioStreamWriter(double[][] samples, float quality) {

  /**
   Convert output values into a ByteBuffer

   @param fmt     to write
   @param samples [frame][channel] output to convert
   @return byte buffer of stream
   */
  public static ByteBuffer byteBufferOf(AudioFormat fmt, double[][] samples) throws FormatException {
    ByteBuffer outputBytes = ByteBuffer.allocate(samples.length * fmt.getFrameSize());
    for (double[] sample : samples)
      for (double v : sample)
        outputBytes.put(AudioSampleFormat.toBytes(v, AudioSampleFormat.typeOfOutput(fmt)));

    return outputBytes;
  }

  /**
   write output bytes to file

   @param outputFilePath path
   @param specs          format
   @param outputEncoder  container, e.g. WAV or OGG
   @throws IOException on failure
   */
  public void writeToFile(String outputFilePath, AudioFormat specs, OutputEncoder outputEncoder) throws IOException, FormatException {
    File outputFile = new File(outputFilePath);

    switch (outputEncoder) {
      case WAV -> writeWAV(outputFile, specs);
      case OGG -> writeOggVorbis(outputFile, specs, quality);
      default -> throw new IOException("Invalid Output Container!");
    }
  }

  /**
   Write output bytes to WAV container

   @param outputFile to write output to
   @param specs      of output
   @throws IOException on failure
   */
  private void writeWAV(File outputFile, AudioFormat specs) throws IOException, FormatException {
    switch (specs.getEncoding().toString()) {
      case "PCM_SIGNED":
      case "PCM_UNSIGNED":
        break;
      case "PCM_FLOAT":
        throw new FormatException("floating-point .WAV output is not currently supported!");
      default:
        throw new FormatException("unsupported .WAV encoding \"" + specs.getEncoding().toString() + "\" for AudioStreamWriter.writeToFile(...)");
    }

    ByteBuffer outputBytes = byteBufferOf(specs, samples);
    AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(outputBytes.array()), specs, samples.length);
    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
  }

  /**
   Write output bytes to OGG-compressed container

   @param outputFile to write output to
   @param specs      of output
   @throws IOException on failure
   */
  private void writeOggVorbis(File outputFile, AudioFormat specs, float quality) throws IOException {
    new VorbisEncoder(samples, (int) Math.floor(specs.getFrameRate()), quality).encode(new FileOutputStream(outputFile));
  }

}

