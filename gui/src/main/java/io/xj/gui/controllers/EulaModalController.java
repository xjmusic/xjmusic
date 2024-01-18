// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.TextAreaUtils;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

@Service
public class EulaModalController extends ReadyAfterBootModalController {
  static final Logger LOG = LoggerFactory.getLogger(EulaModalController.class);
  static final String WINDOW_TITLE = "End User Licensing Agreement (EULA)";
  static final String PREFS_KEY_EULA_ACCEPTED = "eula.accepted";
  private final Resource eulaTextResource;
  private final ThemeService themeService;
  private final Resource eulaModalFxml;
  private final ConfigurableApplicationContext ac;
  private final Preferences prefs;
  private Runnable onAccepted;

  @FXML
  TextArea eulaText;

  @FXML
  Button buttonAccept;

  @FXML
  Button buttonDecline;

  public EulaModalController(
    @Value("classpath:/views/eula-modal.fxml") Resource eulaModalFxml, // TODO create this file
    @Value("classpath:/EULA.txt") Resource eulaTextResource,
    ThemeService themeService,
    ConfigurableApplicationContext ac
  ) {
    this.themeService = themeService;
    this.eulaModalFxml = eulaModalFxml;
    this.eulaTextResource = eulaTextResource;
    this.ac = ac;

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

  @Override
  public void launchModal() {
    createAndShowModal(ac, themeService, eulaModalFxml, WINDOW_TITLE);
  }

  @FXML
  void handlePressedAccept() {
    prefs.putBoolean(PREFS_KEY_EULA_ACCEPTED, true);
    onAccepted.run();
  }

  @FXML
  void handlePressedDecline() {
    System.exit(0);
  }

  /**
   Ensure the EULA has been accepted and then run the given task.
   <p>
   If the user refuses the EULA, the application will exit, and the callback will never run.

   @param onAccepted task to run if the EULA has been accepted
   */
  public void ensureAcceptance(Runnable onAccepted) {
    if (isEulaAccepted()) {
      onAccepted.run();
    } else {
      this.onAccepted = onAccepted;
      launchModal();
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
