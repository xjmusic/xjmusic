package io.xj.engine.project;

import io.xj.model.HubUploadAuthorization;
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
  void getContentLength() {
    assertEquals(139000, subject.getContentLength());
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
  void setAuth_getAuth() {
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setWaveformKey("test-waveform-key");
    subject.setAuth(authorization);

    assertEquals("test-waveform-key", subject.getAuth().getWaveformKey());
  }

  @Test
  void getAuth_failsBeforeSettingAuthorization() {
    var e = assertThrows(NullPointerException.class, subject::getAuth);

    assertEquals("Cannot get Authorization before it is set", e.getMessage());
  }

  @Test
  void setAuthorization_cannotBeNull() {
    var e = assertThrows(NullPointerException.class, () -> subject.setAuth(null));

    assertEquals("Authorization cannot be null", e.getMessage());
  }

  @Test
  void addError_hasErrors_getErrors_toString() {
    assertFalse(subject.hasErrors());

    subject.addError("This is a test error");
    subject.addError("This is another test error");

    assertTrue(subject.hasErrors());
    assertTrue(subject.getErrors().contains("This is a test error"));
    assertTrue(subject.getErrors().contains("This is another test error"));
    assertEquals("Failed to upload audio from " + pathToAudioFile + " to Instrument[" + testInstrumentAudioId + "] with errors This is a test error and This is another test error", subject.toString());
  }

  @Test
  void testToString() {
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setWaveformKey("test-waveform-key");
    subject.setAuth(authorization);
    subject.setSuccess(true);

    assertEquals("Uploaded audio OK from " + pathToAudioFile + " to Instrument[" + testInstrumentAudioId + "], final waveform key test-waveform-key", subject.toString());
  }

  @Test
  void wasSuccessful() {
    assertFalse(subject.wasSuccessful());

    subject.setSuccess(true);

    assertTrue(subject.wasSuccessful());
  }

  @Test
  void setId_getId() {
    subject.setId("test-id");

    assertEquals("test-id", subject.getId());
  }

  @Test
  void getId_failsBeforeSetId() {
    var e = assertThrows(NullPointerException.class, subject::getId);

    assertEquals("Cannot get ID before it is set", e.getMessage());

    assertEquals("wav", subject.getExtension());
  }

}
