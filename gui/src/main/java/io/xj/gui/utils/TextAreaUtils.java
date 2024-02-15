package io.xj.gui.utils;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public enum TextAreaUtils {
  ;

  public static double getContentHeight(TextArea textArea) {
    ScrollPane sp = (ScrollPane) textArea.lookup(".scroll-pane");
    if (sp!=null) {
      return sp.getContent().getBoundsInLocal().getHeight();
    }
    return 0;
  }

  public static double getViewportHeight(TextArea textArea) {
    ScrollPane sp = (ScrollPane) textArea.lookup(".scroll-pane");
    if (sp!=null) {
      return sp.getViewportBounds().getHeight();
    }
    return 0;
  }

  public static double getVvalue(TextArea textArea) {
    ScrollPane sp = (ScrollPane) textArea.lookup(".scroll-pane");
    if (sp!=null) {
      return sp.getVvalue();
    }
    return 0;
  }

  public static double getVmax(TextArea textArea) {
    ScrollPane sp = (ScrollPane) textArea.lookup(".scroll-pane");
    if (sp!=null) {
      return sp.getVmax();
    }
    return 0;
  }

}
