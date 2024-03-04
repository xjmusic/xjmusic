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
import io.xj.gui.utils.WindowUtils;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.xj.gui.services.UIStateService.OPEN_PSEUDO_CLASS;

@Service
public class ProgramEditorController extends ProjectController {
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
  public HBox bindingModeContainer;
  @FXML
  public HBox sequenceBindingsContainer;
  @FXML
  public HBox gridAndZoomGroup;
  @FXML
  public HBox currentSequenceGroup;
  @FXML
  public Label noSequenceLabel;
  @FXML
  protected VBox container;
  @FXML
  protected TextField fieldName;

  @Value("classpath:/views/content/program/program-config.fxml")
  private Resource configFxml;

  @Value("classpath:/views/content/program/sequence-selector.fxml")
  private Resource sequenceSelectorFxml;

  @Value("classpath:/views/content/program/sequence-management.fxml")
  private Resource sequenceManagementFxml;

  @Value("classpath:/views/content/program/sequence-binding-column.fxml")
  private Resource sequenceBindingColumnFxml;

  @Value("classpath:/views/content/common/entity-memes.fxml")
  private Resource entityMemesFxml;

  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
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
  private final CmdModalController cmdModalController;
  protected final ObjectProperty<ProgramSequence> currentProgramSequence = new SimpleObjectProperty<>();
  protected ObservableList<ProgramSequence> programSequenceObservableList = FXCollections.observableArrayList();
  private final Collection<SequenceBindingColumnController> sequenceBindingColumnControllers = new HashSet<>();

