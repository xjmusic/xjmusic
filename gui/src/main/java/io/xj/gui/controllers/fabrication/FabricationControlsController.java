// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.fabrication;

import io.xj.gui.ProjectController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class FabricationControlsController extends ProjectController {
  private static final PseudoClass ENGAGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("engaged");
  private final FabricationService fabricationService;
  private final ObservableMap<String, String> taxonomyCategoryToggleSelections = FXCollections.observableHashMap();
  private final String sliderTrackColorActive;
  private final String sliderTrackColorDefault;

  @FXML
  ScrollPane container;

  @FXML
  VBox controlsContainer;

  public FabricationControlsController(
    @Value("classpath:/views/main-pane-right.fxml") Resource fxml,
    @Value("${view.sliderTrackColorActive}") String sliderTrackColorActive,
    @Value("${view.sliderTrackColorDefault}") String sliderTrackColorDefault,
    ApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.sliderTrackColorActive = sliderTrackColorActive;
    this.sliderTrackColorDefault = sliderTrackColorDefault;
    this.fabricationService = fabricationService;
  }

  @Override
  public void onStageReady() {
    container.visibleProperty().bind(uiStateService.isManualFabricationModeProperty());
    container.managedProperty().bind(uiStateService.isManualFabricationModeProperty());

    // bind a listener to changes in the fabrication service source material
    uiStateService.isManualFabricationActiveProperty().addListener(this::onManualFabricationMode);
  }

  @Override
  public void onStageClose() {
    // no op
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
   Slider to control intensity https://www.pivotaltracker.com/story/show/186950076
   */
  private void initIntensitySlider() {
    // Slider to set intensity override value
    Slider slider = new Slider();
    slider.setValue(38);
    slider.setMin(0);
    slider.setMax(100);
    slider.setMajorTickUnit(50);
    slider.setMinorTickCount(5);
    slider.setShowTickMarks(true);
    slider.disableProperty().bind(fabricationService.intensityOverrideActiveProperty().not());

    // Toggle switch to control whether intensity override is active
    ToggleSwitch active = new ToggleSwitch();
    active.selectedProperty().bindBidirectional(fabricationService.intensityOverrideActiveProperty());
    active.selectedProperty().addListener((o, ov, value) -> setSliderTrackStyle(slider, fabricationService.intensityOverrideActiveProperty().get()));

    // Set the intensity override value when the slider is moved
    slider.valueProperty().addListener((o, ov, value) -> {
      setSliderTrackStyle(slider, true);
      fabricationService.intensityOverrideProperty().set(value.doubleValue() / 100);
    });
    slider.valueProperty().addListener((o, ov, value) -> setSliderTrackStyle(slider, fabricationService.intensityOverrideActiveProperty().get()));
    slider.valueProperty().setValue(fabricationService.intensityOverrideProperty().getValue() * 100);

    // Label over switch and slider
    VBox col = new VBox();
    col.getStyleClass().add("intensity-slider-holder");
    Label label = new Label("Override Intensity");
    label.setPrefWidth(200);
    label.setTextAlignment(TextAlignment.LEFT);
    label.setAlignment(Pos.CENTER_LEFT);
    label.setPadding(new Insets(0, 0, 0, 8));
    HBox header = new HBox();
    header.setPrefWidth(Double.MAX_VALUE);
    header.setPadding(new Insets(5, 5, 5, 5));
    header.getChildren().add(active);
    header.getChildren().add(label);
    col.getChildren().add(header);
    col.getChildren().add(slider);
    controlsContainer.getChildren().add(col);
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

    // Label
    addGroupLabel("Macro Programs");

    // for each macro program, create a toggle button in a toggle group
    macroPrograms.forEach(macroProgram -> {
      var button = addToggleButton(group, macroProgram.getName(), macroProgram.getId().toString());
      Runnable onOverrideChange = () -> updateButtonEngaged(button, Objects.equals(fabricationService.overrideMacroProgramIdProperty().get(), macroProgram.getId()));
      fabricationService.overrideMacroProgramIdProperty().addListener((ChangeListener<? super UUID>) (o, ov, value) -> onOverrideChange.run());
      onOverrideChange.run(); // show initial state
    });
  }

  /**
   Create a button for each taxonomy program in the source material
   */
  private void initTaxonomySelections() {
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

    // Each taxonomy category is in a labeled toggle group
    taxonomy.get().getCategories().forEach(category -> {
      ToggleGroup group = new ToggleGroup();
      group.selectedToggleProperty().addListener((ChangeListener<? super Toggle>) (o, ov, value) -> {
        if (Objects.isNull(value)) {
          taxonomyCategoryToggleSelections.remove(category.getName());
        } else {
          taxonomyCategoryToggleSelections.put(category.getName(), ((ToggleButton) value).getId());
        }
      });
      addGroupLabel(category.getName());

      // Select the first meme in each category
      taxonomyCategoryToggleSelections.put(category.getName(), category.getMemes().get(0));

      // Each meme in the category is a toggle button
      category.getMemes().forEach(meme -> {
        var button = addToggleButton(group, meme, meme);
        Runnable onOverrideChange = () -> updateButtonEngaged(button, fabricationService.overrideMemesProperty().contains(meme));
        fabricationService.overrideMemesProperty().addListener((SetChangeListener.Change<? extends String> ignored) -> onOverrideChange.run());
        onOverrideChange.run(); // show initial state
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
   Set the style for the slider track@param slider the slider@param active whether the slider is active
   */
  private void setSliderTrackStyle(Slider slider, boolean active) {
    StackPane trackPane = (StackPane) slider.lookup(".track");
    if (Objects.nonNull(trackPane))
      trackPane.setStyle(active ?
        String.format("-fx-background-color: linear-gradient(to right, %s %d%%, %s %d%%);", sliderTrackColorActive, (int) slider.getValue(), sliderTrackColorDefault, (int) slider.getValue())
        : "");
  }
}
