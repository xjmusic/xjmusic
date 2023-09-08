// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.listeners;

import com.tangorabox.componentinspector.fx.FXComponentInspectorHandler;
import io.xj.gui.WorkstationIcon;
import io.xj.gui.controllers.MainController;
import io.xj.gui.events.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  private static final int DEFAULT_FONT_SIZE = 12;
  final Resource mainWindowFxml;
  final String debug;
  final MainController mainController;
  final ApplicationContext ac;
  private final List<Resource> fonts;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    @Value("classpath:/fonts/RobotoMono-Bold.ttf") Resource fontRobotoMonoBold,
    @Value("classpath:/fonts/RobotoMono-BoldItalic.ttf") Resource fontRobotoMonoBoldItalic,
    @Value("classpath:/fonts/RobotoMono-ExtraLight.ttf") Resource fontRobotoMonoExtraLight,
    @Value("classpath:/fonts/RobotoMono-ExtraLightItalic.ttf") Resource fontRobotoMonoExtraLightItalic,
    @Value("classpath:/fonts/RobotoMono-Italic.ttf") Resource fontRobotoMonoItalic,
    @Value("classpath:/fonts/RobotoMono-Light.ttf") Resource fontRobotoMonoLight,
    @Value("classpath:/fonts/RobotoMono-LightItalic.ttf") Resource fontRobotoMonoLightItalic,
    @Value("classpath:/fonts/RobotoMono-Medium.ttf") Resource fontRobotoMonoMedium,
    @Value("classpath:/fonts/RobotoMono-MediumItalic.ttf") Resource fontRobotoMonoMediumItalic,
    @Value("classpath:/fonts/RobotoMono-Regular.ttf") Resource fontRobotoMonoRegular,
    @Value("classpath:/fonts/RobotoMono-SemiBold.ttf") Resource fontRobotoMonoSemiBold,
    @Value("classpath:/fonts/RobotoMono-SemiBoldItalic.ttf") Resource fontRobotoMonoSemiBoldItalic,
    @Value("classpath:/fonts/RobotoMono-Thin.ttf") Resource fontRobotoMonoThin,
    @Value("classpath:/fonts/RobotoMono-ThinItalic.ttf") Resource fontRobotoMonoThinItalic,
    @Value("${gui.debug}") String debug,
    MainController mainController,
    ApplicationContext ac
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.debug = debug;
    this.mainController = mainController;
    this.ac = ac;
    this.fonts = List.of(
      fontRobotoMonoBold,
      fontRobotoMonoBoldItalic,
      fontRobotoMonoExtraLight,
      fontRobotoMonoExtraLightItalic,
      fontRobotoMonoItalic,
      fontRobotoMonoLight,
      fontRobotoMonoLightItalic,
      fontRobotoMonoMedium,
      fontRobotoMonoMediumItalic,
      fontRobotoMonoRegular,
      fontRobotoMonoSemiBold,
      fontRobotoMonoSemiBoldItalic,
      fontRobotoMonoThin,
      fontRobotoMonoThinItalic
    );
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);
      mainController.setMainWindowScene(new Scene(mainWindowFxmlLoader.load()));
      primaryStage.setScene(mainController.getMainWindowScene());
      primaryStage.initStyle(StageStyle.DECORATED);
      WorkstationIcon.setup(primaryStage, null);
      WorkstationIcon.setupTaskbar();

      fonts.forEach(font -> {
        try {
          Font.loadFont(font.getURL().toExternalForm(), DEFAULT_FONT_SIZE);
        } catch (IOException e) {
          LOG.error("Failed to load font: {}", font.getFilename(), e);
        }
      });

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
