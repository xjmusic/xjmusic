package io.xj.engine.audio;

import io.xj.engine.mixer.AudioSampleFormat;
import io.xj.engine.mixer.FormatException;
import io.xj.model.pojos.InstrumentAudio;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

public class AudioLoaderImpl implements AudioLoader {

  private static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final int READ_BUFFER_BYTE_SIZE = 1024;

  public AudioLoaderImpl(
  ) {
  }

  @Override
  public AudioInMemory load(InstrumentAudio audio, String path) throws IOException, UnsupportedAudioFileException {
    AudioFormat format = AudioSystem.getAudioFileFormat(new File(path)).getFormat();
    return load(audio, path, format);
  }

  @Override
  public AudioInMemory load(InstrumentAudio instrumentAudio, String path, AudioFormat format) throws IOException, UnsupportedAudioFileException {
    try (
      var fileInputStream = FileUtils.openInputStream(new File(path));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = format.getFrameSize();
      var channels = format.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE == expectBytes)
        throw new IOException("loading audio streams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == audioInputStream.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / format.getFrameSize();
      } else {
        expectFrames = (int) audioInputStream.getFrameLength();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new IOException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(format);

      // buffer size always a multiple of frame size
      int actualReadBufferSize = (int) (Math.floor((double) READ_BUFFER_BYTE_SIZE / frameSize) * frameSize);

      int b; // iterator: byte
      int tc; // iterators: source channel, target channel
      int sf = 0; // current source frame
      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[sampleSize];
      byte[] readBuffer = new byte[actualReadBufferSize];
      float[][] data = new float[expectFrames][channels];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer && sf < data.length; b += frameSize) {
          for (tc = 0; tc < format.getChannels(); tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            data[sf][tc] = (float) AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
          }
          sf++;
        }
      }
      return new AudioInMemory(instrumentAudio, format, path, data);

    } catch (UnsupportedAudioFileException | FormatException e) {
      throw new IOException(String.format("Failed to read and compute float array for file %s", path), e);
    }
  }

}
