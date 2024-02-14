package io.xj.nexus.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectAudioUploadTest {
  private final UUID testInstrumentAudioId = UUID.randomUUID();
  private final String pathToAudioFile;
  private ProjectAudioUpload subject;

  public ProjectAudioUploadTest() throws URISyntaxException {
    var baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToAudioFile = baseDir + File.separator + "test-audio.wav";
  }

  @BeforeEach
  void setUp() throws IOException {
    subject = new ProjectAudioUpload(testInstrumentAudioId, pathToAudioFile);
  }

  @Test
  void getExpectedSize() {
    assertEquals(139000, subject.getExpectedSize());
  }

  @Test
  void getInstrumentAudioId() {
    assertEquals(testInstrumentAudioId, subject.getInstrumentAudioId());
  }

  @Test
  void setPathOnDisk_getPathOnDisk() {
    assertEquals(pathToAudioFile, subject.getPathOnDisk());
  }

  @Test
  void setRemoteUrl_getRemoteUrl() {
    var testUrl = "https://example.com/file.wav";

    subject.setRemoteUrl(testUrl);

    assertEquals(testUrl, subject.getRemoteUrl());
  }

  @Test
  void setRemoteUrl_cannotBeNull() {
    assertThrows(NullPointerException.class, () -> subject.setRemoteUrl(null));
  }

  @Test
  void setWaveformKey_getWaveformKey() {
    var testKey = "waveform-key";

    subject.setWaveformKey(testKey);

    assertEquals(testKey, subject.getWaveformKey());
  }

  @Test
  void setWaveformKey_cannotBeNull() {
    assertThrows(NullPointerException.class, () -> subject.setWaveformKey(null));
  }

  @Test
  void addError_hasErrors_getErrors_toString() {
    assertFalse(subject.hasErrors());

    subject.addError("This is a test error");
    subject.addError("This is another test error");

    assertTrue(subject.hasErrors());
    assertTrue(subject.getErrors().contains("This is a test error"));
    assertTrue(subject.getErrors().contains("This is another test error"));
    assertEquals("Failed to upload audio from " + pathToAudioFile + " to Instrument[" + testInstrumentAudioId + "] because This is a test error and This is another test error", subject.toString());
  }

  @Test
  void testToString() {
    subject.setRemoteUrl("https://example.com/file.wav");
    subject.setWaveformKey("test-waveform-key");

    assertEquals("Uploaded audio OK from " + pathToAudioFile + " to Instrument[" + testInstrumentAudioId + "] with final waveform key test-waveform-key", subject.toString());
  }

}
