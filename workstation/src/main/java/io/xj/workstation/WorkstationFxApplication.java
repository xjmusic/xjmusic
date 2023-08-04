package io.xj.workstation;

import io.xj.workstation.events.StageReadyEvent;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class WorkstationFxApplication extends Application {

  private ConfigurableApplicationContext context;

  @Override
  public void start(Stage primaryStage) {
    context.publishEvent(new StageReadyEvent(primaryStage));
  }

  @Override
  public void stop() {
    context.close();
    Platform.exit();
  }

  @Override
  public void init() {
    context = new SpringApplicationBuilder()
      .headless(false)
      .sources(WorkstationFxApplication.class)
      .initializers(getInitializer())
      .run(getParameters().getRaw().toArray(new String[0]));
  }

  private ApplicationContextInitializer<GenericApplicationContext> getInitializer() {
    return
      ac -> {
        ac.registerBean(Application.class, () -> WorkstationFxApplication.this);
        ac.registerBean(Parameters.class, this::getParameters);
        ac.registerBean(HostServices.class, this::getHostServices);
      };
  }
}
