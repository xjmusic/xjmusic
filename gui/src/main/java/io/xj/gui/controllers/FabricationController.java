// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class FabricationController implements ReadyAfterBootController {
  final MainPaneRightController mainPaneRightController;
  final FabricationTimelineController fabricationTimelineController;

  public FabricationController(
    MainPaneRightController mainPaneRightController,
    FabricationTimelineController fabricationTimelineController
  ) {
    this.mainPaneRightController = mainPaneRightController;
    this.fabricationTimelineController = fabricationTimelineController;
  }

  @FXML
  public ScrollPane mainTimeline;

  @FXML
  public VBox mainPaneRight;

  @Override
  public void onStageReady() {
    mainPaneRightController.onStageReady();
    fabricationTimelineController.onStageReady();
  }

  @Override
  public void onStageClose() {
    mainPaneRightController.onStageClose();
    fabricationTimelineController.onStageClose();
  }
}
