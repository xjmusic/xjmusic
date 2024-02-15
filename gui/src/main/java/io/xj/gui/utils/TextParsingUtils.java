package io.xj.gui.utils;

import javafx.scene.control.TextField;

import java.io.File;

public enum TextParsingUtils {
  ;

  /**
   Add slash to end of file path prefix of text field
   https://www.pivotaltracker.com/story/show/186555998

   @param textField in which to add a trailing slash
   */
  public static void addTrailingSlash(TextField textField) {
    String text = textField.getText();
    if (!text.endsWith(File.separator)) {
      textField.setText(text + File.separator);
    }
  }

  /**
   Add slash to end of file path prefix of string
   https://www.pivotaltracker.com/story/show/186555998

   @param text in which to add a trailing slash
   */
  public static String addTrailingSlash(String text) {
    return text.endsWith(File.separator) ? text:text + File.separator;
  }
}
