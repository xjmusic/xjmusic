package io.xj.nexus.project;

import io.xj.hub.HubUploadAuthorization;
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
  void setAuthorization_getWaveformKey() {
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setWaveformKey("test-waveform-key");
    subject.setAuthorization(authorization);

    assertEquals("test-waveform-key", subject.getWaveformKey());
  }

  @Test
  void setAuthorization_cannotBeNull() {
    var e = assertThrows(NullPointerException.class, () -> subject.setAuthorization(null));

    assertEquals("authorization", e.getMessage());
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
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setWaveformKey("test-waveform-key");
    subject.setAuthorization(authorization);

    assertEquals("Uploaded audio OK from " + pathToAudioFile + " to Instrument[" + testInstrumentAudioId + "] with final waveform key test-waveform-key", subject.toString());
  }

  @Test
  void wasSuccessful() {
    assertFalse(subject.wasSuccessful());

    subject.setSuccess(true);

    assertTrue(subject.wasSuccessful());
  }

  @Test
  void getBucketName() {
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setBucketName("test-bucket-name");
    subject.setAuthorization(authorization);

    assertEquals("test-bucket-name", subject.getBucketName());
  }

  @Test
  void getBucketName_failsBeforeSettingAuthorization() {
    var e = assertThrows(NullPointerException.class, subject::getBucketName);

    assertEquals("Cannot get Bucket Name before Authorization is set", e.getMessage());
  }

  @Test
  void getBucketRegion() {
    HubUploadAuthorization authorization = new HubUploadAuthorization();
    authorization.setBucketRegion("test-bucket-region");
    subject.setAuthorization(authorization);

    assertEquals("test-bucket-region", subject.getBucketRegion());
  }

  @Test
  void getBucketRegion_failsBeforeSettingAuthorization() {
    var e = assertThrows(NullPointerException.class, subject::getBucketRegion);

    assertEquals("Cannot get Bucket Region before Authorization is set", e.getMessage());
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
  }

}
