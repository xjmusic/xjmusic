package io.xj.gui.listeners;

import io.xj.gui.WorkstationIcon;
import io.xj.gui.controllers.MainController;
import io.xj.gui.events.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
  final Resource mainWindowFxml;
  final MainController mainController;
  final ApplicationContext ac;

  public MainWindowStageReadyListener(
    @Value("classpath:/views/main.fxml") Resource mainWindowFxml,
    MainController mainController,
    ApplicationContext ac
  ) {
    this.mainWindowFxml = mainWindowFxml;
    this.mainController = mainController;
    this.ac = ac;
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

      mainController.onStageReady();
      primaryStage.show();

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }
}
