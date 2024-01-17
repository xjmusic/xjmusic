package io.xj.gui.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextParsingUtilsTest {
  @Test
  void addTrailingSlash_String() {
    assertEquals("test" + File.separator, TextParsingUtils.addTrailingSlash("test"));
  }
}
