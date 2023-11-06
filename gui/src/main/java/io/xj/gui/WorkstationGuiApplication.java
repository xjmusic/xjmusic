// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub",
    "io.xj.nexus",
    "io.xj.gui",
  })
public class WorkstationGuiApplication {

  public WorkstationGuiApplication(
  ) {
  }

  public static void main(String[] args) {
    Application.launch(WorkstationGuiFxApplication.class, args);
  }
}
