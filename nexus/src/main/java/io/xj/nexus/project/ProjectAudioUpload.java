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

  private final long contentLength;
  private @Nullable String id;
  private @Nullable HubUploadAuthorization auth;
  private final Collection<String> errors;
  private boolean success = false;

  public ProjectAudioUpload(UUID instrumentAudioId, String pathOnDisk) throws IOException {
    errors = new HashSet<>();
    this.instrumentAudioId = instrumentAudioId;
    this.pathOnDisk = pathOnDisk;
    contentLength = Files.size(Path.of(pathOnDisk));
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

  public HubUploadAuthorization getAuth() {
    Objects.requireNonNull(auth, "Cannot get Authorization before it is set");
    return auth;
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

  public void setAuth(HubUploadAuthorization auth) {
    Objects.requireNonNull(auth, "Authorization cannot be null");
    this.auth = auth;
  }

  public boolean wasSuccessful() {
    return success;
  }

  public long getContentLength() {
    return contentLength;
  }

  public void addError(String error) {
    this.errors.add(error);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public String toString() {
    return wasSuccessful() && Objects.nonNull(auth) ?
      String.format("Uploaded audio OK from %s to Instrument[%s], final waveform key %s", pathOnDisk, instrumentAudioId, auth.getWaveformKey()):
      String.format("Failed to upload audio from %s to Instrument[%s]", pathOnDisk, instrumentAudioId) +
        (hasErrors() ? String.format("with %s %s", errors.size() > 1 ? "errors":"error", StringUtils.toProperCsvAnd(errors.stream().sorted().toList())):"");
  }
<<<<<<< HEAD
=======

  public String getExtension() {
    return ProjectPathUtils.getExtension(pathOnDisk);
  }
>>>>>>> d9f5f915e (Latest updates to project sync)
}
