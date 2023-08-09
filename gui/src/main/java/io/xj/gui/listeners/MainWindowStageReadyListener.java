package io.xj.gui.listeners;

import io.xj.gui.controllers.MainWindowController;
import io.xj.gui.events.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class MainWindowStageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(MainWindowStageReadyListener.class);
  private final String applicationTitle;
  private final Resource mainWindowFxml;

  private final MainWindowController mainWindowController;
  private final ApplicationContext ac;
  private final Image appIcon;

  public MainWindowStageReadyListener(
    @Value("${application.ui.title}") String applicationTitle,
    @Value("classpath:/views/main-window.fxml") Resource mainWindowFxml,
    @Value("classpath:/icons/icon.png") Resource appIcon,
    MainWindowController mainWindowController,
    ApplicationContext ac
  ) {
    try {
      this.appIcon = new Image(new BufferedInputStream(new FileInputStream(appIcon.getFile())));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.applicationTitle = applicationTitle;
    this.mainWindowFxml = mainWindowFxml;
    this.mainWindowController = mainWindowController;
    this.ac = ac;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);
      mainWindowController.setMainWindowScene(new Scene(mainWindowFxmlLoader.load()));
      primaryStage.getIcons().add(appIcon);
      primaryStage.setTitle(applicationTitle);
      primaryStage.setScene(mainWindowController.getMainWindowScene());
      mainWindowController.onStageReady();
      primaryStage.show();

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }
}
