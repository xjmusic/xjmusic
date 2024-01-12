// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.listeners;

import com.tangorabox.componentinspector.fx.FXComponentInspectorHandler;
import io.xj.gui.WorkstationWindow;
import io.xj.gui.controllers.MainController;
import io.xj.gui.events.StageReadyEvent;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
  private final MainController mainController;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    @Value("${gui.debug}") String debug,
    MainController mainController,
    ApplicationContext ac,
    ProjectService projectService,
    ThemeService themeService
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.debug = debug;
    this.mainController = mainController;
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);

      var scene = new Scene(mainWindowFxmlLoader.load());
      primaryStage.setScene(scene);
      primaryStage.initStyle(StageStyle.DECORATED);

      WorkstationWindow.setupIcon(primaryStage, null);
      WorkstationWindow.setupTaskbar();
      primaryStage.titleProperty().bind(projectService.windowTitleProperty());

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
}
