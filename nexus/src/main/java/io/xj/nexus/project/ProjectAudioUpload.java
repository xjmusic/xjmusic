package io.xj.nexus.project;

import io.xj.hub.HubUploadAuthorization;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class ProjectAudioUpload {
  private final UUID instrumentAudioId;
  private final String pathOnDisk;

  private final long expectedSize;
  private @Nullable String id;
  private @Nullable HubUploadAuthorization authorization;
  private final Collection<String> errors;
  private boolean success = false;

  public ProjectAudioUpload(UUID instrumentAudioId, String pathOnDisk) throws IOException {
    errors = new HashSet<>();
    this.instrumentAudioId = instrumentAudioId;
    this.pathOnDisk = pathOnDisk;
    expectedSize = Files.size(Path.of(pathOnDisk));
  }

  public UUID getInstrumentAudioId() {
    return instrumentAudioId;
  }

  public String getPathOnDisk() {
    return pathOnDisk;
  }

  public String getId() {
    Objects.requireNonNull(id, "Cannot get ID before it is set");
    return id;
  }

  public String getWaveformKey() {
    Objects.requireNonNull(authorization, "Cannot get Waveform Key before Authorization is set");
    return authorization.getWaveformKey();
  }

  public String getBucketName() {
    Objects.requireNonNull(authorization, "Cannot get Bucket Name before Authorization is set");
    return authorization.getBucketName();
  }

  public String getBucketRegion() {
    Objects.requireNonNull(authorization, "Cannot get Bucket Region before Authorization is set");
    return authorization.getBucketRegion();
  }

  public Collection<String> getErrors() {
    return errors;
  }

  public void setId(String id) {
    Objects.requireNonNull(id, "Cannot get ID before it is set");
    this.id = id;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public void setAuthorization(HubUploadAuthorization authorization) {
    Objects.requireNonNull(authorization, "Authorization cannot be null");
    this.authorization = authorization;
  }

  public boolean wasSuccessful() {
    return success;
  }

  public long getExpectedSize() {
    return expectedSize;
  }

  public void addError(String error) {
    this.errors.add(error);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public String toString() {
    return wasSuccessful() && Objects.nonNull(authorization) ?
      String.format("Uploaded audio OK from %s to Instrument[%s], final waveform key %s", pathOnDisk, instrumentAudioId, authorization.getWaveformKey()):
      String.format("Failed to upload audio from %s to Instrument[%s]", pathOnDisk, instrumentAudioId) +
        (hasErrors() ? String.format("with %s %s", errors.size() > 1 ? "errors":"error", StringUtils.toProperCsvAnd(errors.stream().sorted().toList())):"");
  }
}
