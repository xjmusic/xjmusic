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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static io.xj.gui.services.UIStateService.ACTIVE_PSEUDO_CLASS;
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
  public TextField sequenceKey;
  @FXML
  public Spinner<Integer> sequenceTotalChooser;
  @FXML
  public TextField sequenceName;
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
  public HBox bindViewParentContainer;
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

  @Value("classpath:/views/content/program/sequence-search.fxml")
  private Resource searchSequenceFxml;

  @Value("classpath:/views/content/program/sequence-management.fxml")
  private Resource sequenceManagementFxml;

  @Value("classpath:/views/content/program/sequence-binding-column.fxml")
  private Resource sequenceBindingColumnFxml;

  @Value("classpath:/views/content/program/sequence-binding-item.fxml")
  private Resource sequenceItemBindingFxml;

  @Value("classpath:/views/content/common/entity-memes.fxml")
  private Resource entityMemesFxml;

  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>(null);
  private final ObjectProperty<UUID> sequenceId = new SimpleObjectProperty<>();
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final StringProperty programName = new SimpleStringProperty("");
  private final ObjectProperty<ProgramType> type = new SimpleObjectProperty<>();
  private final ObjectProperty<ProgramState> state = new SimpleObjectProperty<>();
  private final StringProperty key = new SimpleStringProperty("");

  private final StringProperty config = new SimpleStringProperty("");
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

  public ProgramEditorController(
      @Value("classpath:/views/content/library-editor.fxml") Resource fxml,
      ApplicationContext ac,
      ThemeService themeService,
      ProjectService projectService,
      UIStateService uiStateService,
      CmdModalController cmdModalController) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.cmdModalController = cmdModalController;
  }

  @Override
  public void onStageReady() {
    bindViewParentContainer.visibleProperty().bind(bindButton.selectedProperty());
    var visible = projectService.isStateReadyProperty()
        .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
        .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor))
        setup();
    });
    editorModeToggleGroup.selectedToggleProperty().addListener((o, ov, value) -> {
      if (value.equals(editButton)) {
        bindButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
        editButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
      } else {
        editButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
        bindButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
      }
    });
    editorModeToggleGroup.selectToggle(editButton);
    snapButton.selectedProperty().addListener((o, ov, value) -> snapButton.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, value));
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
    setTextFieldValueToAlwaysCAPS(keyField);
    setTextFieldValueToAlwaysCAPS(sequenceKey);
    sequenceName.textProperty().bindBidirectional(sequencePropertyName);
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);
    programNameField.textProperty().bindBidirectional(programName);
    typeChooser.valueProperty().bindBidirectional(type);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);

    sequenceKey.textProperty().bindBidirectional(sequencePropertyKey);
    // Bind Label text to Chooser value with formatting
    stateChooser.valueProperty().bindBidirectional(state);
    keyField.textProperty().bind(key);

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

    sequenceName.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        if (!newValue) {
          LOG.info("change " + sequenceName.getText());
          sequencePropertyName.set(sequenceName.getText());
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "name",
              sequencePropertyName.get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    sequenceTotalChooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        if (!newValue) {
          LOG.info("change " + sequenceTotalValueFactory.getValue());
          sequenceTotalValueFactory.setValue(sequenceTotalChooser.getValue());
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "total",
              sequenceTotalValueFactory.getValue());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });

    sequenceKey.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        if (!newValue) {
          LOG.info("change " + sequenceTotalChooser.getValue());
          sequencePropertyKey.set(sequenceKey.getText());
          projectService.update(ProgramSequence.class, currentProgramSequence.get().getId(), "key",
              sequencePropertyKey.get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update program sequence ");
      }
    });
  }

  private void setTextFieldValueToAlwaysCAPS(TextField textField) {
    // Create a UnaryOperator to convert text to uppercase
    UnaryOperator<TextFormatter.Change> filter = change -> {
      String text = change.getText();
      if (text.matches("[a-z]")) {
        change.setText(text.toUpperCase());
      }
      return change;
    };

    // Apply the UnaryOperator to the TextFormatter
    TextFormatter<String> textFormatter = new TextFormatter<>(filter);
    // Set the TextFormatter to the TextField
    textField.setTextFormatter(textFormatter);
  }

  @FXML
  protected void launchSequenceSelectorUI(MouseEvent event) {
    try {
      sequenceSelectorLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(searchSequenceFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceSearchController searchSequence = loader.getController();
      searchSequence.setUp(currentProgramSequence.get());
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      positionUIAtLocation(stage, event, 400, 28);
      WindowUtils.closeWindowOnClickingAway(stage, ()-> sequenceSelectorLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @FXML
  protected void launchSequenceManagementUI(MouseEvent event) {
    try {
      sequenceManagementLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceManagementFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceManagementController sequenceManagement = loader.getController();
      sequenceManagement.setUp(currentProgramSequence.get(), stage);
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      positionUIAtLocation(stage, event, 450, 29);
      WindowUtils.closeWindowOnClickingAway(stage, () -> sequenceManagementLauncher.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
    } catch (IOException e) {
      LOG.error("Error opening Sequence Management window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Positions the GUI to the place where the click happened
   */
  private void positionUIAtLocation(Stage stage, MouseEvent event, int xValue, int yValue) {
    // Get the X and Y coordinates of the button
    Node source = (Node) event.getSource();
    double xOffset = source.getLayoutX() + source.localToScreen(0, 0).getX() - xValue;
    double yOffset = source.getLayoutY() + source.localToScreen(0, 0).getY() + yValue;
    // Set the stage's position
    stage.setX(xOffset);
    stage.setY(yOffset);
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
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(configFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      ProgramConfigController configController = loader.getController();
      configController.setUp(stage);
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
    } catch (IOException e) {
      LOG.error("Error loading EditConfig window!\n{}", StringUtils.formatStackTrace(e), e);
    }
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
    this.dirty.set(false);
    this.type.set(program.getType());
    this.state.set(program.getState());
    this.key.set(program.getKey());
    this.tempoValueFactory.setValue(Double.valueOf(program.getTempo()));
    this.config.set(program.getConfig());
    setupProgramMemeContainer();
    setupSequence();
    loadBindingView();
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
          () -> projectService.getContent().getMemesOfProgram(programId.get()),
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

  private void setupSequence() {
    Collection<ProgramSequence> programSequences = projectService.getContent().getSequencesOfProgram(programId.get());
    List<ProgramSequence> sequenceList = new ArrayList<>(programSequences);
    programSequenceObservableList.clear();
    programSequenceObservableList.addAll(sequenceList);
    if (!sequenceList.isEmpty()) {
      currentProgramSequence.set(sequenceList.get(0));
      this.sequenceId.set(currentProgramSequence.get().getId());
      this.sequencePropertyName.set(currentProgramSequence.get().getName());
      this.sequencePropertyKey.set(currentProgramSequence.get().getKey());
      this.sequenceTotalValueFactory.setValue(currentProgramSequence.get().getTotal().intValue());
      this.sequenceIntensityValueFactory.setValue(Double.valueOf(currentProgramSequence.get().getIntensity()));
    } else {
      LOG.info("Program has no sequence");
    }
  }

  private void loadBindingView() {
    bindButton.getStyleClass().add("selected");
    Collection<ProgramSequenceBinding> programSequenceBindingCollection = projectService.getContent().getSequenceBindingsOfProgram(programId.get());
    List<ProgramSequenceBinding> sequenceBindingsOfProgram = new ArrayList<>(programSequenceBindingCollection);
    //clear first before adding to prevent duplicates
    bindViewParentContainer.getChildren().remove(1, bindViewParentContainer.getChildren().size());
    //if sequence bindings number is zero, add the two buttons that appear when empty
    if (sequenceBindingsOfProgram.isEmpty()) {
      addSequenceBindingColumn(bindViewParentContainer.getChildren().size());
    } else {
      //find the highest offset in the current sequenceBindingsOfProgram group and create the offset holders  with an extra button
      for (int i = 1; i <= findHighestOffset(sequenceBindingsOfProgram) + 1; i++) {
        addSequenceBindingColumn(i);
      }
      //populate the sequence binding items on the respective items
      sequenceBindingsOfProgram.forEach(sequenceBindingOfProgram -> {
        int offset = sequenceBindingOfProgram.getOffset();
        addSequenceItem(sequenceBindingOfProgram, ((VBox) bindViewParentContainer.getChildren().get(offset + 1)), offset + 1);
      });
    }
  }

  public int findHighestOffset(List<ProgramSequenceBinding> programSequenceBindingList) {
    if (programSequenceBindingList == null || programSequenceBindingList.isEmpty()) {
      return 0; // Return the minimum value if the list is empty or null
    }

    int highestOffset = 0;

    for (ProgramSequenceBinding obj : programSequenceBindingList) {
      int offset = obj.getOffset();
      if (offset > highestOffset) {
        highestOffset = offset;
      }
    }
    return highestOffset;
  }

  public void addSequenceItem(ProgramSequenceBinding programSequenceBinding, VBox sequenceSelector, int position) {
    try {
      Optional<ProgramSequence> programSequence = projectService.getContent().getProgramSequence(programSequenceBinding.getProgramSequenceId());
      if (programSequence.isEmpty()) {
        return;
      }
      createProgramSequenceBindingItem(programSequenceBinding, sequenceSelector, position, sequenceItemBindingFxml, ac, bindViewParentContainer, projectService);
      checkIfNextItemIsPresent(position);
    } catch (Exception e) {
      LOG.error("Error creating new Sequence \n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  static void createProgramSequenceBindingItem(ProgramSequenceBinding programSequenceBinding, VBox sequenceSelector, int position, Resource sequenceItemBindingFxml, ApplicationContext ac, HBox bindViewParentContainer, ProjectService projectService) throws Exception {
    FXMLLoader loader = new FXMLLoader(sequenceItemBindingFxml.getURL());
    loader.setControllerFactory(ac::getBean);
    Parent root = loader.load();
    HBox.setHgrow(sequenceSelector, Priority.ALWAYS);
    SequenceBindingItemController sequenceBindingItemController = loader.getController();
    sequenceBindingItemController.setup(sequenceSelector, root, bindViewParentContainer, position, programSequenceBinding);
    sequenceSelector.getChildren().add(sequenceSelector.getChildren().size() - 1, root);
    HBox.setHgrow(root, Priority.ALWAYS);
    projectService.getContent().put(programSequenceBinding);
  }

  private void checkIfNextItemIsPresent(int position) {
    if (bindViewParentContainer.getChildren().size() - 1 < position + 1) {
      addSequenceBindingColumn(position + 1);
    }
  }

  protected void addSequenceBindingColumn(int position) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceBindingColumnFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      bindViewParentContainer.getChildren().add(position, root);
      SequenceBindingColumnController sequenceSelector = loader.getController();
      sequenceSelector.setUp(bindViewParentContainer, position, programId.get());
      HBox.setHgrow(root, Priority.ALWAYS);
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
   Set the value of the program config and update the internal store

   @param config value to set
   */
  public void setConfig(String config) {
    this.config.set(config);
  }

  /**
   @return current value of program config
   */
  public String getConfig() {
    return this.config.get();
  }

  /**
   Set the current sequence total

   @param sequenceTotal to set
   */
  public void setSequenceTotal(Integer sequenceTotal) {
    sequenceTotalValueFactory.setValue(sequenceTotal);
  }
}
