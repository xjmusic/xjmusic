// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.ControlMode;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class MainPaneRightController extends ProjectController {
  private static final PseudoClass ENGAGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("engaged");
  private final double intensityDefaultValue;
  private final FabricationService fabricationService;

  @FXML
  protected ScrollPane container;

  @FXML
  protected VBox controlsContainer;
  private final DoubleProperty intensity = new SimpleDoubleProperty(0);
  private final ObservableMap<String, String> taxonomyCategoryToggleSelections = FXCollections.observableHashMap();
  private final Set<ToggleGroup> taxonomyToggleGroups = new HashSet<>();
  private final String sliderTrackColorActive;
  private final String sliderTrackColorDefault;


  public MainPaneRightController(
    @Value("classpath:/views/main-pane-right.fxml") Resource fxml,
    @Value("${slider.track.color.active}") String sliderTrackColorActive,
    @Value("${slider.track.color.default}") String sliderTrackColorDefault,
    @Value("${intensity.default.value}") double intensityDefaultValue,
    ApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.sliderTrackColorActive = sliderTrackColorActive;
    this.sliderTrackColorDefault = sliderTrackColorDefault;
    this.intensityDefaultValue = intensityDefaultValue;
    this.fabricationService = fabricationService;
  }

  @Override
  public void onStageReady() {
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
      initIntensitySlider();
      if (fabricationService.controlModeProperty().get().equals(ControlMode.MACRO)) {
        initMacroSelections();
      } else if (fabricationService.controlModeProperty().get().equals(ControlMode.TAXONOMY)) {
        initTaxonomySelections();
      }

    } else {
      controlsContainer.getChildren().clear();
    }
  }

  /**
   Create the intensity control slider
   */
  private void initIntensitySlider() {
    VBox holder = new VBox();
    holder.getStyleClass().add("intensity-slider-holder");

    Label label = new Label("Intensity");
    label.setPrefWidth(Double.MAX_VALUE);
    label.setTextAlignment(TextAlignment.CENTER);
    label.setAlignment(Pos.CENTER);
    label.setPadding(new Insets(0, 0, 5, 0));
    holder.getChildren().add(label);

    Slider slider = new Slider();
    slider.setValue(38);
    slider.setMin(0);
    slider.setMax(100);
    slider.setMajorTickUnit(50);
    slider.setMinorTickCount(5);
    slider.setShowTickMarks(true);
    holder.getChildren().add(slider);

    // Slider to control intensity https://www.pivotaltracker.com/story/show/186950076
    intensity.bind(Bindings.createDoubleBinding(() -> slider.valueProperty().get() / 100, slider.valueProperty()));
    intensity.addListener((observable, oldValue, newValue) -> fabricationService.setIntensity(newValue.floatValue()));
    slider.valueProperty().addListener((o, ov, value) -> setSliderTrackStyle(slider, value.intValue()));
    slider.valueProperty().setValue(intensityDefaultValue * 100);
    Platform.runLater(() -> setSliderTrackStyle(slider, (int) (intensityDefaultValue * 100)));

    controlsContainer.getChildren().add(holder);
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
      fabricationService.overrideMacroProgramIdProperty().addListener((ChangeListener<? super UUID>) (o, ov, value) ->
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
      group.selectedToggleProperty().addListener((ChangeListener<? super Toggle>) (o, ov, value) -> {
        if (Objects.isNull(value)) {
          taxonomyCategoryToggleSelections.remove(category.getName());
        } else {
          taxonomyCategoryToggleSelections.put(category.getName(), ((ToggleButton) value).getId());
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
    controlsContainer.getChildren().add(btn);
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
    controlsContainer.getChildren().add(btn);
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
    controlsContainer.getChildren().add(label);
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
    controlsContainer.getChildren().add(btn);
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


  /**
   Set the style for the slider track

   @param slider the slider
   @param value  the value
   */
  private void setSliderTrackStyle(Slider slider, int value) {
    StackPane trackPane = (StackPane) slider.lookup(".track");
    if (Objects.nonNull(trackPane))
      trackPane.setStyle(computeSliderTrackStyle(value));
  }

  /**
   Compute the style for the slider track, with a value from 0 to 100

   @param value the value
   @return the style
   */
  private String computeSliderTrackStyle(int value) {
    return String.format("-fx-background-color: linear-gradient(to right, %s %d%%, %s %d%%);", sliderTrackColorActive, value, sliderTrackColorDefault, value);
  }

  @Override
  public void onStageClose() {
    // no op
  }
}
