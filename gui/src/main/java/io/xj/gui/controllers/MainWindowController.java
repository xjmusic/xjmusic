package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ThemeService;
import jakarta.annotation.Nullable;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainWindowController implements ReadyAfterBootController {
  final FabricationService fabricationService;
  final TopPaneController topPaneController;
  final BottomPaneController bottomPaneController;
  final ThemeService themeService;

  @Nullable
  Scene mainWindowScene;

  public MainWindowController(
    TopPaneController topPaneController,
    BottomPaneController bottomPaneController,
    FabricationService fabricationService,
    ThemeService themeService
  ) {
    this.topPaneController = topPaneController;
    this.fabricationService = fabricationService;
    this.bottomPaneController = bottomPaneController;
    this.themeService = themeService;
  }

  @FXML
  public VBox bottomPane;
  @FXML
  public VBox topPane;

  @Override
  public void onStageReady() {
    themeService.setup(mainWindowScene);
    themeService.isDarkThemeProperty().addListener((observable, oldValue, newValue) -> themeService.setup(mainWindowScene));

    topPaneController.onStageReady();
    bottomPaneController.onStageReady();
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(@Nullable Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

}
