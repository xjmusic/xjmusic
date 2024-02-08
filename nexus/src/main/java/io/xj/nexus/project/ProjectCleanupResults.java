package io.xj.nexus.project;

public class ProjectCleanupResults {
  int folders;

  int files;

  public ProjectCleanupResults(int folders, int files) {
    this.folders = folders;
    this.files = files;
  }

  public ProjectCleanupResults() {
    this.folders = 0;
    this.files = 0;
  }

  public void addFolders(int count) {
    this.folders += count;
  }

  public void addFiles(int count) {
    this.files += count;
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
    if (folders==0 && files==0) {
      return "No files or folders were removed";
    } else if (folders==0) {
      return files + " files were removed";
    } else if (files==0) {
      return folders + " folders were removed";
    } else {
      return folders + " folders and " + files + " files were removed";
    }
  }
}
