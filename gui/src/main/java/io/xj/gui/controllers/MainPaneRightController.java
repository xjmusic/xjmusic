// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.UIStateService;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {

  private final LabService labService;
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox macroSelectionContainer;

  public MainPaneRightController(
    LabService labService,
    FabricationService fabricationService,
    UIStateService uiStateService
  ) {
    this.labService = labService;
    this.fabricationService = fabricationService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    macroSelectionContainer.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    macroSelectionContainer.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

    // bind a listener to changes in the fabrication service source material
  }

  @Override
  public void onStageClose() {
    // no op
  }

}
