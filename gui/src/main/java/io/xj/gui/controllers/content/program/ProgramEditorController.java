// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static io.xj.gui.services.UIStateService.OPEN_PSEUDO_CLASS;

@Service
public class ProgramEditorController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private static final String DEFAULT_GRID_VALUE = "1/4";
  private static final String DEFAULT_ZOOM_VALUE = "25%";
  private static final Set<ProgramType> PROGRAM_TYPES_WITH_BINDINGS = Set.of(ProgramType.Main, ProgramType.Macro);
  private final Resource configFxml;
  private final Resource sequenceSelectorFxml;
  private final Resource sequenceManagementFxml;
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
  private final ObservableList<ProgramType> programTypes = FXCollections.observableArrayList(ProgramType.values());
  private final ObservableList<ProgramState> programStates = FXCollections.observableArrayList(ProgramState.values());
  private final StringProperty gridProperty = new SimpleStringProperty("");
  private final StringProperty zoomProperty = new SimpleStringProperty("");
  private final ObservableList<String> gridDivisions =
    FXCollections.observableArrayList(Arrays.asList("1/4", "1/8", "1/16", "1/32"));
  private final ObservableList<String> zoomOptions =
    FXCollections.observableArrayList(Arrays.asList("5%", "10%", "25%", "50%", "100%", "200%", "300%", "400%"));
  protected final SimpleStringProperty sequencePropertyName = new SimpleStringProperty("");
  private final SimpleStringProperty sequencePropertyKey = new SimpleStringProperty("");
  private final BooleanProperty programHasSequences = new SimpleBooleanProperty();
  private final CmdModalController cmdModalController;
  private final ProgramEditorModeEditController editController;
  private final ProgramEditorModeBindController bindController;

  @FXML
  public Spinner<Double> tempoChooser;

  @FXML
  public StackPane programMemeContainer;

  @FXML
  public TextField keyField;

  @FXML
  public ComboBox<ProgramState> stateChooser;

  @FXML
  public ComboBox<ProgramType> typeChooser;

  @FXML
  public TextField programNameField;

  @FXML
  public Button duplicateButton;

  @FXML
  public ToggleGroup editorModeToggleGroup;

  @FXML
  public ToggleButton editButton;

  @FXML
  public ToggleButton bindButton;

  @FXML
  public Button configButton;

  @FXML
  public Spinner<Double> sequenceIntensityChooser;

  @FXML
  public TextField sequenceKeyField;

  @FXML
  public Spinner<Integer> sequenceTotalChooser;

  @FXML
  public TextField sequenceNameField;

  @FXML
  public Button sequenceManagementLauncher;

  @FXML
  public ToggleButton snapButton;

  @FXML
  public ComboBox<String> zoomChooser;

  @FXML
  public ComboBox<String> gridChooser;

  @FXML
  public Button sequenceSelectorLauncher;

  @FXML
  public HBox timelineOptionsGroup;

  @FXML
  public HBox currentSequenceGroup;

  @FXML
  public Label noSequencesLabel;

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  public ProgramEditorController(
    @Value("classpath:/views/content/program/program-editor.fxml") Resource fxml,
    @Value("classpath:/views/content/program/program-config.fxml") Resource configFxml,
    @Value("classpath:/views/content/program/sequence-selector.fxml") Resource sequenceSelectorFxml,
    @Value("classpath:/views/content/program/sequence-management.fxml") Resource sequenceManagementFxml,
    @Value("classpath:/views/content/common/entity-memes.fxml") Resource entityMemesFxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController,
    ProgramEditorModeEditController editController,
    ProgramEditorModeBindController bindController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.configFxml = configFxml;
    this.sequenceSelectorFxml = sequenceSelectorFxml;
    this.sequenceManagementFxml = sequenceManagementFxml;
    this.entityMemesFxml = entityMemesFxml;
    this.cmdModalController = cmdModalController;
    this.editController = editController;
    this.bindController = bindController;
  }

  @Override
  public void onStageReady() {
    editController.onStageReady();
    bindController.onStageReady();

    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor))
        setup();
    });
    editorModeToggleGroup.selectToggle(editButton);
    typeChooser.setItems(programTypes);
    stateChooser.setItems(programStates);
    setTextProcessing(programNameField);
    setTextProcessing(keyField);
    setChooserSelectionProcessing(tempoChooser);
    setComboboxSelectionProcessing(typeChooser);
    setComboboxSelectionProcessing(stateChooser);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);
    gridChooser.setItems(gridDivisions);
    zoomChooser.setItems(zoomOptions);
    sequenceNameField.textProperty().bindBidirectional(sequencePropertyName);
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);
    programNameField.textProperty().bindBidirectional(programName);
    typeChooser.valueProperty().bindBidirectional(type);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);

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

    // Whether the current program has sequences
    uiStateService.currentProgramProperty().addListener((o, ov, value) -> {
      if (value != null) {
        programHasSequences.set(!projectService.getContent().getSequencesOfProgram(value.getId()).isEmpty());
      }
    });
    projectService.addProjectUpdateListener(ProgramSequence.class, () -> {
      if (uiStateService.currentProgramProperty().get() != null) {
        programHasSequences.set(!projectService.getContent().getSequencesOfProgram(uiStateService.currentProgramProperty().get().getId()).isEmpty());
      } else {
        programHasSequences.set(false);
      }
    });

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

    editButton.selectedProperty().bindBidirectional(uiStateService.programEditorEditModeProperty());
    editButton.disableProperty().bind(type.isEqualTo(ProgramType.Macro));

    bindButton.selectedProperty().bindBidirectional(uiStateService.programEditorBindModeProperty());
    bindButton.disableProperty().bind(Bindings.createBooleanBinding(() -> !PROGRAM_TYPES_WITH_BINDINGS.contains(type.get()), type));

    timelineOptionsGroup.visibleProperty().bind(editButton.selectedProperty().and(uiStateService.currentProgramSequenceProperty().isNotNull()));

    UiUtils.toggleGroupPreventDeselect(editorModeToggleGroup);
  }

  @FXML
  protected void launchSequenceSelectorUI() {
    UiUtils.launchModalMenu(sequenceSelectorLauncher, sequenceSelectorFxml, ac, themeService.getMainScene().getWindow(),
      true, (SequenceSelectorController controller, Stage stage) -> controller.setup(
        programId.get(),
        (sequenceId) -> uiStateService.currentProgramSequenceProperty().set(projectService.getContent().getProgramSequence(sequenceId).orElse(null))
      )
    );
  }

  @FXML
  protected void launchSequenceManagementUI() {
    UiUtils.launchModalMenu(sequenceManagementLauncher, sequenceManagementFxml, ac, themeService.getMainScene().getWindow(),
      true, (SequenceManagementController controller, Stage stage) -> controller.setup(programId.get(), stage)
    );
  }

  /**
   Handles value changes listening in the TextField components
   */
  private void setTextProcessing(TextField textField) {
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   Handles value changes listening in the  value Chooser components
   */
  private void setChooserSelectionProcessing(Spinner<?> chooser) {
    chooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   Handles value changes listening in the ComboBox components
   */
  private void setComboboxSelectionProcessing(ComboBox<?> comboBox) {
    comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  @FXML
  protected void openCloneDialog() {
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
  protected void handleEditConfig() {
    UiUtils.launchModalMenu(configButton, configFxml, ac, themeService.getMainScene().getWindow(), false,
      (ProgramConfigController controller, Stage stage) -> controller.setup(stage, programId.get())
    );
  }

  /**
   Update the Program Editor with the current Program.
   */
  private void setup() {
    if (Objects.isNull(uiStateService.currentProgramProperty().get()))
      return;
    var program = projectService.getContent().getProgram(uiStateService.currentProgramProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    LOG.info("Will edit Program \"{}\"", program.getName());
    this.programId.set(program.getId());
    this.programName.set(program.getName());
    this.type.set(program.getType());
    this.state.set(program.getState());
    this.key.set(program.getKey());
    this.tempoValueFactory.setValue(Double.valueOf(program.getTempo()));

    List<ProgramSequence> programSequences = projectService.getContent().getSequencesOfProgram(programId.get()).stream()
      .sorted(Comparator.comparing(ProgramSequence::getName)).toList();
    if (!programSequences.isEmpty()) {
      uiStateService.currentProgramSequenceProperty().set(programSequences.get(0));
    } else {
      uiStateService.currentProgramSequenceProperty().set(null);
    }

    gridChooser.setValue(DEFAULT_GRID_VALUE);
    zoomChooser.setValue(DEFAULT_ZOOM_VALUE);

    // When the program editor opens, if the program is a Macro-type, show the binding mode view, else always start on the Edit mode view
    if (Objects.equals(program.getType(), ProgramType.Macro)) {
      bindButton.setSelected(true);
      editorModeToggleGroup.selectToggle(bindButton);
    } else {
      editButton.setSelected(true);
      editorModeToggleGroup.selectToggle(editButton);
    }

    setupProgramMemeContainer();
    bindController.setup(programId.get());
    editController.setup(programId.get());
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
      LOG.error("Error loading Entity Memes window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   @return the current program ID
   */
  public UUID getProgramId() {
    return programId.get();
  }
}
