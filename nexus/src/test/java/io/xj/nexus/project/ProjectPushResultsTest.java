package io.xj.nexus.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectPushResultsTest {
  private ProjectPushResults subject;

  @BeforeEach
  void setUp() {
    subject = new ProjectPushResults();
    subject.addTemplates(27);
    subject.addLibraries(31);
    subject.addPrograms(14);
    subject.addInstruments(15);
    subject.addAudios(35);
    subject.addAudiosUploaded(72);
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
    assertEquals(35, subject.getAudios());
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
  void addAudios() {
    subject.addAudios(3);

    assertEquals(38, subject.getAudios());
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
    subject.incrementAudios();

    assertEquals(36, subject.getAudios());
  }

  @Test
  void incrementAudiosUploaded() {
    subject.incrementAudiosUploaded();

    assertEquals(35, subject.getAudios());
  }

  @Test
  void testToString() {
    assertEquals("Synchronized 27 templates, 31 libraries, 14 programs, 15 instruments, 35 audios, and 72 audios uploaded", subject.toString());
  }

  @Test
  void addError_addErrors_hasErrors_getErrors_toString() {
    assertFalse(subject.hasErrors());

    subject.addErrors(Set.of("This is another test", "This is yet another test"));
    subject.addError("This is a test");

    assertTrue(subject.hasErrors());
    assertEquals(3, subject.getErrors().size());
    assertEquals("Synchronized 27 templates, 31 libraries, 14 programs, 15 instruments, 35 audios, and 72 audios uploaded with 3 errors: This is a test, This is another test, and This is yet another test", subject.toString());
  }
}
