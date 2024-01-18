// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import com.tangorabox.componentinspector.fx.FXComponentInspectorHandler;
import io.xj.gui.controllers.MainController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.TextAreaUtils;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  static final String PREFS_KEY_EULA_ACCEPTED = "eula.accepted";
  private final Resource mainWindowFxml;
  private final Resource eulaTextResource;
  private final String debug;
  private final MainController mainController;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final Preferences prefs;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    @Value("classpath:/EULA.txt") Resource eulaTextResource,
    @Value("${gui.debug}") String debug,
    MainController mainController,
    ApplicationContext ac,
    ProjectService projectService,
    ThemeService themeService
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.eulaTextResource = eulaTextResource;
    this.debug = debug;
    this.mainController = mainController;
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;

    prefs = Preferences.userNodeForPackage(MainWindowStageReadyListener.class);
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      WorkstationWindow.setupTaskbar();
      WorkstationWindow.setupIcon(primaryStage);
      primaryStage.titleProperty().bind(projectService.windowTitleProperty());

      if (!isEulaAccepted()) {
        // TODO use a whole modal for the EULA
        showEulaDialog();
      }

      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);

      var scene = new Scene(mainWindowFxmlLoader.load());
      primaryStage.setScene(scene);
      primaryStage.initStyle(StageStyle.DECORATED);

      themeService.setup(scene);
      themeService.isDarkThemeProperty().addListener((o, ov, value) -> themeService.setup(scene));
      themeService.setupFonts();

      mainController.onStageReady();
      primaryStage.show();

      // See https://github.com/TangoraBox/ComponentInspector/
      if (debug.equals("true")) {
        FXComponentInspectorHandler.handleAll();
      }

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }


  /**
   Show the EULA dialog and exit if the user does not accept.
   */
  private void showEulaDialog() {
    Alert eulaDialog = new Alert(Alert.AlertType.CONFIRMATION);
    eulaDialog.setTitle("End User License Agreement");
    eulaDialog.setHeaderText("Please read and accept the EULA to use the XJ music workstation.");

    try {
      TextArea eulaText = new TextArea(eulaTextResource.getContentAsString(StandardCharsets.UTF_8));
      eulaText.setEditable(false);
      eulaText.setWrapText(true);

      // Initially disable the OK button
      ButtonType okButton = new ButtonType("Accept Agreement", ButtonBar.ButtonData.OK_DONE);
      eulaDialog.getButtonTypes().setAll(okButton, ButtonType.CANCEL);
      eulaDialog.getDialogPane().lookupButton(okButton).setDisable(true);

      eulaText.scrollTopProperty().addListener((obs, oldVal, newVal) -> {
        double vvalue = TextAreaUtils.getVvalue(eulaText);
        double vMax = TextAreaUtils.getVmax(eulaText);
        eulaDialog.getDialogPane().lookupButton(okButton).setDisable(vvalue < vMax);
      });

      eulaDialog.getDialogPane().setContent(new VBox(eulaText));
      var response = eulaDialog.showAndWait();
      if (response.isEmpty() || response.get() != ButtonType.OK) {
        System.exit(0);
      }
      prefs.putBoolean(PREFS_KEY_EULA_ACCEPTED, true);

    } catch (Exception e) {
      LOG.error("Error loading EULA!\n{}", StringUtils.formatStackTrace(e), e);
      System.exit(1);
    }
  }

  /**
   Check if the EULA has been accepted.

   @return true if the EULA has been accepted
   */
  private boolean isEulaAccepted() {
    return prefs.getBoolean(PREFS_KEY_EULA_ACCEPTED, false);
  }
}
