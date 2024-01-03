// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.ControlMode;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox macroSelectionContainer;

  @FXML
  protected VBox taxonomySelectionContainer;
  private final Map<String, ToggleGroup> taxonomyCategoryToggleGroups = new ConcurrentHashMap<>();

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
    taxonomySelectionContainer.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    taxonomySelectionContainer.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

    // bind a listener to changes in the fabrication service source material
    uiStateService.isManualFabricationActiveProperty().addListener(this::onManualFabricationMode);
  }

  /**
   When the manual fabrication mode is activated, populate the macro and taxonomy selection containers.
   If deactivated, clear the containers.

   @param observable the observable property
   @param ignored    the old value
   @param isActive   the new value
   */
  private void onManualFabricationMode(Observable observable, Boolean ignored, Boolean isActive) {
    if (isActive) {
      if (fabricationService.controlModeProperty().get().equals(ControlMode.MACRO)) {
        initMacroSelections();
      } else if (fabricationService.controlModeProperty().get().equals(ControlMode.TAXONOMY)) {
        initTaxonomySelections();
      }

    } else {
      taxonomySelectionContainer.getChildren().clear();
      macroSelectionContainer.getChildren().clear();
    }
  }

  /**
   Create a button in the vbox macroSelectionContainer for each macro program in the source material
   */
  private void initMacroSelections() {
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
  }

  /**
   Create a button in the vbox taxonomySelectionContainer for each taxonomy program in the source material
   */
  private void initTaxonomySelections() {
    // create a button
    var goButton = new Button("Go");
    goButton.getStyleClass().add("button");
    goButton.setOnAction(event ->
      fabricationService.gotoTaxonomyCategoryMemes(taxonomyCategoryToggleGroups.values().stream()
        .map(ToggleGroup::getSelectedToggle)
        .filter(Objects::nonNull)
        .map(toggle -> ((ToggleButton) toggle).getText())
        .toList()));
    // add the button to the vbox taxonomySelectionContainer
    taxonomySelectionContainer.getChildren().add(goButton);

    var taxonomy = fabricationService.getMemeTaxonomy();
    if (taxonomy.isEmpty()) return;
    taxonomyCategoryToggleGroups.clear();
    // for each taxonomy program, create a button in the vbox taxonomySelectionContainer
    taxonomy.get().getCategories().forEach(category -> {
      ToggleGroup group = new ToggleGroup();

      // create a meme selection property for the category
      taxonomyCategoryToggleGroups.put(category.getName(), group);

      // create a text label for the category and add it to the vbox children
      var label = new Label(category.getName());
      label.setMaxWidth(Double.MAX_VALUE);
      label.setAlignment(Pos.CENTER);
      label.getStyleClass().add("category-label");
      taxonomySelectionContainer.getChildren().add(label);

      // for each meme in the category, create a button in the vbox taxonomySelectionContainer
      category.getMemes().forEach(meme -> {

        // create a button for each meme in the category
        var memeButton = new ToggleButton(meme);
        memeButton.setToggleGroup(group);
        memeButton.getStyleClass().add("button");
        // add the button to the vbox taxonomySelectionContainer
        taxonomySelectionContainer.getChildren().add(memeButton);
      });
    });
  }

  @Override
  public void onStageClose() {
    // no op
  }

}
