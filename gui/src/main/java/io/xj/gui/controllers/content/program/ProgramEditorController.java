// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
import java.util.*;

@Service
public class ProgramEditorController extends ProjectController {
  @FXML
  public Spinner<Double> intensityChooserSpinner;
  @FXML
  public Spinner<Double> tempoChooserSpinner;
  @FXML
  public TextField keyField;
  @FXML
  public ComboBox<ProgramState> stateChooser;
  @FXML
  public ComboBox<ProgramType> typeChooser;
  @FXML
  public TextField programNameField;
  @FXML
  public Button copyButton;
  @FXML
  public Button editButton;
  @FXML
  public Button bindButton;
  @FXML
  public Button configButton;
  @FXML
  public HBox memeTagContainer;
  @FXML
  public Button addMemeButton;
  @FXML
  public Label sequenceIntensityLabel;
  @FXML
  public Spinner<Double> sequenceIntensitySpinner;
  @FXML
  public TextField sequenceKey;
  @FXML
  public Label sequenceTotalLabel;
  @FXML
  public Spinner<Integer> sequenceTotalSpinner;
  @FXML
  public TextField sequenceName;
  @FXML
  public Button sequenceMenuLauncher;
  @FXML
  public ToggleButton snapButton;
  @FXML
  public ComboBox<String> zoomChooser;
  @FXML
  public ComboBox<String> gridChooser;
  @FXML
  public Label gridLabel;
  @FXML
  public ToggleButton sequenceToggle;
  @FXML
  protected VBox container;
  @FXML
  protected TextField fieldName;

  @Value("classpath:/views/content/program/program-config.fxml")
  private Resource configFxml;

  @Value("classpath:/views/content/program/meme-tag.fxml")
  private Resource memeTagFxml;

  @Value("classpath:/views/content/program/clone-menu.fxml")
  private Resource cloneFxml;

  @Value("classpath:/views/content/program/search-sequence.fxml")
  private Resource searchSequenceFxml;

