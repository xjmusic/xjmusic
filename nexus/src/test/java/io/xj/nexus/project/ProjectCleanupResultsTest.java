package io.xj.nexus.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectCleanupResultsTest {

  @Test
  void addFoldersDeleted() {
    var subject = new ProjectCleanupResults();

    subject.addFoldersDeleted(3);

    assertEquals(3, subject.getFolderDeleted());
  }

  @Test
  void addFilesDeleted() {
    var subject = new ProjectCleanupResults();

    subject.addFilesDeleted(3);

    assertEquals(3, subject.getFilesDeleted());
  }

  @Test
  void incrementFoldersDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    subject.incrementFoldersDeleted();

    assertEquals(15, subject.getFolderDeleted());
  }

  @Test
  void incrementFilesDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    subject.incrementFilesDeleted();

    assertEquals(53, subject.getFilesDeleted());
  }

  @Test
  void getFolderDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    assertEquals(14, subject.getFolderDeleted());
  }

  @Test
  void getFilesDeleted() {
    var subject = new ProjectCleanupResults(14, 52);

    assertEquals(52, subject.getFilesDeleted());
  }

  @Test
  void toStringTest() {
    assertEquals("14 folders and 52 files were removed", new ProjectCleanupResults(14, 52).toString());
    assertEquals("14 folders were removed", new ProjectCleanupResults(14, 0).toString());
    assertEquals("52 files were removed", new ProjectCleanupResults(0, 52).toString());
    assertEquals("No files or folders were removed", new ProjectCleanupResults(0, 0).toString());
  }
}
