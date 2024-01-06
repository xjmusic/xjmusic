// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.FabricationService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.ControlMode;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class MainPaneRightController extends VBox implements ReadyAfterBootController {
  private final FabricationService fabricationService;
  private final UIStateService uiStateService;

  @FXML
  protected VBox container;

  private final ObservableMap<String, String> taxonomyCategoryToggleSelections = FXCollections.observableHashMap();
  private final Set<ToggleGroup> taxonomyToggleGroups = new HashSet<>();

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
    container.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    container.managedProperty().bind(uiStateService.isManualFabricationModeProperty());
    container.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    container.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

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
      container.getChildren().clear();
    }
  }

  /**
   Create a button for each macro program in the source material
   */
  private void initMacroSelections() {
    ToggleGroup group = new ToggleGroup();
    var macroPrograms = fabricationService.getAllMacroPrograms();

    // Engage Macro Override
    addEngageButton(
      event -> fabricationService.doOverrideMacro(macroPrograms.stream()
        .filter(macroProgram -> macroProgram.getId().equals(UUID.fromString(((ToggleButton) group.getSelectedToggle()).getId())))
        .findFirst()
        .orElseThrow()),
      Bindings.createBooleanBinding(
        () -> Objects.isNull(group.selectedToggleProperty().get()) ||
          Objects.equals(fabricationService.overrideMacroProgramIdProperty().get(), UUID.fromString(((ToggleButton) group.getSelectedToggle()).getId())),
        group.selectedToggleProperty(), fabricationService.overrideMacroProgramIdProperty()
      )
    );

    // Release Macro Override
    addReleaseButton(
      event -> {
        fabricationService.resetOverrideMacro();
        group.selectToggle(null);
      },
      fabricationService.overrideMacroProgramIdProperty().isNull()
    );

    // Label
    addGroupLabel("Macro Programs");

    // for each macro program, create a toggle button in a toggle group
    macroPrograms.forEach(macroProgram -> {
      var button = addToggleButton(group, macroProgram.getName(), macroProgram.getId().toString());
      fabricationService.overrideMacroProgramIdProperty().addListener((ChangeListener<? super UUID>) (ignored0, ignored1, ignored2) ->
        updateButtonEngaged(button, Objects.equals(fabricationService.overrideMacroProgramIdProperty().get(), macroProgram.getId())));
    });
  }

  /**
   Create a button for each taxonomy program in the source material
   */
  private void initTaxonomySelections() {
    taxonomyToggleGroups.clear();
    taxonomyCategoryToggleSelections.clear();
    var taxonomy = fabricationService.getMemeTaxonomy();
    if (taxonomy.isEmpty()) return;

    // Engage Meme Override
    addEngageButton(
      event -> fabricationService.doOverrideMemes(taxonomyCategoryToggleSelections.values()),
      Bindings.createBooleanBinding(
        () -> taxonomyCategoryToggleSelections.isEmpty() ||
          fabricationService.overrideMemesProperty().containsAll(taxonomyCategoryToggleSelections.values()),
        taxonomyCategoryToggleSelections, fabricationService.overrideMemesProperty()
      )
    );

    // Release Meme Override
    addReleaseButton(
      event -> {
        fabricationService.resetOverrideMemes();
        taxonomyToggleGroups.forEach((toggleGroup -> toggleGroup.selectToggle(null)));
      },
      Bindings.createBooleanBinding(() ->
          Objects.isNull(fabricationService.overrideMemesProperty()) || fabricationService.overrideMemesProperty().isEmpty(),
        fabricationService.overrideMemesProperty())
    );

    // Each taxonomy category is in a labeled toggle group
    taxonomy.get().getCategories().forEach(category -> {
      ToggleGroup group = new ToggleGroup();
      taxonomyToggleGroups.add(group);
      group.selectedToggleProperty().addListener((ChangeListener<? super Toggle>) (ignored0, ignored1, newToggle) -> {
        if (Objects.isNull(newToggle)) {
          taxonomyCategoryToggleSelections.remove(category.getName());
        } else {
          taxonomyCategoryToggleSelections.put(category.getName(), ((ToggleButton) newToggle).getId());
        }
      });
      addGroupLabel(category.getName());

      // Each meme in the category is a toggle button
      category.getMemes().forEach(meme -> {
        var button = addToggleButton(group, meme, meme);
        fabricationService.overrideMemesProperty().addListener((SetChangeListener.Change<? extends String> ignored) ->
          updateButtonEngaged(button, fabricationService.overrideMemesProperty().contains(meme)));
      });
    });
  }

  /**
   Create the engage button and add it to the container

   @param actionHandler   handle press
   @param disabledBinding binding to disable the button
   */
  private void addEngageButton(EventHandler<ActionEvent> actionHandler, BooleanBinding disabledBinding) {
    var btn = new Button("Engage");
    btn.getStyleClass().add("button");
    btn.getStyleClass().add("engage-button");
    btn.setOnAction(actionHandler);
    btn.disableProperty().bind(disabledBinding);
    container.getChildren().add(btn);
  }

  /**
   Create the reset button and add it to the container

   @param actionHandler   handle press
   @param disabledBinding binding to disable the button
   */
  private void addReleaseButton(EventHandler<ActionEvent> actionHandler, BooleanBinding disabledBinding) {
    var btn = new Button("Release");
    btn.getStyleClass().add("button");
    btn.getStyleClass().add("release-button");
    btn.setOnAction(actionHandler);
    btn.disableProperty().bind(disabledBinding);
    btn.opacityProperty().bind(Bindings.when(disabledBinding).then(0.3).otherwise(0.8));
    container.getChildren().add(btn);
  }

  /**
   Create a label for the given text and add it to the container

   @param text for the label
   */
  private void addGroupLabel(String text) {
    var label = new Label(text);
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER);
    label.getStyleClass().add("group-label");
    container.getChildren().add(label);
  }

  /**
   Create a toggle button for the given text and add it to the container

   @param group of toggle buttons
   @param text  for the button
   @param id    for the button
   @return the button
   */
  private ToggleButton addToggleButton(ToggleGroup group, String text, String id) {
    var btn = new ToggleButton(text);
    btn.setId(id);
    btn.setToggleGroup(group);
    btn.getStyleClass().add("button");
    btn.getStyleClass().add("toggle-button");
    container.getChildren().add(btn);
    return btn;
  }

  /**
   Update the pseudo class for the given button

   @param button  to update
   @param engaged true if engaged
   */
  private <N extends ButtonBase> void updateButtonEngaged(N button, boolean engaged) {
    button.pseudoClassStateChanged(ENGAGED_PSEUDO_CLASS, engaged);
  }


  @Override
  public void onStageClose() {
    // no op
  }

}
