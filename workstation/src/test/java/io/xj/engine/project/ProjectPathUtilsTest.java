package io.xj.engine.project;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectPathUtilsTest {

  @Test
  void matchPrefixAndFileName_projectFile() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator + "MyProject" + File.separator;
    var name = "MyProject";
    var extension = "xj";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals(prefix, matcher.group(1));
    assertEquals(name, matcher.group(2));
    assertEquals(extension, matcher.group(3));
  }

  @Test
  void matchPrefixAndFileName_wavFile() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator;
    var name = "demo";
    var extension = "wav";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals(prefix, matcher.group(1));
    assertEquals(name, matcher.group(2));
    assertEquals(extension, matcher.group(3));
  }

  @Test
  void matchPrefixAndFileName_wavFileWithSpaces() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator;
    var name = "demo with spaces";
    var extension = "wav";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals(prefix, matcher.group(1));
    assertEquals(name, matcher.group(2));
    assertEquals(extension, matcher.group(3));
  }

  @Test
  void getExtension() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator;
    var name = "demo with spaces";
    var extension = "wav";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals(extension, ProjectPathUtils.getExtension(prefix + name + "." + extension));
  }

  @Test
  void getFilename() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator;
    var name = "demo with spaces";
    var extension = "wav";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals("demo with spaces.wav", ProjectPathUtils.getFilename(prefix + name + "." + extension));
  }

  @Test
  void getPrefix() {
    var prefix = File.separator + "Users" + File.separator + "Documents" + File.separator;
    var name = "demo with spaces";
    var extension = "wav";

    var matcher = ProjectPathUtils.matchPrefixNameExtension(prefix + name + "." + extension);

    assertTrue(matcher.find());
    assertEquals(prefix, ProjectPathUtils.getPrefix(prefix + name + "." + extension));
  }
}
