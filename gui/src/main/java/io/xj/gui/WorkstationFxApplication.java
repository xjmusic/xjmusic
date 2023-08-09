package io.xj.gui;

import io.xj.gui.events.StageReadyEvent;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Objects;

public class WorkstationFxApplication extends Application {
  static final Logger LOG = LoggerFactory.getLogger(WorkstationFxApplication.class);

  @Nullable
  private ConfigurableApplicationContext ac;

  public WorkstationFxApplication() {
  }

  @Override
  public void start(Stage primaryStage) {
    if (Objects.isNull(ac)) {
      LOG.error("Cannot start without application context!");
      return;
    }
    LOG.info("Will publish StageReadyEvent");
    ac.publishEvent(new StageReadyEvent(primaryStage));
  }

  @Override
  public void stop() {
    if (Objects.isNull(ac)) {
      LOG.error("Cannot stop without application context!");
      return;
    }
    LOG.info("Will close application context");
    ac.close();
    Platform.exit();
  }

  @Override
  public void init() {
    ac = new SpringApplicationBuilder()
      .headless(false)
      .sources(WorkstationApplication.class)
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
