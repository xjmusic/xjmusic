// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import jakarta.annotation.Nullable;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Objects;

public class WorkstationGuiFxApplication extends Application {
  static final Logger LOG = LoggerFactory.getLogger(WorkstationGuiFxApplication.class);

  @Nullable
  ConfigurableApplicationContext ac;

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
    exit(ac);
  }

  @Override
  public void init() {
    ac = new SpringApplicationBuilder()
      .headless(false)
      .sources(WorkstationGuiApplication.class)
      .initializers(getInitializer())
      .run(getParameters().getRaw().toArray(new String[0]));
  }

  ApplicationContextInitializer<GenericApplicationContext> getInitializer() {
    return
      ac -> {
        ac.registerBean(Application.class, () -> WorkstationGuiFxApplication.this);
        ac.registerBean(Parameters.class, this::getParameters);
        ac.registerBean(HostServices.class, this::getHostServices);
      };
  }

  /**
   This can be used from anywhere to close the Spring application context and quit the application

   @param ac the application context
   */
  public static void exit(ApplicationContext ac) {
    LOG.info("Will close application context");
    var exitCode = SpringApplication.exit(ac, () -> 0);
    LOG.info("Will exit with code {}", exitCode);
    System.exit(exitCode);
  }
}
