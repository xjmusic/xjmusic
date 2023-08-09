package io.xj.gui.events;

import io.xj.gui.MainWindowScene;
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
public class StageReadyListener implements ApplicationListener<StageReadyEvent> {
  static final Logger LOG = LoggerFactory.getLogger(StageReadyListener.class);
  private final String applicationTitle;
  private final Resource mainWindowFxml;

  private final ApplicationContext ac;
  private final String darkTheme;
  private final MainWindowScene mainWindowScene;

  public StageReadyListener(
    @Value("${application.ui.title}") String applicationTitle,
    @Value("classpath:/views/main-window.fxml") Resource mainWindowFxml,
    @Value("${gui.theme.dark}") String darkTheme,
    MainWindowScene mainWindowScene,
    ApplicationContext ac
  ) {
    this.darkTheme = darkTheme;
    this.mainWindowScene = mainWindowScene;
    this.applicationTitle = applicationTitle;
    this.mainWindowFxml = mainWindowFxml;
    this.ac = ac;
  }

  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      var primaryStage = event.getStage();
      FXMLLoader mainWindowFxmlLoader = new FXMLLoader(mainWindowFxml.getURL());
      mainWindowFxmlLoader.setControllerFactory(ac::getBean);
      mainWindowScene.set(new Scene(mainWindowFxmlLoader.load()));
      primaryStage.setTitle(applicationTitle);
      primaryStage.setScene(mainWindowScene.get());
      mainWindowScene.get().getStylesheets().add(darkTheme);
      primaryStage.show();

    } catch (IOException e) {
      LOG.error("Failed to set the scene on the primary stage!", e);
    }
  }
}
