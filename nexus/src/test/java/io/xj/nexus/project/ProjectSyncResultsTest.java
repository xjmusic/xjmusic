package io.xj.nexus.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectSyncResultsTest {
  private ProjectSyncResults subject;

  @BeforeEach
  void setUp() {
    subject = new ProjectSyncResults(27,31,14,15,36,72);
  }

  @Test
  void getTemplates() {
    assertEquals(27, subject.getTemplates());
  }

  @Test
  void getLibraries() {
    assertEquals(31, subject.getLibraries());
  }

  @Test
  void getPrograms() {
    assertEquals(14, subject.getPrograms());
  }

  @Test
  void getInstruments() {
    assertEquals(15, subject.getInstruments());
  }

  @Test
  void getAudiosDownloaded() {
    assertEquals(36, subject.getAudiosDownloaded());
  }

  @Test
  void getAudiosUploaded() {
    assertEquals(72, subject.getAudiosUploaded());
  }

  @Test
  void addTemplates() {
    subject.addTemplates(3);

    assertEquals(30, subject.getTemplates());
  }

  @Test
  void addLibraries() {
    subject.addLibraries(3);

    assertEquals(34, subject.getLibraries());
  }

  @Test
  void addPrograms() {
    subject.addPrograms(3);

    assertEquals(17, subject.getPrograms());
  }

  @Test
  void addInstruments() {
    subject.addInstruments(3);

    assertEquals(18, subject.getInstruments());
  }

  @Test
  void addAudiosDownloaded() {
    subject.addAudiosDownloaded(3);

    assertEquals(39, subject.getAudiosDownloaded());
  }

  @Test
  void addAudiosUploaded() {
    subject.addAudiosUploaded(3);

    assertEquals(75, subject.getAudiosUploaded());
  }

  @Test
  void incrementTemplates() {
    subject.incrementTemplates();

    assertEquals(28, subject.getTemplates());
  }

  @Test
  void incrementLibraries() {
    subject.incrementLibraries();

    assertEquals(32, subject.getLibraries());
  }

  @Test
  void incrementPrograms() {
    subject.incrementPrograms();

    assertEquals(15, subject.getPrograms());
  }

  @Test
  void incrementInstruments() {
    subject.incrementInstruments();

    assertEquals(16, subject.getInstruments());
  }

  @Test
  void incrementAudiosDownloaded() {
    subject.incrementAudiosDownloaded();

    assertEquals(37, subject.getAudiosDownloaded());
  }

  @Test
  void incrementAudiosUploaded() {
    subject.incrementAudiosUploaded();

    assertEquals(73, subject.getAudiosUploaded());
  }

  @Test
  void testToString() {
    assertEquals("Synchronized 27 templates, 31 libraries, 14 programs, 15 instruments, 36 audios downloaded, and 72 audios uploaded", subject.toString());
  }
}
