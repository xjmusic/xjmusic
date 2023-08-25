package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.FabricationStatus;
import io.xj.gui.services.ThemeService;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import jakarta.annotation.Nullable;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MainWindowController implements ReadyAfterBootController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  static final List<FabricationStatus> BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES = Arrays.asList(
    FabricationStatus.Standby,
    FabricationStatus.Active,
    FabricationStatus.Cancelled,
    FabricationStatus.Done,
    FabricationStatus.Failed
  );
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

  @Nullable
  Scene mainWindowScene;

  @FXML
  TextField fieldInputTemplateKey;

  @FXML
  ChoiceBox<InputMode> choiceInputMode;

  @FXML
  ChoiceBox<OutputMode> choiceOutputMode;

  @FXML
  ChoiceBox<OutputFileMode> choiceOutputFileMode;

  @FXML
  TextField fieldOutputSeconds;

  @FXML
  TextField fieldOutputPathPrefix;


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
    choiceInputMode.getItems().setAll(InputMode.values());
    choiceOutputMode.getItems().setAll(OutputMode.values());
    choiceOutputFileMode.getItems().setAll(OutputFileMode.values());

    fieldInputTemplateKey.textProperty().bindBidirectional(fabricationService.inputTemplateKeyProperty());
      choiceInputMode.valueProperty().bindBidirectional(fabricationService.inputModeProperty());
    choiceOutputMode.valueProperty().bindBidirectional(fabricationService.outputModeProperty());
      choiceOutputFileMode.valueProperty().bindBidirectional(fabricationService.outputFileModeProperty());
    fieldOutputSeconds.textProperty().bindBidirectional(fabricationService.outputSecondsProperty());
      fieldOutputPathPrefix.textProperty().bindBidirectional(fabricationService.outputPathPrefixProperty());

    themeService.setup(mainWindowScene);
    themeService.isDarkThemeProperty().bind(checkboxDarkTheme.selectedProperty());
    themeService.isDarkThemeProperty().addListener((observable, oldValue, newValue) -> themeService.setup(mainWindowScene));

    buttonAction.disableProperty().bind(Bindings.createBooleanBinding(() ->
        BUTTON_ACTION_ACTIVE_IN_FABRICATION_STATES.contains(fabricationService.statusProperty().get()),
      fabricationService.statusProperty()).not());

    buttonAction.textProperty().bind(Bindings.createStringBinding(() ->
        switch (fabricationService.statusProperty().get()) {
          case Starting, Standby -> BUTTON_TEXT_START;
          case Active -> BUTTON_TEXT_STOP;
          case Cancelled, Failed, Done -> BUTTON_TEXT_RESET;
        },
      fabricationService.statusProperty()));

    bottomPaneController.onStageReady();
  }

  @FXML
  protected void onQuit() {
    var exitCode = SpringApplication.exit(ac, () -> 0);
    LOG.info("Will exit with code {}", exitCode);
    System.exit(exitCode);
  }

  @FXML
  protected void onButtonActionPress() {
    switch (fabricationService.statusProperty().get()) {
      case Standby -> start();
      case Active -> stop();
      case Cancelled, Done, Failed -> reset();
    }
  }

  public void start() {
    fabricationService.start();
  }

  public void stop() {
    fabricationService.cancel();
  }

  public void reset() {
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

}
