package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ProjectUtils {
  static final Logger LOG = LoggerFactory.getLogger(ProjectUtils.class);

  /**
   Choose a directory

   @param stage            the stage
   @param title            the title of the chooser window
   @param initialDirectory the initial directory
   @return the absolute path of the chosen directory, or null if none chosen
   */
  public static @Nullable String chooseDirectory(Window stage, String title, String initialDirectory) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle(title);
    directoryChooser.setInitialDirectory(new File(initialDirectory));
    File selectedDirectory = directoryChooser.showDialog(stage);
    return Objects.nonNull(selectedDirectory) ? selectedDirectory.getAbsolutePath():null;
  }

  /**
   Choose a file

   @param stage            the stage
   @param title            the title of the chooser window
   @param initialDirectory the initial directory
   @return the absolute path of the chosen file, or null if none chosen
   */
  public static @Nullable String chooseXJProjectFile(Window stage, String title, String initialDirectory) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(initialDirectory));
    fileChooser.setTitle(title);
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XJ projects (*.xj)", "*.xj");
    fileChooser.getExtensionFilters().add(extFilter);
    File file = fileChooser.showOpenDialog(stage);
    return Objects.nonNull(file) ? file.getAbsolutePath():null;
  }

  /**
   Choose an audio file

   @param stage the stage
   @param title the title of the chooser window
   @return the absolute path of the chosen file, or null if none chosen
   */
  public static @Nullable String chooseAudioFile(Window stage, String title) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio files (*.wav, *.aiff, *.mp3, *.aac, *.flac)", "*.wav", "*.aiff", "*.mp3", "*.aac", "*.flac");
    fileChooser.getExtensionFilters().add(extFilter);
    File file = fileChooser.showOpenDialog(stage);
    return Objects.nonNull(file) ? file.getAbsolutePath():null;
  }

  /**
   Open a path in the desktop file browser

   @param path the path to open
   */
  public static void openDesktopPath(String path) {
    if (Desktop.isDesktopSupported()) {
      try {
        File file = new File(path);

        // Open the file in the default file browser
        Desktop.getDesktop().open(file);
      } catch (IOException | IllegalArgumentException e) {
        LOG.error("Could not open folder \"{}\"", path, e);
      }
    } else {
      LOG.error("Desktop file browsing not support on this OS! The folder is at {}", path);
    }
  }
}
