package io.xj.gui.utils;

import jakarta.annotation.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

public class DirectoryChooserUtils {
  public static @Nullable String chooseDirectory(Window stage, String title, String initialDirectory) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle(title);
    directoryChooser.setInitialDirectory(new File(initialDirectory));
    File selectedDirectory = directoryChooser.showDialog(stage);
    return Objects.nonNull(selectedDirectory) ? selectedDirectory.getAbsolutePath() : null;
  }
}
