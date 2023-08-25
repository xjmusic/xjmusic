package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.ThemeService;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MainWindowController implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  final static String BUTTON_TEXT_START = "Start";
  final static String BUTTON_TEXT_STOP = "Stop";
  final static String BUTTON_TEXT_RESET = "Reset";
  final HostServices hostServices;
  final ConfigurableApplicationContext ac;
  final FabricationService fabricationService;
  final BottomPaneController bottomPaneController;
  final ModalLabConnectionController modalLabConnectionController;
  final ThemeService themeService;
  final String launchGuideUrl;
  FabricationStatus status;

  @Nullable
  Scene mainWindowScene;

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    BottomPaneController bottomPaneController,
    ModalLabConnectionController modalLabConnectionController,
    FabricationService fabricationService,
    ThemeService themeService,
    ConfigurableApplicationContext ac
  ) {
    this.fabricationService = fabricationService;
    this.bottomPaneController = bottomPaneController;
    this.modalLabConnectionController = modalLabConnectionController;
    this.themeService = themeService;
    status = FabricationStatus.Standby;
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
  }

  @FXML
  public VBox bottomPane;
  @FXML
  protected CheckMenuItem checkboxDarkTheme;
  @FXML
  protected Button buttonAction;

  @Override
  public void onStageReady() {
    onStatusUpdate(status);
    bottomPaneController.onStageReady();
    themeService.setup(mainWindowScene);
    themeService.isDarkThemeProperty().bind(checkboxDarkTheme.selectedProperty());
    themeService.isDarkThemeProperty().addListener((observable, oldValue, newValue) -> themeService.setup(mainWindowScene));
  }

  @FXML
  protected void onQuit() {
    var exitCode = SpringApplication.exit(ac, () -> 0);
    LOG.info("Will exit with code {}", exitCode);
    System.exit(exitCode);
  }

  @FXML
  protected void onButtonActionPress() {
    switch (status) {
      case Standby -> start();
      case Active -> stop();
      case Cancelled, Done, Failed -> reset();
    }
  }

  public void start() {
    onStatusUpdate(FabricationStatus.Starting);
    fabricationService.setOnReady((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Standby));
    fabricationService.setOnRunning((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Active));
    fabricationService.setOnSucceeded((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Done));
    fabricationService.setOnCancelled((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Cancelled));
    fabricationService.setOnFailed((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Failed));
    fabricationService.start();
  }

  public void stop() {
    onStatusUpdate(FabricationStatus.Cancelling);
    fabricationService.cancel();
  }

  public void reset() {
    onStatusUpdate(FabricationStatus.Resetting);
    fabricationService.reset();
  }

  @FXML
  protected void onLaunchUserGuide() {
    LOG.info("Will launch user guide");
    hostServices.showDocument(launchGuideUrl);
  }

  @FXML
  protected void onConnectToLab() {
    modalLabConnectionController.launchModal();
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(@Nullable Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

  public void onStatusUpdate(FabricationStatus status) {
    LOG.info("Status update: {} -> {}", this.status, status);
    this.status = status;
    bottomPaneController.setStatusText(status.toString());
    switch (status) {
      case Initializing, Starting, Cancelling, Resetting -> buttonAction.setDisable(true);
      case Standby -> {
        buttonAction.setText(BUTTON_TEXT_START);
        buttonAction.setDisable(false);
      }
      case Active -> {
        buttonAction.setText(BUTTON_TEXT_STOP);
        buttonAction.setDisable(false);
      }
      case Cancelled, Done, Failed -> {
        buttonAction.setText(BUTTON_TEXT_RESET);
        buttonAction.setDisable(false);
      }
    }
  }
}
