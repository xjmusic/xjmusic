// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.audio;

import io.xj.mixer.OutputContainer;
import io.xj.mixer.impl.exception.FormatException;

import de.sciss.jump3r.lowlevel.LameEncoder;

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
  private final ByteBuffer outputBytes;

  // RIFF format values
  private static final int WAVE_FORMAT_PCM = 0x0001; // PCM
  private static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003; // IEEE float
//  private static final int WAVE_FORMAT_ALAW = 0x0006; // 8-bit ITU-T G.711 A-law
//  private static final int WAVE_FORMAT_MULAW = 0x0007; // 8-bit ITU-T G.711 µ-law
//  private static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE; // Determined by SubFormat

  /**
   create a new audio stream writer instance

   @param outputBytes buffer of bytes to output
   */
  public AudioStreamWriter(ByteBuffer outputBytes) {
    this.outputBytes = outputBytes;
  }

  /**
   write output bytes to file

   @param outputFilePath  path
   @param outputFormat    format
   @param outputContainer container, e.g. WAV or MP3
   @param totalFrames     frames
   @throws IOException on failure
   */
  public void writeToFile(String outputFilePath, AudioFormat outputFormat, OutputContainer outputContainer, long totalFrames) throws Exception {
    switch (outputFormat.getEncoding().toString()) {
      case "PCM_SIGNED":
      case "PCM_UNSIGNED":
        writeAudioInputStreamToFile(outputContainer, outputFilePath, outputFormat, totalFrames);
        break;
      case "PCM_FLOAT":
        throw new FormatException("floating-point output is not currently supported!");
//        writeDirectToFile(outputFilePath, outputFormat, totalFrames);
//        break;
      default:
        throw new FormatException("unsupported encoding \"" + outputFormat.getEncoding().toString() + "\" for AudioStreamWriter.writeToFile(...)");
    }
  }

  /**
   use AudioInputStream method to write output bytes to file

   @param outputContainer container, e.g. WAV or MP3
   @param outputFilePath  path
   @param outputFormat    format
   @param totalFrames     frames
   @throws IOException on failure
   */
  private void writeAudioInputStreamToFile(OutputContainer outputContainer, String outputFilePath, AudioFormat outputFormat, long totalFrames) throws IOException {
    File outputFile = new File(outputFilePath);

    if (outputContainer.equals(OutputContainer.WAV))
      writeWAV(outputFile, outputFormat, totalFrames);

    else if (outputContainer.equals(OutputContainer.MP3))
      writeMP3(outputFile, outputFormat);

    else throw new IOException("Invalid Output Container!");
  }

  /**
   Write output bytes to WAV container

   @param outputFile   to write output to
   @param outputFormat of output
   @param totalFrames  to write
   @throws IOException on failure
   */
  private void writeWAV(File outputFile, AudioFormat outputFormat, long totalFrames) throws IOException {
    AudioInputStream ais = new AudioInputStream(
      new ByteArrayInputStream(outputBytes.array()), outputFormat,
      totalFrames
    );
    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
  }

  /**
   Write output bytes to MP3-compressed container

   @param outputFile   to write output to
   @param outputFormat of output
   @throws IOException on failure
   */
  private void writeMP3(File outputFile, AudioFormat outputFormat) throws IOException {
    LameEncoder encoder = new LameEncoder(outputFormat, 256, LameEncoder.CHANNEL_MODE_AUTO, LameEncoder.QUALITY_HIGHEST, false);

    byte[] pcm = outputBytes.array();

    FileOutputStream out = new FileOutputStream(outputFile);
    byte[] buffer = new byte[encoder.getPCMBufferSize()];

    int bytesToTransfer = Math.min(buffer.length, pcm.length);
    int bytesWritten;
    int currentPcmPosition = 0;
    while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
      currentPcmPosition += bytesToTransfer;
      bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

      out.write(buffer, 0, bytesWritten);
    }

    encoder.close();


  }

}

