package io.xj.nexus.project;

import io.xj.nexus.util.FormatUtils;

import java.util.Map;

public class ProjectMigrationResults {
  int folders;
  int files;

  public ProjectMigrationResults() {
    this.folders = 0;
    this.files = 0;
  }

  public ProjectMigrationResults addFolders(int count) {
    this.folders += count;
    return this;
  }

  public ProjectMigrationResults addFiles(int count) {
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
      return "Nothing was moved";
    } else {
      return "Moved " + FormatUtils.describeCounts(Map.of(
        "files", files,
        "folders", folders
      ));
    }
  }
}
