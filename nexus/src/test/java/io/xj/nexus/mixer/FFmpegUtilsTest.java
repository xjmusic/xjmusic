// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;

import io.xj.nexus.util.InternalResource;
import io.xj.hub.util.ValueException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FFmpegUtilsTest {

  @Test
  void resampleAudio() throws IOException, ValueException {
    String inputAudioFilePath = new InternalResource("/demo_reference_outputs/48000Hz_Signed_32bit_2ch.wav").getFile().getAbsolutePath();
    String outputAudioFilePath = Files.createTempFile("ffmpeg-test-resample-output", ".wav").toAbsolutePath().toString();

    FFmpegUtils.resampleAudio(inputAudioFilePath, outputAudioFilePath, 22000, 8, 1);

    Path generatedFilePath = Paths.get(outputAudioFilePath);
    InternalResource resource = new InternalResource("/test_audio_resampled/ffmpeg-test-resample-output-22000-8bit-1ch.wav");
    byte[] generatedFileBytes = Files.readAllBytes(generatedFilePath);
    byte[] testFileBytes = Files.readAllBytes(resource.getFile().toPath());
    assertArrayEquals(testFileBytes, generatedFileBytes);
  }
}
