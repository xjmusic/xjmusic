// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.HubTopology;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub",
    "io.xj.nexus",
    "io.xj.gui",
  })
public class WorkstationGuiApplication {

  final EntityFactory entityFactory;

  public WorkstationGuiApplication(
    EntityFactory entityFactory
  ) {
    this.entityFactory = entityFactory;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() {
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
  }

  public static void main(String[] args) {
    Application.launch(WorkstationGuiFxApplication.class, args);
  }
}
