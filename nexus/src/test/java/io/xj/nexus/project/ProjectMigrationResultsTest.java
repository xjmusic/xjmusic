package io.xj.nexus.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectMigrationResultsTest {

  @Test
  void addFoldersMoved() {
    var subject = new ProjectMigrationResults();

    subject.addFolders(3);

    assertEquals(3, subject.getFolders());
  }

  @Test
  void addFilesMoved() {
    var subject = new ProjectMigrationResults();

    subject.addFiles(3);

    assertEquals(3, subject.getFiles());
  }

  @Test
  void incrementFoldersMoved() {
    var subject = new ProjectMigrationResults().addFolders(14);

    subject.incrementFolders();

    assertEquals(15, subject.getFolders());
  }

  @Test
  void incrementFilesMoved() {
    var subject = new ProjectMigrationResults().addFiles(52);

    subject.incrementFiles();

    assertEquals(53, subject.getFiles());
  }

  @Test
  void getFoldersMoved() {
    var subject = new ProjectMigrationResults().addFolders(14);

    assertEquals(14, subject.getFolders());
  }

  @Test
  void getFilesMoved() {
    var subject = new ProjectMigrationResults().addFiles(52);

    assertEquals(52, subject.getFiles());
  }

  @Test
  void toStringTest() {
    assertEquals("Moved 52 files and 14 folders", new ProjectMigrationResults().addFolders(14).addFiles(52).toString());
    assertEquals("Moved 14 folders", new ProjectMigrationResults().addFolders(14).toString());
    assertEquals("Moved 52 files", new ProjectMigrationResults().addFiles(52).toString());
    assertEquals("Nothing was moved", new ProjectMigrationResults().toString());
  }
}
