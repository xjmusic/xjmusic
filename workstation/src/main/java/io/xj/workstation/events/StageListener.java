package io.xj.workstation.events;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(StageListener.class);
  private final String applicationTitle;
  private final Resource fxml;

  private final ApplicationContext ac;

  public StageListener(
    @Value("application.ui.title") String applicationTitle,
    @Value("classpath:/main-window.fxml") Resource fxml,
    ApplicationContext ac
  ) {
    this.applicationTitle = applicationTitle;
    this.fxml = fxml;
    this.ac = ac;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
      fxmlLoader.setControllerFactory(ac::getBean);
      var scene = new Scene(fxmlLoader.load());
      primaryStage.setTitle(applicationTitle);
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }
}
