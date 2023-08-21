package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.work.WorkConfiguration;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

@Service
public class MainWindowController implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private final static String LIGHT_THEME_STYLE_PATH = "styles/light-theme.css";
  private final static String DARK_THEME_STYLE_PATH = "styles/dark-theme.css";
  private final static String BUTTON_TEXT_START = "Start";
  private final static String BUTTON_TEXT_STOP = "Stop";
  private final static String BUTTON_TEXT_RESET = "Reset";
  private final HostServices hostServices;
  private final ConfigurableApplicationContext ac;
  private final FabricationService fabricationService;
  private final BottomPaneController bottomPaneController;
  private final String launchGuideUrl;
  private final String lightTheme;
  private final String darkTheme;
  private final String defaultOutputPathPrefix;
  private final InputMode defaultInputMode;
  private final OutputMode defaultOutputMode;
  private final OutputFileMode defaultOutputFileMode;
  private final String defaultOutputSeconds;
  private FabricationStatus status;

  @Nullable
  private Scene mainWindowScene;

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") String defaultOutputSeconds,
    ConfigurableApplicationContext ac,
    FabricationService fabricationService,
    BottomPaneController bottomPaneController
  ) {
    this.fabricationService = fabricationService;
    this.bottomPaneController = bottomPaneController;
    status = FabricationStatus.Ready;
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
    this.lightTheme = LIGHT_THEME_STYLE_PATH;
    this.darkTheme = DARK_THEME_STYLE_PATH;
    this.defaultOutputSeconds = defaultOutputSeconds;
    this.defaultOutputPathPrefix = System.getProperty("user.home") + File.separator;
    this.defaultInputMode = InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT));
    this.defaultOutputMode = OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT));
    this.defaultOutputFileMode = OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT));
  }

  @FXML
  protected CheckMenuItem darkThemeCheck;
  @FXML
  protected TextField fieldInputTemplateKey;
  @FXML
  protected TextField fieldOutputPathPrefix;
  @FXML
  protected TextField fieldOutputSeconds;
  @FXML
  protected ChoiceBox<InputMode> choiceInputMode;
  @FXML
  protected ChoiceBox<OutputMode> choiceOutputMode;
  @FXML
  protected ChoiceBox<OutputFileMode> choiceOutputFileMode;
  @FXML
  protected Button buttonAction;
  @FXML
  protected Label labelStatus;

  @Override
  public void onStageReady() {
    enableDarkTheme();
    onStatusUpdate(status);
    fieldOutputSeconds.setText(defaultOutputSeconds);
    fieldOutputPathPrefix.setText(defaultOutputPathPrefix);
    choiceInputMode.getItems().setAll(InputMode.values());
    choiceOutputMode.getItems().setAll(OutputMode.values());
    choiceOutputFileMode.getItems().setAll(OutputFileMode.values());
    choiceInputMode.setValue(defaultInputMode);
    choiceOutputMode.setValue(defaultOutputMode);
    choiceOutputFileMode.setValue(defaultOutputFileMode);
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
    fabricationService.setConfiguration(new WorkConfiguration()
      .setInputMode(choiceInputMode.getValue())
      .setInputTemplateKey(fieldInputTemplateKey.getText())
      .setOutputFileMode(choiceOutputFileMode.getValue())
      .setOutputMode(choiceOutputMode.getValue())
      .setOutputPathPrefix(fieldOutputPathPrefix.getText())
      .setOutputSeconds(Integer.parseInt(fieldOutputSeconds.getText())));
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
    LOG.info("Will connect to lab");
    // TODO open connection to lab modal
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
    labelStatus.setText(status.toString());
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
      mainWindowScene.getStylesheets().remove(lightTheme);
      mainWindowScene.getStylesheets().add(darkTheme);
    }
  }

  private void disableDarkTheme() {
    if (Objects.nonNull(mainWindowScene)) {
      mainWindowScene.getStylesheets().remove(darkTheme);
      mainWindowScene.getStylesheets().add(lightTheme);
    }
  }
}
