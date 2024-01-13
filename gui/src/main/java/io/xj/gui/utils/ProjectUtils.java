package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

public class ProjectUtils {
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
    return Objects.nonNull(selectedDirectory) ? selectedDirectory.getAbsolutePath() : null;
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
    return Objects.nonNull(file) ? file.getAbsolutePath() : null;
  }
}
