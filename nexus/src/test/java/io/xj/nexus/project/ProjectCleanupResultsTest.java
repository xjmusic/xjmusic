package io.xj.nexus.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void addEntitiesDeleted() {
    var subject = new ProjectCleanupResults();

    subject.addEntities(3);

    assertEquals(3, subject.getEntities());
  }

  @Test
  void incrementFoldersDeleted() {
    var subject = new ProjectCleanupResults().addFolders(14);

    subject.incrementFolders();

    assertEquals(15, subject.getFolders());
  }

  @Test
  void incrementFilesDeleted() {
    var subject = new ProjectCleanupResults().addFiles(52);

    subject.incrementFiles();

    assertEquals(53, subject.getFiles());
  }

  @Test
  void incrementEntitiesDeleted() {
    var subject = new ProjectCleanupResults().addEntities(52);

    subject.incrementEntities();

    assertEquals(53, subject.getEntities());
  }

  @Test
  void getFoldersDeleted() {
    var subject = new ProjectCleanupResults().addFolders(14);

    assertEquals(14, subject.getFolders());
  }

  @Test
  void getFilesDeleted() {
    var subject = new ProjectCleanupResults().addFiles(52);

    assertEquals(52, subject.getFiles());
  }

  @Test
  void getEntitiesDeleted() {
    var subject = new ProjectCleanupResults().addEntities(52);

    assertEquals(52, subject.getEntities());
  }

  @Test
  void toStringTest() {
    assertEquals("Removed 73 entities, 52 files, and 14 folders", new ProjectCleanupResults().addFolders(14).addFiles(52).addEntities(73).toString());
    assertEquals("Removed 52 files and 14 folders", new ProjectCleanupResults().addFolders(14).addFiles(52).toString());
    assertEquals("Removed 14 folders", new ProjectCleanupResults().addFolders(14).toString());
    assertEquals("Removed 52 files", new ProjectCleanupResults().addFiles(52).toString());
    assertEquals("Nothing was removed", new ProjectCleanupResults().toString());
  }
}
