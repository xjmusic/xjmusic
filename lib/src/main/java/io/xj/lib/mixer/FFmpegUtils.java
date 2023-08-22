package io.xj.lib.mixer;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public interface FFmpegUtils {
  Logger LOG = LoggerFactory.getLogger(FFmpegUtils.class);

  /**
   * Convert the audio file to the target sample rate
   *
   * @param inputAudioFilePath  path to input audio file
   * @param outputAudioFilePath path to output audio file
   * @param targetSampleRate    target sample rate
   * @param targetSampleBits    target sample bits
   * @param targetChannels      target channels
   * @throws RuntimeException if unable to convert audio
   */
  static void resampleAudio(String inputAudioFilePath, String outputAudioFilePath, int targetSampleRate, int targetSampleBits, int targetChannels) throws RuntimeException {
    try (FFmpegFrameGrabber input = new FFmpegFrameGrabber(inputAudioFilePath)) {

      input.start();
      if (input.getAudioChannels() <= 0) {
        throw new RuntimeException("No audio channels found in the input file.");
      }

      try (FFmpegFrameRecorder output = new FFmpegFrameRecorder(outputAudioFilePath, targetChannels)) {
        output.setSampleFormat(computeSampleFormat(targetSampleBits));
        output.setAudioCodec(computeAudioCodec(targetSampleBits));
        output.setSampleRate(targetSampleRate);
        output.start();

        Frame frame;
        while ((frame = input.grabFrame(true, false, true, true)) != null) {
          if (0 < frame.samples.length && Objects.nonNull(frame.samples[0])) {
            output.record(frame);
          }
        }

        output.stop();
        input.stop();
        LOG.info("Did resample audio file {} to {}Hz {}-bit {}-channel", outputAudioFilePath, output.getSampleRate(), targetSampleBits, targetChannels);

      } catch (IOException e) {
        LOG.error("Unable to resample audio file: {}", inputAudioFilePath, e);
        throw new RuntimeException(String.format("Unable to resample audio file: %s", inputAudioFilePath));
      }
    } catch (IOException e) {
      LOG.error("Unable to resample audio file: {}", inputAudioFilePath, e);
      throw new RuntimeException(String.format("Unable to resample audio file: %s", inputAudioFilePath));
    }
  }

  static int computeAudioCodec(int targetSampleBits) {
    return switch (targetSampleBits) {
      case 8 -> avcodec.AV_CODEC_ID_PCM_U8;
      case 16 -> avcodec.AV_CODEC_ID_PCM_S16LE;
      case 32 -> avcodec.AV_CODEC_ID_PCM_S32LE;
      case 64 -> avcodec.AV_CODEC_ID_PCM_S64LE;
      case 32 | 64 -> avcodec.AV_CODEC_ID_PCM_F32LE;
      case 64 | 128 -> avcodec.AV_CODEC_ID_PCM_F64LE;
      default -> throw new RuntimeException(String.format("Unsupported sample bits: %d", targetSampleBits));
    };
  }

  static int computeSampleFormat(int targetSampleBits) {
    return switch (targetSampleBits) {
      case 8 -> avutil.AV_SAMPLE_FMT_U8;
      case 16 -> avutil.AV_SAMPLE_FMT_S16;
      case 32 -> avutil.AV_SAMPLE_FMT_S32;
      case 64 -> avutil.AV_SAMPLE_FMT_S64;
      case 32 | 64 -> avutil.AV_SAMPLE_FMT_FLT;
      case 64 | 128 -> avutil.AV_SAMPLE_FMT_DBL;
      default -> throw new RuntimeException(String.format("Unsupported sample bits: %d", targetSampleBits));
    };
  }
}
