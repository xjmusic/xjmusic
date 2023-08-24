package io.xj.gui.controllers;

import io.xj.gui.WorkstationIcon;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class MainWindowController implements ReadyAfterBootController {
  private static final String CONNECT_TO_LAB_WINDOW_NAME = "Connect to Lab";
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private final static String LIGHT_THEME_STYLE_PATH = "styles/default-theme.css";
  private final static String DARK_THEME_STYLE_PATH = "styles/dark-theme.css";
  private final static String BUTTON_TEXT_START = "Start";
  private final static String BUTTON_TEXT_STOP = "Stop";
  private final static String BUTTON_TEXT_RESET = "Reset";
  private final HostServices hostServices;
  private final ConfigurableApplicationContext ac;
  private final FabricationService fabricationService;
  private final BottomPaneController bottomPaneController;
  private final ModalLabConnectionController modalLabConnectionController;
  private final String launchGuideUrl;
  private final String defaultTheme;
  private final String darkTheme;
  private final Resource modalLabConnectionFxml;
  private FabricationStatus status;

  @Nullable
  private Scene mainWindowScene;

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("classpath:/views/modal-lab-connection.fxml") Resource modalLabConnectionFxml,
    BottomPaneController bottomPaneController,
    ModalLabConnectionController modalLabConnectionController,
    FabricationService fabricationService,
    ConfigurableApplicationContext ac
    ) {
    this.fabricationService = fabricationService;
    this.bottomPaneController = bottomPaneController;
    this.modalLabConnectionFxml = modalLabConnectionFxml;
    this.modalLabConnectionController = modalLabConnectionController;
    status = FabricationStatus.Ready;
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
    this.defaultTheme = LIGHT_THEME_STYLE_PATH;
    this.darkTheme = DARK_THEME_STYLE_PATH;
  }

  @FXML
  public VBox bottomPane;
  @FXML
  protected CheckMenuItem darkThemeCheck;
  @FXML
  protected Button buttonAction;

  @Override
  public void onStageReady() {
    if (Objects.nonNull(mainWindowScene)) {
      mainWindowScene.getStylesheets().add(defaultTheme);
    }
    enableDarkTheme();
    onStatusUpdate(status);
    bottomPaneController.onStageReady();
  }

  @FXML
  private void toggleDarkTheme() {
    if (darkThemeCheck.isSelected()) {
      enableDarkTheme();
    } else {
      disableDarkTheme();
    }
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
      case Ready -> start();
      case Active -> stop();
      case Cancelled, Done, Failed -> reset();
    }
  }

  public void start() {
    onStatusUpdate(FabricationStatus.Starting);
    fabricationService.setOnReady((WorkerStateEvent ignored) -> onStatusUpdate(FabricationStatus.Ready));
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
    try {
      // Load the FXML file
      FXMLLoader loader = new FXMLLoader(modalLabConnectionFxml.getURL());
      loader.setControllerFactory(ac::getBean);

      // Create a new stage (window)
      Stage stage = new Stage();
      WorkstationIcon.setup(stage, CONNECT_TO_LAB_WINDOW_NAME);

      Scene scene = new Scene(loader.load());
      scene.getStylesheets().add(defaultTheme);
      if (darkThemeCheck.isSelected()) {
        scene.getStylesheets().add(darkTheme);
      }


      // Set the scene and show the stage
      stage.setScene(scene);
      stage.initModality(Modality.APPLICATION_MODAL); // make it a modal window
      modalLabConnectionController.onStageReady();
      stage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
      case Ready -> {
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

  private void enableDarkTheme() {
    if (Objects.nonNull(mainWindowScene)) {
//      mainWindowScene.getStylesheets().remove(lightTheme);
      mainWindowScene.getStylesheets().add(darkTheme);
    }
  }

  private void disableDarkTheme() {
    if (Objects.nonNull(mainWindowScene)) {
      mainWindowScene.getStylesheets().remove(darkTheme);
//      mainWindowScene.getStylesheets().add(lightTheme);
    }
  }
}
