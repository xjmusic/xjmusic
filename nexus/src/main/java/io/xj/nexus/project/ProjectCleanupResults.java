package io.xj.nexus.project;

public class ProjectCleanupResults {
  int folderDeleted;

  int filesDeleted;

  public ProjectCleanupResults(int folderDeleted, int filesDeleted) {
    this.folderDeleted = folderDeleted;
    this.filesDeleted = filesDeleted;
  }

  public ProjectCleanupResults() {
    this.folderDeleted = 0;
    this.filesDeleted = 0;
  }

  public void addFoldersDeleted(int count) {
    this.folderDeleted += count;
  }

  public void addFilesDeleted(int count) {
    this.filesDeleted += count;
  }

  public void incrementFoldersDeleted() {
    this.folderDeleted++;
  }

  public void incrementFilesDeleted() {
    this.filesDeleted++;
  }

  public int getFolderDeleted() {
    return folderDeleted;
  }

  public int getFilesDeleted() {
    return filesDeleted;
  }

  @Override
  public String toString() {
    if (folderDeleted == 0 && filesDeleted == 0) {
      return "No files or folders were removed";
    } else if (folderDeleted == 0) {
      return filesDeleted + " files were removed";
    } else if (filesDeleted == 0) {
      return folderDeleted + " folders were removed";
    } else {
      return folderDeleted + " folders and " + filesDeleted + " files were removed";
    }
  }
}
