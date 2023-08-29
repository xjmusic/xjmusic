package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ThemeService;
import jakarta.annotation.Nullable;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainController implements ReadyAfterBootController {
  final FabricationService fabricationService;
  final MainPaneTopController mainPaneTopController;
  final MainPaneBottomController mainPaneBottomController;
  final ThemeService themeService;

  @Nullable
  Scene mainWindowScene;

  public MainController(
    MainPaneTopController mainPaneTopController,
    MainPaneBottomController mainPaneBottomController,
    FabricationService fabricationService,
    ThemeService themeService
  ) {
    this.mainPaneTopController = mainPaneTopController;
    this.fabricationService = fabricationService;
    this.mainPaneBottomController = mainPaneBottomController;
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

    mainPaneTopController.onStageReady();
    mainPaneBottomController.onStageReady();
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(@Nullable Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

}
