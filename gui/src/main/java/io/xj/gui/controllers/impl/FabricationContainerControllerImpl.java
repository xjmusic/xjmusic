// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.impl;

import io.xj.gui.controllers.FabricationContainerController;
import io.xj.gui.controllers.ReadyAfterBootController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class FabricationContainerControllerImpl implements ReadyAfterBootController, FabricationContainerController {
  final MainPaneRightControllerImpl mainPaneRightController;
  final MainTimelineControllerImpl mainTimelineController;

  public FabricationContainerControllerImpl(
    MainPaneRightControllerImpl mainPaneRightController,
    MainTimelineControllerImpl mainTimelineController
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
