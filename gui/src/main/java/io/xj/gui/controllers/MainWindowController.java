package io.xj.gui.controllers;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class MainWindowController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private final HostServices hostServices;
  private final ConfigurableApplicationContext ac;
  private final String launchGuideUrl;
  private final String lightTheme;
  private final String darkTheme;
  private final String defaultOutputPathPrefix;
  private final String defaultAudioBaseUrl;
  private final Collection<String> inputModeChoices;
  private final Collection<String> outputModeChoices;
  private final Collection<String> outputFileModeChoices;
  private final String defaultInputMode;
  private final String defaultOutputMode;
  private final String defaultOutputFileMode;
  private final String defaultOutputSeconds;
  @Nullable
  private Scene mainWindowScene;

  public MainWindowController(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${audio.base.url}") String defaultAudioBaseUrl,
    @Value("${gui.launch.guide.url}") String launchGuideUrl,
    @Value("${gui.theme.dark}") String darkTheme,
    @Value("${gui.theme.light}") String lightTheme,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") String defaultOutputSeconds,
    ConfigurableApplicationContext ac
  ) {
    this.hostServices = hostServices;
    this.ac = ac;
    this.launchGuideUrl = launchGuideUrl;
    this.lightTheme = lightTheme;
    this.darkTheme = darkTheme;
    this.defaultAudioBaseUrl = defaultAudioBaseUrl;
    this.defaultOutputSeconds = defaultOutputSeconds;
    this.defaultOutputPathPrefix = System.getProperty("user.home") + File.separator;
    this.inputModeChoices = Arrays.stream(InputMode.values()).map(Enum::name).collect(Collectors.toList());
    this.outputModeChoices = Arrays.stream(OutputMode.values()).map(Enum::name).collect(Collectors.toList());
    this.outputFileModeChoices = Arrays.stream(OutputFileMode.values()).map(Enum::name).collect(Collectors.toList());
    this.defaultInputMode = defaultInputMode.toUpperCase(Locale.ROOT);
    this.defaultOutputMode = defaultOutputMode.toUpperCase(Locale.ROOT);
    this.defaultOutputFileMode = defaultOutputFileMode.toUpperCase(Locale.ROOT);
  }

  @FXML
  protected CheckMenuItem darkThemeCheck;
  @FXML
  protected TextField fieldAudioBaseUrl;
  @FXML
  protected TextField fieldInputTemplateKey;
  @FXML
  protected TextField fieldOutputPathPrefix;
  @FXML
  protected TextField fieldOutputSeconds;
  @FXML
  protected ChoiceBox<String> choiceInputMode;
  @FXML
  protected ChoiceBox<String> choiceOutputMode;
  @FXML
  protected ChoiceBox<String> choiceOutputFileMode;
  @FXML
  protected TextArea textAreaLogs;

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
  protected void onLaunchUserGuide() {
    LOG.info("Will launch user guide");
    hostServices.showDocument(launchGuideUrl);
  }

  private void enableDarkTheme() {
    mainWindowScene.getStylesheets().remove(lightTheme);
    mainWindowScene.getStylesheets().add(darkTheme);
  }

  private void disableDarkTheme() {
    mainWindowScene.getStylesheets().remove(darkTheme);
    mainWindowScene.getStylesheets().add(lightTheme);
  }

  public @Nullable Scene getMainWindowScene() {
    return mainWindowScene;
  }

  public void setMainWindowScene(Scene mainWindowScene) {
    this.mainWindowScene = mainWindowScene;
  }

  public void onStageReady() {
    enableDarkTheme();
    fieldOutputSeconds.setText(defaultOutputSeconds);
    fieldAudioBaseUrl.setText(defaultAudioBaseUrl);
    fieldOutputPathPrefix.setText(defaultOutputPathPrefix);
    choiceInputMode.getItems().setAll(inputModeChoices);
    choiceOutputMode.getItems().setAll(outputModeChoices);
    choiceOutputFileMode.getItems().setAll(outputFileModeChoices);
    choiceInputMode.setValue(defaultInputMode);
    choiceOutputMode.setValue(defaultOutputMode);
    choiceOutputFileMode.setValue(defaultOutputFileMode);
  }
}
