// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.controllers.content.program.bind_mode.BindModeController;
import io.xj.gui.controllers.content.program.edit_mode.EditModeController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ProgramEditorMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class ProgramEditorController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private static final Set<ProgramType> PROGRAM_TYPES_WITH_BINDINGS = Set.of(ProgramType.Main, ProgramType.Macro);
  private final Resource configFxml;
  private final Resource popupSelectorMenuFxml;
  private final Resource popupActionMenuFxml;
  private final Resource entityMemesFxml;
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>(null);
  private final ObjectProperty<UUID> sequenceId = new SimpleObjectProperty<>();
  private final StringProperty programName = new SimpleStringProperty("");
  private final ObjectProperty<ProgramType> type = new SimpleObjectProperty<>();
  private final ObjectProperty<ProgramState> state = new SimpleObjectProperty<>();
  private final StringProperty key = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0);
  private final SpinnerValueFactory<Double> tempoValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1300, 0);
  private final FloatProperty sequenceIntensity = new SimpleFloatProperty(0);
  private final SpinnerValueFactory<Double> sequenceIntensityValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, sequenceIntensity.doubleValue(), 0.1);
  private final ObjectProperty<Double> sequenceIntensityDoubleValue = new SimpleObjectProperty<>(sequenceIntensityValueFactory.getValue());
  private final IntegerProperty sequenceTotal = new SimpleIntegerProperty(0);
  private final SpinnerValueFactory<Integer> sequenceTotalValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, sequenceTotal.intValue());
  private final ObjectProperty<Integer> sequenceTotalIntegerValue = new SimpleObjectProperty<>(sequenceTotalValueFactory.getValue());
  private final ObjectProperty<Double> tempoDoubleValue = new SimpleObjectProperty<>(tempoValueFactory.getValue());
  protected final SimpleStringProperty sequencePropertyName = new SimpleStringProperty("");
  private final SimpleStringProperty sequencePropertyKey = new SimpleStringProperty("");
  private final BooleanBinding programHasSequences;
  private final CmdModalController cmdModalController;
  private final EditModeController editController;
  private final BindModeController bindController;
  private final ChangeListener<? super ContentMode> onEditProgram = (o, ov, v) -> {
    teardown();
    if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor) && uiStateService.currentProgramProperty().isNotNull().get())
      setup(uiStateService.currentProgramProperty().get().getId());
  };

  @FXML
  Spinner<Double> tempoChooser;

  @FXML
  StackPane programMemeContainer;

  @FXML
  TextField keyField;

  @FXML
  ComboBox<ProgramState> stateChooser;

  @FXML
  ComboBox<ProgramType> typeChooser;

  @FXML
  TextField programNameField;

  @FXML
  Button duplicateButton;

  @FXML
  ToggleGroup editorModeToggleGroup;

  @FXML
  ToggleButton editButton;

  @FXML
  ToggleButton bindButton;

  @FXML
  Button configButton;

  @FXML
  Spinner<Double> sequenceIntensityChooser;

  @FXML
  TextField sequenceKeyField;

  @FXML
  Spinner<Integer> sequenceTotalChooser;

  @FXML
  TextField sequenceNameField;

  @FXML
  Button sequenceActionLauncher;

  @FXML
  ToggleButton snapButton;

  @FXML
  ComboBox<ZoomChoice> zoomChooser;

  @FXML
  ComboBox<GridChoice> gridChooser;

  @FXML
  Button sequenceSelectorLauncher;

  @FXML
  HBox timelineOptionsGroup;

  @FXML
  HBox currentSequenceGroup;

  @FXML
  Label noSequencesLabel;

  @FXML
  VBox container;

  public ProgramEditorController(
    @Value("classpath:/views/content/program/program-editor.fxml") Resource fxml,
    @Value("classpath:/views/content/program/program-config.fxml") Resource configFxml,
    @Value("classpath:/views/content/common/popup-selector-menu.fxml") Resource popupSelectorMenuFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    @Value("classpath:/views/content/common/entity-memes.fxml") Resource entityMemesFxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController,
    EditModeController editController,
    BindModeController bindController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.configFxml = configFxml;
    this.popupSelectorMenuFxml = popupSelectorMenuFxml;
    this.popupActionMenuFxml = popupActionMenuFxml;
    this.entityMemesFxml = entityMemesFxml;
    this.cmdModalController = cmdModalController;
    this.editController = editController;
    this.bindController = bindController;

    programHasSequences = Bindings.createBooleanBinding(() -> !uiStateService.sequencesOfCurrentProgramProperty().isEmpty(), uiStateService.sequencesOfCurrentProgramProperty());
  }

  @Override
  public void onStageReady() {
    editController.onStageReady();
    bindController.onStageReady();

    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    uiStateService.contentModeProperty().addListener(onEditProgram);
    typeChooser.setItems(FXCollections.observableArrayList(ProgramType.values()));
    stateChooser.setItems(FXCollections.observableArrayList(ProgramState.values()));
    UiUtils.onBlur(programNameField, this::handleProgramSave);
    UiUtils.onBlur(keyField, this::handleProgramSave);
    UiUtils.onBlur(tempoChooser, this::handleProgramSave);
    UiUtils.onBlur(typeChooser, this::handleProgramSave);
    UiUtils.onBlur(stateChooser, this::handleProgramSave);
    gridChooser.valueProperty().bindBidirectional(uiStateService.programEditorGridProperty());
    zoomChooser.valueProperty().bindBidirectional(uiStateService.programEditorZoomProperty());
    gridChooser.setItems(uiStateService.getProgramEditorGridChoices());
    zoomChooser.setItems(uiStateService.getProgramEditorZoomChoices());
    sequenceNameField.textProperty().bindBidirectional(sequencePropertyName);
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);
    programNameField.textProperty().bindBidirectional(programName);
    typeChooser.valueProperty().bindBidirectional(type);

    // if the type is changed to macro, force selection of bind mode
    type.addListener((observable, oldValue, newValue) -> {
      if (newValue == ProgramType.Macro) {
        editorModeToggleGroup.selectToggle(bindButton);
      }
    });

    sequenceKeyField.textProperty().bindBidirectional(sequencePropertyKey);
    // Bind Label text to Chooser value with formatting
    stateChooser.valueProperty().bindBidirectional(state);
    keyField.textProperty().bindBidirectional(key);

    // Update the ObjectProperty when the Chooser value changes(sequenceIntensity)
    sequenceIntensity.bind(Bindings.createFloatBinding(() -> sequenceIntensityDoubleValue.get().floatValue(), sequenceIntensityDoubleValue));
    sequenceIntensityValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> sequenceIntensityDoubleValue.set(newValue));
    sequenceIntensityChooser.setValueFactory(sequenceIntensityValueFactory);

    // Update the ObjectProperty when the Chooser value changes(sequenceTotal)
    sequenceTotal.bind(Bindings.createIntegerBinding(sequenceTotalIntegerValue::get, sequenceTotalIntegerValue));
    sequenceTotalValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> sequenceTotalIntegerValue.set(newValue));
    sequenceTotalChooser.setValueFactory(sequenceTotalValueFactory);

    // Bind the Chooser's value to the ObjectProperty
    tempo.bind(Bindings.createFloatBinding(() -> tempoDoubleValue.get().floatValue(), tempoDoubleValue));

    // Update the ObjectProperty when the Chooser value changes
    tempoChooser.valueProperty().addListener((observable, oldValue, newValue) -> tempoDoubleValue.set(newValue));
    tempoChooser.setValueFactory(tempoValueFactory);
    currentSequenceGroup.visibleProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    currentSequenceGroup.managedProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    noSequencesLabel.visibleProperty().bind(programHasSequences.not());
    noSequencesLabel.managedProperty().bind(programHasSequences.not());
    sequenceSelectorLauncher.disableProperty().bind(programHasSequences.not());

    // Fields lose focus on Enter key press
    UiUtils.transferFocusOnEnterKeyPress(programNameField);
    UiUtils.transferFocusOnEnterKeyPress(keyField);
    UiUtils.transferFocusOnEnterKeyPress(tempoChooser);
    UiUtils.transferFocusOnEnterKeyPress(sequenceNameField);
    UiUtils.transferFocusOnEnterKeyPress(sequenceKeyField);
    UiUtils.transferFocusOnEnterKeyPress(sequenceTotalChooser);
    UiUtils.transferFocusOnEnterKeyPress(sequenceIntensityChooser);

    sequenceNameField.focusedProperty().addListener((o, ov, focused) -> {
      try {
        if (!focused) {
          projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "name",
            sequenceNameField.textProperty().get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    sequenceTotalChooser.focusedProperty().addListener((o, ov, focused) -> {
      try {
        if (!focused) {
          sequenceTotalValueFactory.setValue(sequenceTotalChooser.getValue());
          projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "total",
            sequenceTotalValueFactory.getValue());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    sequenceKeyField.focusedProperty().addListener((o, ov, focused) -> {
      try {
        if (!focused) {
          projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "key",
            sequencePropertyKey.get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    uiStateService.currentProgramSequenceProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        sequenceId.set(uiStateService.currentProgramSequenceProperty().get().getId());
        sequencePropertyName.set(uiStateService.currentProgramSequenceProperty().get().getName());
        sequencePropertyKey.set(uiStateService.currentProgramSequenceProperty().get().getKey());
        sequenceTotalValueFactory.setValue(uiStateService.currentProgramSequenceProperty().get().getTotal().intValue());
        sequenceIntensityValueFactory.setValue(Double.valueOf(uiStateService.currentProgramSequenceProperty().get().getIntensity()));
      }
    });

    timelineOptionsGroup.visibleProperty().bind(editButton.selectedProperty().and(uiStateService.currentProgramSequenceProperty().isNotNull()));

    editButton.disableProperty().bind(type.isEqualTo(ProgramType.Macro));
    bindButton.disableProperty().bind(Bindings.createBooleanBinding(() -> !PROGRAM_TYPES_WITH_BINDINGS.contains(type.get()), type));
    editorModeToggleGroup.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, editButton))
        uiStateService.programEditorModeProperty().set(ProgramEditorMode.Edit);
      else if (Objects.equals(v, bindButton))
        uiStateService.programEditorModeProperty().set(ProgramEditorMode.Bind);
      else
        uiStateService.programEditorModeProperty().set(null);
    });
    UiUtils.toggleGroupPreventDeselect(editorModeToggleGroup);
  }

  @FXML
  void handlePressedSequenceSelectorLauncher() {
    UiUtils.launchModalMenu(sequenceSelectorLauncher, popupSelectorMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupSelectorMenuController controller) -> controller.setup(
        projectService.getContent().getSequencesOfProgram(programId.get()),
        (sequenceId) -> uiStateService.currentProgramSequenceProperty().set(projectService.getContent().getProgramSequence(sequenceId).orElse(null))
      )
    );
  }

  @FXML
  void handlePressedSequenceActionLauncher() {
    UiUtils.launchModalMenu(sequenceActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
        this::handleCreateSequence,
        programHasSequences.get() ? this::handleDeleteSequence : null,
        programHasSequences.get() ? this::handleCloneSequence : null
      )
    );
  }

  @FXML
  void openCloneDialog() {
    var program = projectService.getContent().getProgram(programId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    cmdModalController.cloneProgram(program);
  }

  @Override
  public void onStageClose() {
    editController.onStageClose();
    bindController.onStageClose();
    LOG.info("Closed Program Editor");
  }

  protected void handleProgramSave() {
    var program = projectService.getContent().getProgram(programId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    program.setName(programName.get());
    program.setKey(key.get());
    program.setTempo(tempo.get());
    program.setType(type.get());
    program.setState(state.get());
    projectService.updateProgram(program);
  }

  @FXML
  void handleEditConfig() {
    UiUtils.launchModalMenu(configButton, configFxml, ac, themeService.getMainScene().getWindow(), false,
      (ProgramConfigController controller) -> controller.setup(programId.get())
    );
  }

  /**
   Update the Program Editor with the current Program.
   */
  private void setup(UUID programId) {
    var program = projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    LOG.info("Will edit Program \"{}\"", program.getName());
    this.programId.set(program.getId());
    this.programName.set(program.getName());
    this.type.set(program.getType());
    this.state.set(program.getState());
    this.key.set(program.getKey());
    this.tempoValueFactory.setValue(Double.valueOf(program.getTempo()));

    List<ProgramSequence> programSequences = projectService.getContent().getSequencesOfProgram(programId).stream()
      .sorted(Comparator.comparing(ProgramSequence::getName)).toList();
    if (!programSequences.isEmpty()) {
      uiStateService.currentProgramSequenceProperty().set(programSequences.get(0));
    } else {
      uiStateService.currentProgramSequenceProperty().set(null);
    }

    // When the program editor opens, if the program is a Macro-type, show the binding mode view, else always start on the Edit mode view
    if (Objects.equals(program.getType(), ProgramType.Macro)) {
      editorModeToggleGroup.selectToggle(bindButton);
    } else {
      editorModeToggleGroup.selectToggle(editButton);
    }

    setupProgramMemeContainer();
    bindController.setup(programId);
    editController.setup(programId);
  }

  /**
   Teardown the Program Editor
   */
  private void teardown() {
    bindController.teardown();
    editController.teardown();
  }

  /**
   Set up the Program Meme Container FXML and its controller
   */
  private void setupProgramMemeContainer() {
    try {
      FXMLLoader loader = new FXMLLoader(entityMemesFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      programMemeContainer.getChildren().clear();
      programMemeContainer.getChildren().add(root);
      EntityMemesController entityMemesController = loader.getController();
      entityMemesController.setup(
        true, () -> projectService.getContent().getMemesOfProgram(programId.get()),
        () -> projectService.createProgramMeme(programId.get()),
        (Object meme) -> {
          try {
            projectService.update(meme);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      );
    } catch (IOException e) {
      LOG.error("Error loading Entity Memes window! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Create a new sequence
   */
  private void handleCreateSequence() {
    try {
      ProgramSequence newProgramSequence = projectService.createProgramSequence(programId.get());
      uiStateService.currentProgramSequenceProperty().set(newProgramSequence);
    } catch (Exception e) {
      LOG.info("Failed to create new sequence! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Delete the current sequence
   */
  private void handleDeleteSequence() {
    var currentSequence = uiStateService.currentProgramSequenceProperty().get();
    if (Objects.isNull(currentSequence)) return;
    if (!projectService.getContent().getBindingsOfSequence(currentSequence.getId()).isEmpty()) {
      projectService.showWarningAlert("Cannot Delete Sequence", "Must delete Sequence Bindings first!", "Cannot delete a sequence while it is still referenced by sequence bindings.");
      return;
    }
    if (!projectService.showConfirmationDialog("Delete Sequence?", "This action cannot be undone.", String.format("Are you sure you want to delete the Sequence \"%s\"?", currentSequence.getName())))
      return;
    try {
      projectService.deleteContent(currentSequence);
      var sequences = projectService.getContent().getSequencesOfProgram(programId.get()).stream()
        .sorted(Comparator.comparing(ProgramSequence::getName)).toList();
      if (!sequences.isEmpty()) {
        uiStateService.currentProgramSequenceProperty().set(sequences.get(0));
      } else {
        uiStateService.currentProgramSequenceProperty().set(null);
      }
    } catch (Exception e) {
      LOG.info("Failed to delete sequence " + currentSequence.getName());
    }
  }

  /**
   Clone the current sequence
   */
  private void handleCloneSequence() {
    var currentSequence = uiStateService.currentProgramSequenceProperty().get();
    if (Objects.isNull(currentSequence)) return;
    try {
      ProgramSequence clonedProgramSequence = projectService.cloneProgramSequence(currentSequence.getId(), "Clone of " + currentSequence.getName());
      uiStateService.currentProgramSequenceProperty().set(clonedProgramSequence);
    } catch (Exception e) {
      LOG.info("Failed to clone sequence ");
    }
  }
}
