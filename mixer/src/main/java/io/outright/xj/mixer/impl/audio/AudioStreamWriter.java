// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer.impl.audio;

import io.outright.xj.mixer.impl.exception.FormatException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
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

   @param outputFilePath path
   @param outputFormat   format
   @param totalFrames    frames
   @throws IOException on failure
   */
  public void writeToFile(String outputFilePath, AudioFormat outputFormat, long totalFrames) throws Exception {
    switch (outputFormat.getEncoding().toString()) {
      case "PCM_SIGNED":
      case "PCM_UNSIGNED":
        writeAudioInputStreamToFile(outputFilePath, outputFormat, totalFrames);
        break;
      case "PCM_FLOAT":
        throw new FormatException("floating-point output is not currently supported! See [#137] Support for floating-point output encoding.");
//        writeDirectToFile(outputFilePath, outputFormat, totalFrames);
//        break;
      default:
        throw new FormatException("unsupported encoding \"" + outputFormat.getEncoding().toString() + "\" for AudioStreamWriter.writeToFile(...)");
    }
  }

  /**
   use AudioInputStream method to write output bytes to file

   @param outputFilePath path
   @param outputFormat   format
   @param totalFrames    frames
   @throws IOException on failure
   */
  private void writeAudioInputStreamToFile(String outputFilePath, AudioFormat outputFormat, long totalFrames) throws IOException {
    AudioInputStream ais = new AudioInputStream(
      new ByteArrayInputStream(outputBytes.array()), outputFormat,
      totalFrames
    );
    File outputFile = new File(outputFilePath);
    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
  }

  /**
   use direct method to write output bytes to file

   @param outputFilePath path
   @param outputFormat   format
   @param totalFrames    frames
   @throws IOException on failure
   */
  private void writeDirectToFile(String outputFilePath, AudioFormat outputFormat, long totalFrames)
    throws Exception {

    DataOutputStream outputStream = new DataOutputStream(
      new BufferedOutputStream(new FileOutputStream(outputFilePath)));

    writeStreamHeader(outputStream, outputFormat, totalFrames);
    outputStream.write(outputBytes.array());
  }

  /**
   Write the format chunk to RIFF
   See the file `docs/Microsoft_WAVE_format.pdf` contained in this module.
   <p>
   The "WAVE" format consists of two subchunks: "fmt " and "data".
   The "fmt " subchunk describes the audio format and data encoding.
   The "data" subchunk contains the size of the data and the actual sound.

   @param outputStream target output stream
   @param outputFormat format
   @throws IOException on failure
   */
  private void writeStreamHeader(DataOutputStream outputStream, AudioFormat outputFormat, long totalFrames)
    throws Exception {

    //
    // [at byte]    [length]
    //
    // [0]          [4]
    outputStream.writeBytes("RIFF");
    // Header indicates this is a RIFF file
    //
    // [4]          [4]
    outputStream.writeInt(36 + sizeSubChunk2(outputFormat, totalFrames));
    // == 36 + SubChunk2Size, or more precisely: 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
    // This is the size of the rest of the chunk following this number.  This is the size of the
    // entire file in bytes minus 8 bytes for the two fields not included in this count:
    // ChunkID and ChunkSize.
    //
    // [8]          [4]
    outputStream.writeBytes("WAVE");
    // Header indicates this is a WAVE-type RIFF file
    //
    // [12]         [4]
    outputStream.writeBytes("fmt ");
    // Header begins the "fmt " subchunk a.k.a. SubChunk1
    //
    // [16]         [4]
    outputStream.writeInt(16);
    // This is the size of the rest of SubChunk1 which follows this number.
    //
    // [20]         [2]
    outputStream.writeShort(formatCode(outputFormat));
    // AudioFormat SIGNED/UNSIGNED INT = 1
    //             FLOAT = 3
    //
    // [22]         [2]
    outputStream.writeShort(outputFormat.getChannels());
    // NumChannels, Mono = 1, Stereo = 2, etc.
    //
    // [24]         [4]
    outputStream.writeInt(sampleRate(outputFormat));
    // SampleRate 8000, 44100, etc.
    //
    // [28]         [4]
    outputStream.writeInt(outputFormat.getFrameSize() * sampleRate(outputFormat));
    // ByteRate == SampleRate * NumChannels * BitsPerSample/8
    //
    // [32]         [2]
    outputStream.writeShort(outputFormat.getFrameSize());
    // BlockAlign == NumChannels * BitsPerSample/8
    // The number of bytes for one sample including all channels.
    //
    // [34]         [2]
    outputStream.writeShort(outputFormat.getSampleSizeInBits());
    // BitsPerSample, 8 bits = 8, 16 bits = 16, etc.
    //
    // [36]         [4]
    outputStream.writeBytes("data");
    // Header begins the "data" subchunk a.k.a. SubChunk2
    //
    // [40]         [4]
    outputStream.writeInt(sizeSubChunk2(outputFormat, totalFrames));
    // Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8
    // This is the number of bytes in the data. You can also think of this as the size
    // of the read of the subchunk following this number.
    //
    // [44]         [*]
    // Data The actual sound data follows...
  }

  /**
   Get the size of SubChunk2 for a given WAVE spec

   @param outputFormat of WAVE
   @param totalFrames  to output as WAVE
   @return size in bytes
   @throws FormatException if unable to compute
   */
  private int sizeSubChunk2(AudioFormat outputFormat, long totalFrames) throws FormatException {
    long sizeSubChunk2 = totalFrames * outputFormat.getFrameSize();
    if (sizeSubChunk2 > 2147483647) {
      throw new FormatException("Maximum supported output size of a RIFF container is 2,147,483,647 bytes");
    }
    return (int) sizeSubChunk2;
  }

  /**
   Get integer value sample rate from audio format, or throw exception

   @param outputFormat to get sample rate from
   @return sample rate integer
   @throws FormatException if non-integer
   */
  private int sampleRate(AudioFormat outputFormat) throws FormatException {
    float sampleRateFloat = outputFormat.getSampleRate();
    int sampleRateInt = (int) Math.floor(sampleRateFloat);
    if ((float) sampleRateInt != sampleRateFloat) {
      throw new FormatException("Currently, non-integer sample/frame rate Hz are supported. " +
        "Was asked to render " + String.valueOf(sampleRateFloat) + "Hz; Bailing out!");
    }
    return sampleRateInt;
  }

  /**
   Get the RIFF format code for an audio format

   @param outputFormat to get code from
   @return code
   */
  private short formatCode(AudioFormat outputFormat) {
    switch (outputFormat.getEncoding().toString()) {
      case "PCM_SIGNED":
      case "PCM_UNSIGNED":
        return WAVE_FORMAT_PCM;
      case "PCM_FLOAT":
        return WAVE_FORMAT_IEEE_FLOAT;
      default:
        return -1;
    }
  }

}