  @Value("classpath:/views/content/program/sequence-menu.fxml")
  private Resource sequenceManagementFxml;
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>(null);
  private final ObjectProperty<UUID> sequenceId = new SimpleObjectProperty<>(null);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final StringProperty name = new SimpleStringProperty("");
  private final ObjectProperty<ProgramType> type = new SimpleObjectProperty<>();
  private final ObjectProperty<ProgramState> state = new SimpleObjectProperty<>();
  private final StringProperty key = new SimpleStringProperty("");
  protected final StringProperty config = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0);
  private final SpinnerValueFactory<Double> tempoValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1300, 0);
  private final FloatProperty intensity = new SimpleFloatProperty(0);
  private final SpinnerValueFactory<Double> intensityValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0, 0.1);

  private final ObjectProperty<Double> intensityDoubleValue = new SimpleObjectProperty<>(intensityValueFactory.getValue());

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
  private final SimpleStringProperty sequencePropertyName = new SimpleStringProperty("");
  private final SimpleStringProperty sequencePropertyKey = new SimpleStringProperty("");


  public ProgramEditorController(
    @Value("classpath:/views/content/library-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);
    programNameField.textProperty().bindBidirectional(name);
    typeChooser.valueProperty().bindBidirectional(type);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);
    sequenceName.textProperty().bindBidirectional(sequencePropertyName);
    sequenceKey.textProperty().bindBidirectional(sequencePropertyKey);
    sequenceTotalLabel.textProperty().bind(sequenceTotalSpinner.valueProperty().asString());
    // Bind Label text to Spinner value with formatting
    sequenceIntensityLabel.textProperty().bind(Bindings.createStringBinding(() ->
      String.format("%.1f", sequenceIntensityDoubleValue.get()), sequenceIntensityDoubleValue));
    stateChooser.valueProperty().bindBidirectional(state);
    keyField.textProperty().bindBidirectional(key);
    sequenceKey.textProperty().bindBidirectional(sequencePropertyKey);
    sequenceName.textProperty().bindBidirectional(sequencePropertyName);
    intensityChooserSpinner.setValueFactory(intensityValueFactory);

    // Bind the Spinner's value to the ObjectProperty(intensity)
    intensity.bind(Bindings.createFloatBinding(() -> intensityDoubleValue.get().floatValue(), intensityDoubleValue));
    intensityValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> intensityDoubleValue.set(newValue));

    // Update the ObjectProperty when the Spinner value changes(sequenceIntensity)
    sequenceIntensity.bind(Bindings.createFloatBinding(() -> sequenceIntensityDoubleValue.get().floatValue(), sequenceIntensityDoubleValue));
    sequenceIntensityValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> sequenceIntensityDoubleValue.set(newValue));
    sequenceIntensitySpinner.setValueFactory(sequenceIntensityValueFactory);

    // Update the ObjectProperty when the Spinner value changes(sequenceTotal)
    sequenceTotal.bind(Bindings.createIntegerBinding(sequenceTotalIntegerValue::get, sequenceTotalIntegerValue));
    sequenceTotalValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> sequenceTotalIntegerValue.set(newValue));
    sequenceTotalSpinner.setValueFactory(sequenceTotalValueFactory);

    // Bind the Spinner's value to the ObjectProperty
    tempo.bind(Bindings.createFloatBinding(() -> tempoDoubleValue.get().floatValue(), tempoDoubleValue));
    // Update the ObjectProperty when the Spinner value changes
    tempoChooserSpinner.valueProperty().addListener((observable, oldValue, newValue) -> tempoDoubleValue.set(newValue));
    tempoChooserSpinner.setValueFactory(tempoValueFactory);
    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor))
        setup();
    });

    bindButton.setOnAction(event -> {
      editButton.getStyleClass().remove("selected");
      bindButton.getStyleClass().add("selected");
    });

    editButton.setOnAction(event -> {
      bindButton.getStyleClass().remove("selected");
      editButton.getStyleClass().add("selected");
    });

    snapButton.getStyleClass().add("snap-button");
    createDisabilityBindingForTypes(editButton, Arrays.asList(ProgramType.Main, ProgramType.Macro));
    createDisabilityBindingForTypes(bindButton, Arrays.asList(ProgramType.Main, ProgramType.Macro));
    typeChooser.setItems(programTypes);
    stateChooser.setItems(programStates);
    setTextProcessing(programNameField);
    setTextProcessing(keyField);
    setSpinnerSelectionProcessing(tempoChooserSpinner);
    setSpinnerSelectionProcessing(intensityChooserSpinner);
    setComboboxSelectionProcessing(typeChooser);
    setComboboxSelectionProcessing(stateChooser);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);
    gridChooser.setItems(gridDivisions);
    gridChooser.setValue("1/4");
    zoomChooser.setItems(zoomOptions);
    zoomChooser.setValue("25%");
    sequenceToggle.setOnMouseClicked(this::showSequenceUI);
    sequenceMenuLauncher.setOnMouseClicked(this::showSequenceManagementUI);
    sequenceIntensitySpinner.setVisible(false);
    sequenceTotalSpinner.setVisible(false);
    createDisabilityBindingForTypes(snapButton, Arrays.asList(ProgramType.Beat, ProgramType.Detail));
    toggleVisibilityBetweenEditorAndLabel(sequenceIntensitySpinner, sequenceIntensityLabel);
    toggleVisibilityBetweenEditorAndLabel(sequenceTotalSpinner, sequenceTotalLabel);
  }


  protected void showSequenceUI(javafx.scene.input.MouseEvent event) {
    try {
      var sequences = projectService.getContent().getSequencesOfProgram(programId.get());
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(searchSequenceFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SearchSequence searchSequence = loader.getController();
      searchSequence.setUp(sequences);
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      positionUIAtLocation(stage, event, 400, 28);
      closeWindowOnClickingAway(stage);
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }


  protected void showSequenceManagementUI(javafx.scene.input.MouseEvent event) {
    try {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceManagementFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceManagement sequenceManagement = loader.getController();
      sequenceManagement.setUp(sequenceId, stage);
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      positionUIAtLocation(stage, event, 450, 29);
      closeWindowOnClickingAway(stage);
    } catch (IOException e) {
      LOG.error("Error opening Sequence Management window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  private void toggleVisibilityBetweenEditorAndLabel(Spinner<?> spinner, Label label) {
    label.setOnMouseClicked(e -> {
      label.setVisible(false);
      spinner.setVisible(true);
      //shift focus to the nameField
      spinner.requestFocus();
    });
    // Add a focus listener to the TextField
    spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        spinner.setVisible(false);
        label.setVisible(true);
      }
    });
  }

  /**
   * Positions the GUI to the place where the click happened
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
   * binds the disability state of the given node to the provided state(s)
   */
  private void createDisabilityBindingForTypes(Node node, List<ProgramType> types) {
    BooleanBinding anyTypeMatched = Bindings.createBooleanBinding(() ->
        types.stream().noneMatch(type -> type.equals(typeChooser.getValue())),
      typeChooser.valueProperty());
    node.disableProperty().bind(anyTypeMatched);
  }


  @FXML
  private void addMemeTag() {
    ProgramMeme programMeme = new ProgramMeme(UUID.randomUUID(), "XXX", this.programId.getValue());
    loadMemeTag(programMeme);
    try {
      projectService.getContent().put(programMeme);
    } catch (Exception e) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }


  /**
   * handles value changes listening in the textfield components
   */
  private void setTextProcessing(TextField textField) {
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   * handles value changes listening in the  value Spinner components
   */
  private void setSpinnerSelectionProcessing(Spinner<?> spinner) {
    spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   * handles value changes listening in the ComboBox components
   */
  private void setComboboxSelectionProcessing(ComboBox<?> comboBox) {
    comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  protected void loadMemeTag(ProgramMeme programMeme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      MemeTagController memeTagController = loader.getController();
      memeTagController.setUp(root, programMeme, programId.get());
      memeTagContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @FXML
  protected void openCloneDialog() {
    try {
      var program = projectService.getContent().getProgram(programId.get())
        .orElseThrow(() -> new RuntimeException("Could not find Program"));
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(cloneFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      CloneMenuController cloneMenuController = loader.getController();
      cloneMenuController.setUp(program, stage);
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
    } catch (IOException e) {
      LOG.error("Error opening clone window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
    System.out.println("closed");
  }

  protected void handleProgramSave() {
    var program = projectService.getContent().getProgram(programId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    program.setName(name.get());
    program.setKey(key.get());
    program.setIntensity(intensity.get());
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
   * Update the Program Editor with the current Program.
   */
  private void setup() {
    if (Objects.isNull(uiStateService.currentProgramProperty().get()))
      return;
    var program = projectService.getContent().getProgram(uiStateService.currentProgramProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    LOG.info("Will edit Program \"{}\"", program.getName());
    this.programId.set(program.getId());
    this.name.set(program.getName());
    this.dirty.set(false);
    this.type.set(program.getType());
    this.state.set(program.getState());
    this.key.set(program.getKey());
    this.tempoValueFactory.setValue(Double.valueOf(program.getTempo()));
    this.intensityValueFactory.setValue(Double.valueOf(program.getIntensity()));
    this.config.set(program.getConfig());
    memeTagContainer.getChildren().clear();
    projectService.getContent().getMemesOfProgram(program.getId()).forEach(this::loadMemeTag);
    setUpSequence();
  }

  private void setUpSequence() {
    Collection<ProgramSequence> programSequences = projectService.getContent().getSequencesOfProgram(programId.get());
    ProgramSequence sequence;
    List<ProgramSequence> sequenceList = new ArrayList<>(programSequences);
    if (!sequenceList.isEmpty()) {
      sequence = sequenceList.get(0);
      this.sequenceId.set(sequence.getId());
      this.sequencePropertyName.set(sequence.getName());
      this.sequencePropertyKey.set(sequence.getKey());
      this.sequenceTotalValueFactory.setValue(sequence.getTotal().intValue());
      this.sequenceIntensityValueFactory.setValue(Double.valueOf(sequence.getIntensity()));
    } else {
      LOG.info("Program has no sequence");
    }
  }

  /**
   * closes the stage when clicking outside it (loses focus)
   */
  public static void closeWindowOnClickingAway(Stage window) {
    window.focusedProperty().addListener((obs, oldValue, newValue) -> {
      if (!newValue) {
        window.close();
      }
    });
  }
}
