// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.ControlMode;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox selectionContainer;

  private final Map<String, ToggleGroup> taxonomyCategoryToggleGroups = new ConcurrentHashMap<>();

  private static final PseudoClass ENGAGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("engaged");

  public MainPaneRightController(
    FabricationService fabricationService,
    UIStateService uiStateService
  ) {
    this.fabricationService = fabricationService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    selectionContainer.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    selectionContainer.managedProperty().bind(uiStateService.isManualFabricationModeProperty());
    selectionContainer.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    selectionContainer.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

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
      selectionContainer.getChildren().clear();
    }
  }

  /**
   Create a button for each macro program in the source material
   */
  private void initMacroSelections() {
    ToggleGroup group = new ToggleGroup();
    var macroPrograms = fabricationService.getAllMacroPrograms();

    addEngageButton(selectionContainer, event ->
      fabricationService.doOverrideMacro(macroPrograms.stream()
        .filter(macroProgram -> macroProgram.getId().equals(UUID.fromString(((ToggleButton) group.getSelectedToggle()).getId())))
        .findFirst()
        .orElseThrow()));

    addResetButton(selectionContainer, event -> {
      fabricationService.resetOverrideMacro();
      group.selectToggle(null);
    });

    addGroupLabel(selectionContainer, "Macro Programs");

    // for each macro program, create a toggle button in a toggle group in the vbox macroSelectionContainer
    macroPrograms.forEach(macroProgram -> {
      var button = new ToggleButton(macroProgram.getName());
      button.setId(macroProgram.getId().toString());
      button.setToggleGroup(group);
      button.getStyleClass().add("button");
      fabricationService.overrideMacroProgramIdProperty().addListener((ChangeListener<? super UUID>) (ignored0, ignored1, ignored2) ->
        updateToggleGroupButtonEngaged(button, fabricationService.overrideMacroProgramIdProperty().get().equals(macroProgram.getId())));
      selectionContainer.getChildren().add(button);
    });
  }

  /**
   Create a button for each taxonomy program in the source material
   */
  private void initTaxonomySelections() {
    addEngageButton(selectionContainer, event ->
      fabricationService.doOverrideMemes(taxonomyCategoryToggleGroups.values().stream()
        .map(ToggleGroup::getSelectedToggle)
        .filter(Objects::nonNull)
        .map(toggle -> ((ToggleButton) toggle).getText())
        .toList()));

    addResetButton(selectionContainer, event -> {
      fabricationService.resetOverrideMemes();
      taxonomyCategoryToggleGroups.values().forEach((toggleGroup -> toggleGroup.selectToggle(null)));
    });

    // for each taxonomy program, create a button in the vbox taxonomySelectionContainer
    var taxonomy = fabricationService.getMemeTaxonomy();
    if (taxonomy.isEmpty()) return;
    taxonomyCategoryToggleGroups.clear(); // keep track of the toggle groups
    taxonomy.get().getCategories().forEach(category -> {
      ToggleGroup group = new ToggleGroup();
      taxonomyCategoryToggleGroups.put(category.getName(), group);
      addGroupLabel(selectionContainer, category.getName());

      // for each meme in the category, create a button in the vbox taxonomySelectionContainer
      category.getMemes().forEach(meme -> {
        var memeButton = new ToggleButton(meme);
        memeButton.setToggleGroup(group);
        memeButton.getStyleClass().add("button");
        selectionContainer.getChildren().add(memeButton);
        fabricationService.overrideMemesProperty().addListener((ListChangeListener.Change<? extends String> ignored) ->
          updateToggleGroupButtonEngaged(memeButton, fabricationService.overrideMemesProperty().contains(meme)));
      });
    });
  }

  /**
   Create the engage button and add it to the container

   @param container     in which to add the button
   @param actionHandler the action handler for the button
   */
  private void addEngageButton(VBox container, EventHandler<ActionEvent> actionHandler) {
    var engageButton = new Button("Engage");
    engageButton.getStyleClass().add("button");
    engageButton.setOnAction(actionHandler);
    container.getChildren().add(engageButton);
  }

  /**
   Create the reset button and add it to the container

   @param container     in which to add the button
   @param actionHandler the action handler for the button
   */
  private void addResetButton(VBox container, EventHandler<ActionEvent> actionHandler) {
    var resetButton = new Button("Reset");
    resetButton.getStyleClass().add("button");
    resetButton.setOnAction(actionHandler);
    container.getChildren().add(resetButton);
  }

  /**
   Create a label for the given text and add it to the container

   @param container in which to add the label
   @param text      for the label
   */
  private void addGroupLabel(VBox container, String text) {
    var label = new Label(text);
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER);
    label.getStyleClass().add("group-label");
    container.getChildren().add(label);
  }

  private void updateToggleGroupButtonEngaged(ToggleButton button, boolean engaged) {
    button.pseudoClassStateChanged(ENGAGED_PSEUDO_CLASS, engaged);
  }

  @Override
  public void onStageClose() {
    // no op
  }

}
