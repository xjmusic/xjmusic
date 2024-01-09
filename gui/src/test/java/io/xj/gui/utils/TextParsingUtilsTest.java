package io.xj.gui.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextParsingUtilsTest {

  @BeforeAll
  public static void setUpClass() {
    Platform.startup(() -> {
    });
  }

  @Test
  void addTrailingSlash_TextField() {
    TextField textField = new TextField("test");

    TextParsingUtils.addTrailingSlash(textField);

    assertEquals("test" + File.separator, textField.getText());
  }

  @Test
  void addTrailingSlash_String() {
    assertEquals("test" + File.separator, TextParsingUtils.addTrailingSlash("test"));
  }
}
