// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import com.tangorabox.componentinspector.fx.FXComponentInspectorHandler;
import io.xj.gui.controllers.EulaModalController;
import io.xj.gui.controllers.MainController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.model.util.StringUtils;
import jakarta.annotation.Nonnull;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  private final Resource mainWindowFxml;
  private final String debug;
  private final EulaModalController eulaModalController;
  private final MainController mainController;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final ThemeService themeService;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    @Value("${debug}") String debug,
    EulaModalController eulaModalController,
    MainController mainController,
    ApplicationContext ac,
    ProjectService projectService,
    UIStateService uiStateService,
    ThemeService themeService
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.debug = debug;
    this.eulaModalController = eulaModalController;
    this.mainController = mainController;
    this.ac = ac;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
    this.themeService = themeService;
  }

  @Override
  public void onApplicationEvent(@Nonnull StageReadyEvent event) {
    Stage primaryStage;
    try {
      UiUtils.setupTaskbar();
      primaryStage = event.getStage();
      UiUtils.setupIcon(primaryStage);
      primaryStage.initStyle(StageStyle.DECORATED);
    } catch (Exception e) {
      LOG.error("Failed to initialize Stage! {}\n{}", e.getMessage(), StringUtils.formatStackTrace(e));
      return;
    }

    try {
      eulaModalController.ensureAcceptance(primaryStage, () -> onEulaAccepted(primaryStage));
    } catch (Exception e) {
      LOG.error("Failed to launch EULA modal! {}\n{}", e.getMessage(), StringUtils.formatStackTrace(e));
    }
  }

  /**
   Called when the EULA has been accepted.

   @param primaryStage the primary stage
   */
  private void onEulaAccepted(Stage primaryStage) {
    try {
      var scene = UiUtils.loadSceneFxml(ac, mainWindowFxml);
      primaryStage.titleProperty().bind(uiStateService.windowTitleProperty());
      primaryStage.setScene(scene);

      themeService.setMainScene(scene);
      themeService.setup(scene);
      themeService.setupFonts();

      primaryStage.setOnCloseRequest(this::onCloseRequest);

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
   Called when the user requests to close the application.

   @param event the window event
   */
  private void onCloseRequest(WindowEvent event) {
    LOG.info("Closing the application...");
    event.consume();
    projectService.promptToSaveChanges(() -> WorkstationGuiFxApplication.exit(ac));
  }
}
