package io.xj.lib.mixer;

import io.xj.hub.util.InternalResource;
import io.xj.hub.util.ValueException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static io.xj.hub.util.Assertion.assertFileMatchesResourceFile;

class FFmpegUtilsTest {

  @Test
  void resampleAudio() throws IOException, ValueException {
    String inputAudioFilePath = new InternalResource("demo_reference_outputs/48000Hz_Signed_32bit_2ch.wav").getFile().getAbsolutePath();
    String outputAudioFilePath = Files.createTempFile("ffmpeg-test-resample-output", ".wav").toAbsolutePath().toString();

    FFmpegUtils.resampleAudio(inputAudioFilePath, outputAudioFilePath, 22000, 8, 1);

    assertFileMatchesResourceFile("test_audio_resampled/ffmpeg-test-resample-output-22000-8bit-1ch.wav", outputAudioFilePath);
  }
}
