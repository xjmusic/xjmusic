package io.xj.nexus.project;

import io.xj.nexus.util.FormatUtils;

import java.util.Map;

public class ProjectCleanupResults {
  int folders;
  int files;

  public ProjectCleanupResults() {
    this.folders = 0;
    this.files = 0;
  }

  public ProjectCleanupResults addFolders(int count) {
    this.folders += count;
    return this;
  }

  public ProjectCleanupResults addFiles(int count) {
    this.files += count;
    return this;
  }

  public void incrementFolders() {
    this.folders++;
  }

  public void incrementFiles() {
    this.files++;
  }

  public int getFolders() {
    return folders;
  }

  public int getFiles() {
    return files;
  }

  @Override
  public String toString() {
    if (folders == 0 && files == 0) {
      return "Nothing was removed";
    } else {
      return "Removed " + FormatUtils.describeCounts(Map.of(
        "files", files,
        "folders", folders
      ));
    }
  }
}
