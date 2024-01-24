// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import com.tangorabox.componentinspector.fx.FXComponentInspectorHandler;
import io.xj.gui.controllers.EulaModalController;
import io.xj.gui.controllers.MainController;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.WindowUtils;
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
  private final EulaModalController eulaModalController;
  private final MainController mainController;
  private final ApplicationContext ac;
  private final UIStateService uiStateService;
  private final ThemeService themeService;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    @Value("${gui.debug}") String debug,
    EulaModalController eulaModalController,
    MainController mainController,
    ApplicationContext ac,
    UIStateService uiStateService,
    ThemeService themeService
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.debug = debug;
    this.eulaModalController = eulaModalController;
    this.mainController = mainController;
    this.ac = ac;
    this.uiStateService = uiStateService;
    this.themeService = themeService;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    WindowUtils.setupTaskbar();
    var primaryStage = event.getStage();
    WindowUtils.setupIcon(primaryStage);
    primaryStage.initStyle(StageStyle.DECORATED);

    eulaModalController.ensureAcceptance(primaryStage, () -> {
      try {
        var scene = WindowUtils.loadSceneFxml(ac, mainWindowFxml);

        primaryStage.titleProperty().bind(uiStateService.windowTitleProperty());
        primaryStage.setScene(scene);

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
    });
  }
}
