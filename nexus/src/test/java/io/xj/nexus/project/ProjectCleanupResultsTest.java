package io.xj.nexus.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectCleanupResultsTest {

  @Test
  void addFoldersDeleted() {
    var subject = new ProjectCleanupResults();

    subject.addFolders(3);

    assertEquals(3, subject.getFolders());
  }

  @Test
  void addFilesDeleted() {
    var subject = new ProjectCleanupResults();

    subject.addFiles(3);

    assertEquals(3, subject.getFiles());
  }

  @Test
  void incrementFoldersDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    subject.incrementFolders();

    assertEquals(15, subject.getFolders());
  }

  @Test
  void incrementFilesDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    subject.incrementFiles();

    assertEquals(53, subject.getFiles());
  }

  @Test
  void getFolderDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    assertEquals(14, subject.getFolders());
  }

  @Test
  void getFilesDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    assertEquals(52, subject.getFiles());
  }

  @Test
  void toStringTest() {
    assertEquals("14 folders and 52 files were removed", new ProjectCleanupResults(14, 52).toString());
    assertEquals("14 folders were removed", new ProjectCleanupResults(14, 0).toString());
    assertEquals("52 files were removed", new ProjectCleanupResults(0, 52).toString());
    assertEquals("No files or folders were removed", new ProjectCleanupResults(0, 0).toString());
  }
}
