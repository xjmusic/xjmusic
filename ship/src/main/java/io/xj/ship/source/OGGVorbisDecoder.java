// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.api.client.util.Lists;
import io.xj.lib.mixer.AudioSampleFormat;
import io.xj.lib.mixer.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 An HTTP Live Streaming Media Segment
 <p>
 SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 <p>
 SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class OGGVorbisDecoder {
  private static final Logger LOG = LoggerFactory.getLogger(OGGVorbisDecoder.class);
  private static final int READ_BUFFER_BYTE_SIZE = 4096;
  private final InputStream inputStream;
  private final List<double[]> pcmData = Lists.newArrayList(); /* [frame][channel] */
  private AudioFormat audioFormat;

  /**
   Create an OGG decoder from the input stream

   @param inputStream to decode
   */
  private OGGVorbisDecoder(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   Create an OGG decoder from the input stream

   @param inputStream to decode
   */
  public static OGGVorbisDecoder decode(InputStream inputStream) {
    var ogg = new OGGVorbisDecoder(inputStream);
    ogg.decode();
    return ogg;
  }

  /**
   Get the target audio format

   @return target audio format
   */
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }

  private void decode() {
    if (!pcmData.isEmpty())
      LOG.error("Cannot decode data twice!");

    else try {
      AudioInputStream in = AudioSystem.getAudioInputStream(inputStream);
      if (in != null) {
        AudioFormat baseFormat = in.getFormat();

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
          16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

        var frameSize = audioFormat.getFrameSize();
        if (2 != audioFormat.getChannels()) throw new FormatException("Must be stereo!");
        var sampleSize = frameSize / 2;

        AudioInputStream dataIn = AudioSystem.getAudioInputStream(audioFormat, in);
        AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(audioFormat);

        int numBytesReadToBuffer;
        double v0, v1;
        byte[] sampleBuffer = new byte[sampleSize];
        byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
        while (-1 != (numBytesReadToBuffer = dataIn.read(readBuffer, 0, READ_BUFFER_BYTE_SIZE))) {
          for (int b = 0; b < numBytesReadToBuffer; b += frameSize) {
            System.arraycopy(readBuffer, b, sampleBuffer, 0, sampleSize);
            v0 = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            System.arraycopy(readBuffer, b + sampleSize, sampleBuffer, 0, sampleSize);
            v1 = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            pcmData.add(new double[]{v0, v1});
          }
        }

        dataIn.close();
        in.close();
      }
    } catch (IOException | UnsupportedAudioFileException | FormatException e) {
      LOG.error("Failed to decode audio!", e);
    }
  }

  /**
   Get an array of the final PCM data

   @return PCM data
   */
  public List<double[]> getPcmData() {
    return pcmData;
  }

}
