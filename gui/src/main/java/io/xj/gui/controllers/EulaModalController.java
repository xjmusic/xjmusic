// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.TextAreaUtils;
import io.xj.gui.utils.WindowUtils;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

@Service
public class EulaModalController extends ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(EulaModalController.class);
  static final String WINDOW_TITLE = "End User Licensing Agreement (EULA)";
  static final String PREFS_KEY_EULA_ACCEPTED = "eula.accepted";
  private final Resource eulaTextResource;
  private final Preferences prefs;
  private Runnable onAccepted;

  @FXML
  TextArea eulaText;

  @FXML
  Button buttonAccept;

  @FXML
  Button buttonDecline;

  public EulaModalController(
    @Value("classpath:/views/eula-modal.fxml") Resource fxml,
    @Value("classpath:/EULA.txt") Resource eulaTextResource,
    ThemeService themeService,
    ConfigurableApplicationContext ac
  ) {
    super(fxml, ac, themeService);
    this.eulaTextResource = eulaTextResource;

    prefs = Preferences.userNodeForPackage(EulaModalController.class);
  }

  @Override
  public void onStageReady() {
    try {
      eulaText.setText(eulaTextResource.getContentAsString(StandardCharsets.UTF_8));

      buttonAccept.setDisable(true);
      eulaText.scrollTopProperty().addListener((observable, oldValue, newValue) -> {
        double vvalue = TextAreaUtils.getVvalue(eulaText);
        double vMax = TextAreaUtils.getVmax(eulaText);
        buttonAccept.setDisable(vvalue < vMax);
      });

    } catch (Exception e) {
      LOG.error("Error loading EULA!\n{}", StringUtils.formatStackTrace(e), e);
      System.exit(1);
    }
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void handlePressedAccept() {
    prefs.putBoolean(PREFS_KEY_EULA_ACCEPTED, true);
    closeStage();
    onAccepted.run();
  }

  @FXML
  void handlePressedDecline() {
    closeStage();
    System.exit(0);
  }

  /**
   Ensure the EULA has been accepted and then run the given task.
   <p>
   If the user refuses the EULA, the application will exit, and the callback will never run.@param stage@param onAccepted task to run if the EULA has been accepted
   */
  public void ensureAcceptance(Stage primaryStage, Runnable onAccepted) {
    if (isEulaAccepted()) {
      onAccepted.run();
    } else {
      try {
        this.onAccepted = onAccepted;

        primaryStage.setTitle(WindowUtils.computeTitle(WINDOW_TITLE));

        FXMLLoader mainWindowFxmlLoader = new FXMLLoader(fxml.getURL());
        mainWindowFxmlLoader.setControllerFactory(ac::getBean);

        var scene = new Scene(mainWindowFxmlLoader.load());
        primaryStage.setScene(scene);

        themeService.setup(scene);
        themeService.isDarkThemeProperty().addListener((o, ov, value) -> themeService.setup(scene));
        themeService.setupFonts();

        onStageReady();
        primaryStage.show();

      } catch (Exception e) {
        LOG.error("Failed to set the scene on the primary stage!", e);
        System.exit(1);
      }
    }
  }

  /**
   Check if the EULA has been accepted.

   @return true if the EULA has been accepted
   */
  private boolean isEulaAccepted() {
    return prefs.getBoolean(PREFS_KEY_EULA_ACCEPTED, false);
  }

  /**
   Close the stage.
   */
  private void closeStage() {
    Stage stage = (Stage) buttonAccept.getScene().getWindow();
    stage.close();
    onStageClose();
  }
}