  public ProgramEditorController(
    @Value("classpath:/views/content/program/program-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.cmdModalController = cmdModalController;
  }

  @Override
  public void onStageReady() {
    bindingModeContainer.visibleProperty().bind(bindButton.selectedProperty());
    bindingModeContainer.managedProperty().bind(bindButton.selectedProperty());
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor))
        setupProgram();
    });
    editButton.setSelected(true);
    editorModeToggleGroup.selectToggle(editButton);
    createDisabilityBindingForTypes(editButton, Arrays.asList(ProgramType.Main, ProgramType.Macro));
    createDisabilityBindingForTypes(bindButton, Arrays.asList(ProgramType.Main, ProgramType.Macro));
    createVisibilityBindingForTypes(gridAndZoomGroup, List.of(ProgramType.Macro));
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
    gridChooser.setValue("1/4");
    zoomChooser.setItems(zoomOptions);
    zoomChooser.setValue("25%");
    createDisabilityBindingForTypes(snapButton, Arrays.asList(ProgramType.Beat, ProgramType.Detail));
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

    // Bind the Chooser's value to the ObjectProperty
    tempo.bind(Bindings.createFloatBinding(() -> tempoDoubleValue.get().floatValue(), tempoDoubleValue));
    // Update the ObjectProperty when the Chooser value changes
    tempoChooser.valueProperty().addListener((observable, oldValue, newValue) -> tempoDoubleValue.set(newValue));
    tempoChooser.setValueFactory(tempoValueFactory);
    currentSequenceGroup.visibleProperty().bind(currentProgramSequence.isNotNull());
    currentSequenceGroup.managedProperty().bind(currentProgramSequence.isNotNull());
    noSequenceLabel.visibleProperty().bind(currentProgramSequence.isNull());
    noSequenceLabel.managedProperty().bind(currentProgramSequence.isNull());

    // Fields lose focus on Enter key press
    WindowUtils.transferFocusOnEnterKeyPress(programNameField);
    WindowUtils.transferFocusOnEnterKeyPress(keyField);
    WindowUtils.transferFocusOnEnterKeyPress(tempoChooser);
    WindowUtils.transferFocusOnEnterKeyPress(sequenceNameField);
    WindowUtils.transferFocusOnEnterKeyPress(sequenceKeyField);
    WindowUtils.transferFocusOnEnterKeyPress(sequenceTotalChooser);
    WindowUtils.transferFocusOnEnterKeyPress(sequenceIntensityChooser);

    sequenceNameField.focusedProperty().addListener((o, ov, focused) -> {
      try {
        if (!focused) {
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "name",
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
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "total",
            sequenceTotalValueFactory.getValue());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    sequenceKeyField.focusedProperty().addListener((o, ov, focused) -> {
      try {
        if (!focused) {
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "key",
            sequencePropertyKey.get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    currentProgramSequence.addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        sequenceId.set(currentProgramSequence.get().getId());
        sequencePropertyName.set(currentProgramSequence.get().getName());
        sequencePropertyKey.set(currentProgramSequence.get().getKey());
        sequenceTotalValueFactory.setValue(currentProgramSequence.get().getTotal().intValue());
        sequenceIntensityValueFactory.setValue(Double.valueOf(currentProgramSequence.get().getIntensity()));
      }
    });
  }

  @FXML
  protected void launchSequenceSelectorUI() {
    try {
      sequenceSelectorLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceSelectorFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceSelectorController controller = loader.getController();
      controller.setup(programId.get(), (sequenceId) -> currentProgramSequence.set(projectService.getContent().getProgramSequence(sequenceId).orElse(null)));
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      WindowUtils.darkenBackgroundUntilClosed(stage, sequenceSelectorLauncher.getScene(),
        () -> sequenceSelectorLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
      WindowUtils.closeWindowOnClickingAway(stage);
      WindowUtils.setStagePositionBelowParentNode(stage, sequenceSelectorLauncher);
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @FXML
  protected void launchSequenceManagementUI() {
    try {
      sequenceManagementLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceManagementFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceManagementController controller = loader.getController();
      controller.setup(currentProgramSequence.get(), stage);
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      WindowUtils.darkenBackgroundUntilClosed(stage, sequenceManagementLauncher.getScene(),
        () -> sequenceManagementLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
      WindowUtils.closeWindowOnClickingAway(stage);
      WindowUtils.setStagePositionBelowParentNode(stage, sequenceManagementLauncher);
    } catch (IOException e) {
      LOG.error("Error opening Sequence Management window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   binds the disability state of the given node to the provided state(s)
   */
  private void createDisabilityBindingForTypes(Node node, List<ProgramType> types) {
    BooleanBinding anyTypeMatched = Bindings.createBooleanBinding(() ->
        types.stream().noneMatch(type -> type.equals(typeChooser.getValue())),
      typeChooser.valueProperty());
    node.disableProperty().bind(anyTypeMatched);
  }

  /**
   binds the visibility state of the given node to the provided state(s)
   */
  private void createVisibilityBindingForTypes(Node node, List<ProgramType> types) {
    BooleanBinding anyTypeMatched = Bindings.createBooleanBinding(() ->
        types.stream().noneMatch(type -> type.equals(typeChooser.getValue())),
      typeChooser.valueProperty());
    node.visibleProperty().bind(anyTypeMatched);
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
    // FUTURE: on stage close
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
    try {
      configButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(configFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      ProgramConfigController configController = loader.getController();
      configController.setup(stage, programId.get());
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      WindowUtils.darkenBackgroundUntilClosed(stage, configButton.getScene(),
        () -> configButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
    } catch (IOException e) {
      LOG.error("Error loading EditConfig window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Update the Program Editor with the current Program.
   */
  private void setupProgram() {
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
    programSequenceObservableList.setAll(programSequences);
    if (!programSequences.isEmpty()) {
      currentProgramSequence.set(programSequences.get(0));
    } else {
      LOG.info("Program has no sequence");
    }

    setupProgramMemeContainer();
    setupSequenceBindingView();
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
   Set up the Sequence Binding View
   */
  private void setupSequenceBindingView() {
    Collection<ProgramSequenceBinding> bindings = projectService.getContent().getSequenceBindingsOfProgram(programId.get());

    // clear first before adding to prevent duplicates -- teardown controllers first
    for (SequenceBindingColumnController controller : sequenceBindingColumnControllers) controller.teardown();
    sequenceBindingColumnControllers.clear();
    sequenceBindingsContainer.getChildren().clear();

    // find the highest offset in the current sequenceBindingsOfProgram group and create the offset holders  with an extra button
    // if sequence bindings number is zero, add the two buttons that appear when empty
    int highestOffset = bindings.stream().map(ProgramSequenceBinding::getOffset).max(Integer::compareTo).orElse(-1);
    for (int i = 0; i <= highestOffset + 1; i++) {
      addSequenceBindingColumn(i);
    }
  }

  protected void addSequenceBindingColumn(int offset) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceBindingColumnFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      sequenceBindingsContainer.getChildren().add(root);
      SequenceBindingColumnController controller = loader.getController();
      sequenceBindingColumnControllers.add(controller);
      controller.setup(offset, programId.get());
    } catch (IOException e) {
      LOG.error("Error loading Sequence Selector view!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   @return the current program ID
   */
  public UUID getProgramId() {
    return programId.get();
  }

  /**
   Set the current sequence id

   @param sequenceId to set
   */
  public void setSequenceId(UUID sequenceId) {
    this.sequenceId.set(sequenceId);
  }

  /**
   Set the current sequence total

   @param sequenceTotal to set
   */
  public void setSequenceTotal(Integer sequenceTotal) {
    sequenceTotalValueFactory.setValue(sequenceTotal);
  }
}
