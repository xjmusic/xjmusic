// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.UIStateService;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox macroSelectionContainer;

  @FXML
  protected VBox taxonomySelectionContainer;
  private final Map<String, ToggleGroup> taxonomyCategoryToggleGroup = new ConcurrentHashMap<>();

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
      initTaxonomySelections();

    } else {
      taxonomySelectionContainer.getChildren().clear();
      macroSelectionContainer.getChildren().clear();
    }
  }

  /**
   Create a button in the vbox taxonomySelectionContainer for each taxonomy program in the source material
   */
  private void initTaxonomySelections() {
    var taxonomy = fabricationService.getMemeTaxonomy();
    if (taxonomy.isEmpty()) return;
    taxonomyCategoryToggleGroup.clear();
    // for each taxonomy program, create a button in the vbox taxonomySelectionContainer
    taxonomy.get().getCategories().forEach(category -> {
      ToggleGroup group = new ToggleGroup();

      // create a meme selection property for the category
      taxonomyCategoryToggleGroup.put(category.getName(), group);

      // create a text label for the category and add it to the vbox children
      var label = new Label(category.getName());
      label.setMaxWidth(Double.MAX_VALUE);
      label.setAlignment(Pos.CENTER);
      label.getStyleClass().add("category-label");
      taxonomySelectionContainer.getChildren().add(label);

      // for each meme in the category, create a button in the vbox taxonomySelectionContainer
      category.getMemes().forEach(meme -> {
        // create a button
        var button = new ToggleButton(meme);
        button.setToggleGroup(group);
        button.getStyleClass().add("button");
        button.setOnAction(event -> handleToggleSelection(group, button));
        // add the button to the vbox taxonomySelectionContainer
        taxonomySelectionContainer.getChildren().add(button);
      });

      /*
        TODO add a button to submit the latest meme taxonomy
                // get the current meme selections
          var memeSelections = taxonomyCategorySelectionProperties.values().stream()
            .map(StringProperty::getValue)
            .filter(meme -> !meme.isEmpty())
            .toList();
          // go to the meme selections
          fabricationService.gotoTaxonomyCategoryMeme(memeSelections);

       */
    });
  }

  private void handleToggleSelection(ToggleGroup group, ToggleButton selectedButton) {
/*
    group.getToggles().forEach(toggle -> {
      ToggleButton button = (ToggleButton) toggle;
      if (button != selectedButton) {
        button.setDisable(selectedButton.isSelected());
      }
    });
*/
  }

  @Override
  public void onStageClose() {
    // no op
  }

}
