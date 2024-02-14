package io.xj.nexus.project;

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
  private @Nullable String remoteUrl;
  private @Nullable String waveformKey;
  private final Collection<String> errors;

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

  public @Nullable String getRemoteUrl() {
    return remoteUrl;
  }

  public @Nullable String getWaveformKey() {
    return waveformKey;
  }

  public Collection<String> getErrors() {
    return errors;
  }

  public long getExpectedSize() {
    return expectedSize;
  }

  public void setRemoteUrl(String remoteUrl) {
    Objects.requireNonNull(remoteUrl, "Remote URL cannot be set with null value");
    this.remoteUrl = remoteUrl;
  }

  public void setWaveformKey(String waveformKey) {
    Objects.requireNonNull(waveformKey, "Waveform key cannot be set with null value");
    this.waveformKey = waveformKey;
  }

  public void addError(String error) {
    this.errors.add(error);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public String toString() {
    return hasErrors() ?
      String.format("Failed to upload audio from %s to Instrument[%s] because %s", pathOnDisk, instrumentAudioId, StringUtils.toProperCsvAnd(errors.stream().sorted().toList())):
      String.format("Uploaded audio OK from %s to Instrument[%s] with final waveform key %s", pathOnDisk, instrumentAudioId, waveformKey);
  }

}
