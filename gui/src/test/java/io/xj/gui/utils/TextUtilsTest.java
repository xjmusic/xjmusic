package io.xj.gui.utils;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilsTest {
  @Test
  void addTrailingSlash_String() {
    assertEquals("test" + File.separator, TextUtils.addTrailingSlash("test"));
  }
}
