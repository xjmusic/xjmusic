// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.UIStateService;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox macroSelectionContainer;

  public MainPaneRightController(
    FabricationService fabricationService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    macroSelectionContainer.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    macroSelectionContainer.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

    // bind a listener to changes in the fabrication service source material
    uiStateService.isManualFabricationActiveProperty().addListener(this::onActivityChanged);
  }

  private void onActivityChanged(Observable observable, Boolean ignored, Boolean value) {
    if (value) {
      // if active, create a button in the vbox macroSelectionContainer for each macro program in the source material
      var macroPrograms = fabricationService.getAllMacroPrograms();
      // for each macro program, create a button in the vbox macroSelectionContainer
      macroPrograms.forEach(macroProgram -> {
        // create a button
        var button = new Button(macroProgram.getName());
        button.getStyleClass().add("button");
        button.onActionProperty().setValue(event -> {
          // when the button is clicked, set the macro program in the fabrication service
          fabricationService.gotoMacroProgram(macroProgram);
        });
        // add the button to the vbox macroSelectionContainer
        macroSelectionContainer.getChildren().add(button);
      });

    } else {
      // if inactive, clear the vbox macroSelectionContainer
      macroSelectionContainer.getChildren().clear();
    }
  }

  @Override
  public void onStageClose() {
    // no op
  }

}
