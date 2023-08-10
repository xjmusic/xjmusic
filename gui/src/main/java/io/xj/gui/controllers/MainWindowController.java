package io.xj.gui.controllers;

import io.xj.gui.WorkstationStatus;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.work.WorkFactory;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Locale;

@Service
public class MainWindowController {
  private WorkstationStatus status;
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private final HostServices hostServices;
  private final ConfigurableApplicationContext ac;
  private final WorkFactory workFactory;
  private final String launchGuideUrl;
  private final String lightTheme;
  private final String darkTheme;
  private final String defaultOutputPathPrefix;
  private final InputMode defaultInputMode;
  private final OutputMode defaultOutputMode;
  private final OutputFileMode defaultOutputFileMode;
  private final String defaultOutputSeconds;

  @Nullable
  private Scene mainWindowScene;

  @Nullable
  private Thread workThread;

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("${gui.theme.dark}") String darkTheme,
    @Value("${gui.theme.light}") String lightTheme,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") String defaultOutputSeconds,
    ConfigurableApplicationContext ac,
    WorkFactory workFactory
  ) {
    this.workFactory = workFactory;
    status = WorkstationStatus.Ready;
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
    this.lightTheme = lightTheme;
    this.darkTheme = darkTheme;
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
  protected TextArea textAreaLogs;
  @FXML
  protected Button buttonAction;

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
      case Ready -> onWorkStart();
      case Working -> onWorkStop();
      case Stopped, Done, Failed -> onWorkReset();
    }
  }

  private void onWorkStart() {
    Platform.runLater(() -> {
      onStatusUpdate(WorkstationStatus.Working);
      workThread = new Thread(() -> workFactory.start(
        choiceInputMode.getValue(),
        fieldInputTemplateKey.getText(),
        choiceOutputFileMode.getValue(),
        choiceOutputMode.getValue(),
        fieldOutputPathPrefix.getText(),
        Integer.parseInt(fieldOutputSeconds.getText()),
        this::onWorkDone
      ));
      workThread.start();
    });
  }

  private void onWorkStop() {
    Platform.runLater(() -> {
      onStatusUpdate(WorkstationStatus.Stopping);
      workThread.interrupt();
      try {
        workThread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      onStatusUpdate(WorkstationStatus.Stopped);
    });
  }

  private void onWorkDone() {
    onStatusUpdate(WorkstationStatus.Done);
  }

  private void onWorkReset() {
    onStatusUpdate(WorkstationStatus.Ready);
  }

  @FXML
  protected Label labelStatus;

  @FXML
  protected void onLaunchUserGuide() {
    LOG.info("Will launch user guide");
    hostServices.showDocument(launchGuideUrl);
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

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
  }

  public void onStatusUpdate(WorkstationStatus status) {
    this.status = status;
    labelStatus.setText(status.toString());
    switch (status) {
      case Ready -> {
        buttonAction.setText("Start");
        buttonAction.setDisable(false);
      }
      case Working -> {
        buttonAction.setText("Stop");
        buttonAction.setDisable(false);
      }
      case Stopping -> {
        buttonAction.setText("Stopping");
        buttonAction.setDisable(true);
      }
      case Stopped, Done, Failed -> {
        buttonAction.setText("Reset");
        buttonAction.setDisable(false);
      }
    }
  }

  private void enableDarkTheme() {
    mainWindowScene.getStylesheets().remove(lightTheme);
    mainWindowScene.getStylesheets().add(darkTheme);
  }

  private void disableDarkTheme() {
    mainWindowScene.getStylesheets().remove(darkTheme);
    mainWindowScene.getStylesheets().add(lightTheme);
  }
}
