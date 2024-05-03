package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;

public interface ThemeService {
  void setup(@Nullable Scene scene);

  /**
   Fonts directory contains subdirectories with font files.
   */
  void setupFonts();

  /**
   Get the application main scene (must be set on application bootstrap)

   @return the main scene
   */
  Scene getMainScene();

  /**
   Set the main scene

   @param scene main to set
   */
  void setMainScene(Scene scene);

  /**
   Setup a Dialog with the standard properties

   @param dialog to setup
   */
  void setup(Dialog<?> dialog);
}
