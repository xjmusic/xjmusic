package io.xj.gui;

import jakarta.annotation.Nullable;
import javafx.scene.Scene;
import org.springframework.stereotype.Service;

@Service
public class MainWindowScene {
  @Nullable Scene mainWindowScene;

  public @Nullable Scene get() {
    return mainWindowScene;
  }

  public void set(Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

}
