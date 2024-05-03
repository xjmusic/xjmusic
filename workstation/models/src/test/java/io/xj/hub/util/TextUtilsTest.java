package io.xj.hub.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilsTest {
  @Test
  void addTrailingSlash_String() {
    assertEquals("test" + File.separator, LocalFileUtils.addTrailingSlash("test"));
  }
}
