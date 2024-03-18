// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.controllers.content.program.bind_mode.BindModeController;
import io.xj.gui.controllers.content.program.chord_edit_mode.ChordEditModeController;
import io.xj.gui.controllers.content.program.event_edit_mode.EventEditModeController;
import io.xj.gui.nav.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.GridChoice;
import io.xj.gui.types.ProgramEditorMode;
import io.xj.gui.types.ViewContentMode;
import io.xj.gui.types.ZoomChoice;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
  private final Resource entityMemesFxml;
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>(null);
  private final ObjectProperty<UUID> sequenceId = new SimpleObjectProperty<>();
  private final BooleanBinding programHasSequences;
  private final CmdModalController cmdModalController;
  private final EventEditModeController editEventController;
  private final ChordEditModeController editChordController;
  private final BindModeController bindController;
  private final ChangeListener<? super ViewContentMode> onEditProgram = (o, ov, v) -> {
    teardown();
    if (Objects.equals(uiStateService.contentModeProperty().get(), ViewContentMode.ProgramEditor) && uiStateService.currentProgramProperty().isNotNull().get())
      setup(uiStateService.currentProgramProperty().get().getId());
  };
  private final Runnable updateProgramName;
  private final Runnable updateProgramType;
  private final Runnable updateProgramState;
  private final Runnable updateProgramKey;
  private final Runnable updateProgramTempo;
  private final Runnable updateSequenceName;
  private final Runnable updateSequenceTotal;
  private final Runnable updateSequenceKey;
  private final Runnable updateSequenceIntensity;

  @FXML
  StackPane programMemeContainer;

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
  TextField programNameField;

  @FXML
  ComboBox<ProgramType> programTypeChooser;

  @FXML
  ComboBox<ProgramState> programStateChooser;

  @FXML
  TextField programKeyField;

  @FXML
  TextField programTempoField;

  @FXML
  TextField sequenceNameField;

  @FXML
  TextField sequenceTotalField;

  @FXML
  TextField sequenceKeyField;

  @FXML
  TextField sequenceIntensityField;

  @FXML
  ComboBox<GridChoice> gridChooser;

  @FXML
  ComboBox<ZoomChoice> zoomChooser;

  @FXML
  ToggleButton snapButton;

  @FXML
  Button sequenceActionLauncher;

  @FXML
  Button sequenceSelectorLauncher;

  @FXML
  HBox timelineOptionsGroup;

  @FXML
  HBox currentSequenceGroup;

  @FXML
  Label noSequencesLabel;

  @FXML
  AnchorPane container;

  public ProgramEditorController(
    @Value("classpath:/views/content/program/program-editor.fxml") Resource fxml,
    @Value("classpath:/views/content/program/program-config.fxml") Resource configFxml,
    @Value("classpath:/views/content/common/entity-memes.fxml") Resource entityMemesFxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController,
    BindModeController bindController,
    EventEditModeController editEventController,
    ChordEditModeController editChordController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.configFxml = configFxml;
    this.entityMemesFxml = entityMemesFxml;
    this.cmdModalController = cmdModalController;
    this.bindController = bindController;
    this.editEventController = editEventController;
    this.editChordController = editChordController;

    programHasSequences = Bindings.createBooleanBinding(() -> !uiStateService.sequencesOfCurrentProgramProperty().isEmpty(), uiStateService.sequencesOfCurrentProgramProperty());

    updateProgramName = () -> projectService.update(Program.class, programId.get(), "name", programNameField.getText());
    updateProgramType = () -> {
      if (uiStateService.currentProgramProperty().isNull().get()) return;
      if (Objects.equals(programTypeChooser.getValue(), uiStateService.currentProgramProperty().get().getType()))
        return;
      if (!projectService.updateProgramType(programId.get(), programTypeChooser.getValue())) {
        programTypeChooser.setValue(uiStateService.currentProgramProperty().get().getType());
        return;
      }
      switch (programTypeChooser.getValue()) {
        case Macro -> editorModeToggleGroup.selectToggle(bindButton);
        case Detail -> editorModeToggleGroup.selectToggle(editButton);
      }
      teardown();
      setup(programId.get());
    };
    updateProgramState = () -> projectService.update(Program.class, programId.get(), "state", programStateChooser.getValue());
    updateProgramKey = () -> projectService.update(Program.class, programId.get(), "key", programKeyField.getText());
    updateProgramTempo = () -> projectService.update(Program.class, programId.get(), "tempo", programTempoField.getText());
    updateSequenceName = () -> {
      if (uiStateService.currentProgramSequenceProperty().isNotNull().get())
        projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "name", sequenceNameField.getText());
    };
    updateSequenceTotal = () -> {
      if (uiStateService.currentProgramSequenceProperty().isNull().get()) return;
      if (!projectService.updateProgramSequenceTotal(uiStateService.currentProgramSequenceProperty().get().getId(), sequenceTotalField.getText())) {
        sequenceTotalField.setText(uiStateService.currentProgramSequenceProperty().get().getTotal().toString());
      }
    };
    updateSequenceKey = () -> {
      if (uiStateService.currentProgramSequenceProperty().isNotNull().get())
        projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "key", sequenceKeyField.getText());
    };
    updateSequenceIntensity = () -> {
      if (uiStateService.currentProgramSequenceProperty().isNotNull().get())
        projectService.update(ProgramSequence.class, uiStateService.currentProgramSequenceProperty().get().getId(), "intensity", sequenceIntensityField.getText());
    };
  }

  @Override
  public void onStageReady() {
    bindController.onStageReady();
    editEventController.onStageReady();
    editChordController.onStageReady();

    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get().route() == Route.ProgramEditor,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    uiStateService.contentModeProperty().addListener(onEditProgram);
    programTypeChooser.setItems(FXCollections.observableArrayList(ProgramType.values()));
    programStateChooser.setItems(FXCollections.observableArrayList(ProgramState.values()));
    gridChooser.valueProperty().bindBidirectional(uiStateService.programEditorGridProperty());
    zoomChooser.valueProperty().bindBidirectional(uiStateService.programEditorZoomProperty());
    snapButton.selectedProperty().bindBidirectional(uiStateService.programEditorSnapProperty());
    gridChooser.setItems(uiStateService.getProgramEditorGridChoices());
    zoomChooser.setItems(uiStateService.getProgramEditorZoomChoices());
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    currentSequenceGroup.visibleProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    currentSequenceGroup.managedProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    noSequencesLabel.visibleProperty().bind(programHasSequences.not());
    noSequencesLabel.managedProperty().bind(programHasSequences.not());
    programHasSequences.addListener((o, ov, value) -> sequenceSelectorLauncher.setDisable(!value));

    // Fields lose focus on Enter key press
    UiUtils.blurOnEnterKeyPress(programNameField);
    UiUtils.blurOnEnterKeyPress(programKeyField);
    UiUtils.blurOnEnterKeyPress(programTempoField);
    UiUtils.blurOnEnterKeyPress(sequenceNameField);
    UiUtils.blurOnEnterKeyPress(sequenceTotalField);
    UiUtils.blurOnEnterKeyPress(sequenceKeyField);
    UiUtils.blurOnEnterKeyPress(sequenceIntensityField);

    // On blur, update the underlying value
    UiUtils.onBlur(programNameField, updateProgramName);
    UiUtils.onChange(programTypeChooser.valueProperty(), updateProgramType);
    UiUtils.onChange(programStateChooser.valueProperty(), updateProgramState);
    UiUtils.onBlur(programKeyField, updateProgramKey);
    UiUtils.onBlur(programTempoField, updateProgramTempo);
    UiUtils.onBlur(sequenceNameField, updateSequenceName);
    UiUtils.onBlur(sequenceTotalField, updateSequenceTotal);
    UiUtils.onBlur(sequenceKeyField, updateSequenceKey);
    UiUtils.onBlur(sequenceIntensityField, updateSequenceIntensity);

    // On selection of a sequence, set values from the sequence
    uiStateService.currentProgramSequenceProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        sequenceId.set(uiStateService.currentProgramSequenceProperty().get().getId());
        sequenceNameField.setText(uiStateService.currentProgramSequenceProperty().get().getName());
        sequenceKeyField.setText(uiStateService.currentProgramSequenceProperty().get().getKey());
        sequenceTotalField.setText(uiStateService.currentProgramSequenceProperty().get().getTotal().toString());
        sequenceIntensityField.setText(uiStateService.currentProgramSequenceProperty().get().getIntensity().toString());
      }
    });

    timelineOptionsGroup.visibleProperty().bind(editButton.selectedProperty().and(uiStateService.currentProgramSequenceProperty().isNotNull()));

    editButton.disableProperty().bind(programTypeChooser.valueProperty().isEqualTo(ProgramType.Macro));
    bindButton.disableProperty().bind(Bindings.createBooleanBinding(() -> !PROGRAM_TYPES_WITH_BINDINGS.contains(programTypeChooser.valueProperty().get()), programTypeChooser.valueProperty()));
    editorModeToggleGroup.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, editButton))
        uiStateService.programEditorModeProperty().set(ProgramEditorMode.Edit);
      else if (Objects.equals(v, bindButton))
        uiStateService.programEditorModeProperty().set(ProgramEditorMode.Bind);
      else
        uiStateService.programEditorModeProperty().set(null);
    });
    UiUtils.toggleGroupPreventDeselect(editorModeToggleGroup);

    container.maxWidthProperty().bind(container.getScene().getWindow().widthProperty());
    container.maxHeightProperty().bind(container.getScene().getWindow().heightProperty());
  }

  @FXML
  void handlePressedSequenceSelectorLauncher() {
    uiStateService.launchPopupSelectorMenu(
      sequenceSelectorLauncher,
      (PopupSelectorMenuController controller) -> controller.setup(
        projectService.getContent().getSequencesOfProgram(programId.get()),
        (sequenceId) -> uiStateService.currentProgramSequenceProperty().set(projectService.getContent().getProgramSequence(sequenceId).orElse(null))
      )
    );
  }

  @FXML
  void handlePressedSequenceActionLauncher() {
    uiStateService.launchPopupActionMenu(
      sequenceActionLauncher,
      (PopupActionMenuController controller) -> controller.setup(
        "New Sequence",
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
    bindController.onStageClose();
    editEventController.onStageClose();
    editChordController.onStageClose();
    LOG.info("Closed Program Editor");
  }

  @FXML
  void handleEditConfig() {
    uiStateService.launchModalMenu(
      configFxml,
      configButton,
      (ProgramConfigController controller) -> controller.setup(programId.get()),
      LaunchMenuPosition.from(configButton),
      true, null);
  }

  /**
   Update the Program Editor with the current Program.
   */
  private void setup(UUID programId) {
    var program = projectService.getContent().getProgram(programId)
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    LOG.info("Will edit Program \"{}\"", program.getName());
    this.programId.set(program.getId());
    programNameField.setText(program.getName());
    programTypeChooser.setValue(program.getType());
    programStateChooser.setValue(program.getState());
    programKeyField.setText(program.getKey());
    programTempoField.setText(program.getTempo().toString());

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
    editEventController.setup(programId);
    editChordController.setup(programId);
  }

  /**
   Teardown the Program Editor
   */
  private void teardown() {
    bindController.teardown();
    editEventController.teardown();
    editChordController.teardown();
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
    if (!projectService.getContent().getPatternsOfSequence(currentSequence.getId()).isEmpty()) {
      projectService.showWarningAlert("Cannot Delete Sequence", "Must delete Sequence Patterns first!", "Cannot delete a sequence while it is still referenced by sequence patterns.");
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
      ProgramSequence clonedProgramSequence = projectService.cloneProgramSequence(currentSequence.getId());
      uiStateService.currentProgramSequenceProperty().set(clonedProgramSequence);
    } catch (Exception e) {
      LOG.info("Failed to clone sequence ");
    }
  }
}
