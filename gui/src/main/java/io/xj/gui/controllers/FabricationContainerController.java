// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class FabricationContainerController implements ReadyAfterBootController {
  final MainPaneRightController mainPaneRightController;
  final MainTimelineController mainTimelineController;

  public FabricationContainerController(
    MainPaneRightController mainPaneRightController,
    MainTimelineController mainTimelineController
  ) {
    this.mainPaneRightController = mainPaneRightController;
    this.mainTimelineController = mainTimelineController;
  }

  @FXML
  public ScrollPane mainTimeline;

  @FXML
  public VBox mainPaneRight;

  @Override
  public void onStageReady() {
    mainPaneRightController.onStageReady();
    mainTimelineController.onStageReady();
  }

  @Override
  public void onStageClose() {
    mainPaneRightController.onStageClose();
    mainTimelineController.onStageClose();
  }
}
