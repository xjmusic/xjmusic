// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.controllers.CmdModalController;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

@Service
public class ProgramEditorController extends ProjectController {
  @FXML
  public Spinner<Double> intensityChooser;
  @FXML
  public Spinner<Double> tempoChooser;
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
  public Spinner<Double> sequenceIntensityChooser;
  @FXML
  public TextField sequenceKey;
  @FXML
  public Label sequenceTotalLabel;
  @FXML
  public Spinner<Integer> sequenceTotalChooser;
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
  public Button sequenceButton;
  @FXML
  protected VBox container;
  @FXML
  protected TextField fieldName;

  @Value("classpath:/views/content/program/program-config.fxml")
  private Resource configFxml;

  @Value("classpath:/views/content/program/meme-tag.fxml")
  private Resource memeTagFxml;

  @Value("classpath:/views/content/program/search-sequence.fxml")
  private Resource searchSequenceFxml;

  @Value("classpath:/views/content/program/sequence-management.fxml")
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
  private final CmdModalController cmdModalController;
  private ProgramSequence programSequence = new ProgramSequence();

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
    sequenceTotalLabel.textProperty().bind(sequenceTotalChooser.valueProperty().asString());
    // Bind Label text to Chooser value with formatting
    sequenceIntensityLabel.textProperty().bind(Bindings.createStringBinding(() ->
      String.format("%.1f", sequenceIntensityDoubleValue.get()), sequenceIntensityDoubleValue));
    stateChooser.valueProperty().bindBidirectional(state);
    keyField.textProperty().bindBidirectional(key);
    intensityChooser.setValueFactory(intensityValueFactory);

    // Bind the Chooser's value to the ObjectProperty(intensity)
    intensity.bind(Bindings.createFloatBinding(() -> intensityDoubleValue.get().floatValue(), intensityDoubleValue));
    intensityValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> intensityDoubleValue.set(newValue));

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
    setChooserSelectionProcessing(tempoChooser);
    setChooserSelectionProcessing(intensityChooser);
    setComboboxSelectionProcessing(typeChooser);
    setComboboxSelectionProcessing(stateChooser);
    gridChooser.valueProperty().bindBidirectional(gridProperty);
    zoomChooser.valueProperty().bindBidirectional(zoomProperty);
    gridChooser.setItems(gridDivisions);
    gridChooser.setValue("1/4");
    zoomChooser.setItems(zoomOptions);
    zoomChooser.setValue("25%");
    sequenceButton.setOnMouseClicked(this::showSequenceUI);
    sequenceMenuLauncher.setOnMouseClicked(this::showSequenceManagementUI);
    sequenceIntensityChooser.setVisible(false);
    sequenceTotalChooser.setVisible(false);
    createDisabilityBindingForTypes(snapButton, Arrays.asList(ProgramType.Beat, ProgramType.Detail));
    toggleVisibilityBetweenEditorAndLabel(sequenceIntensityChooser, sequenceIntensityLabel);
    toggleVisibilityBetweenEditorAndLabel(sequenceTotalChooser, sequenceTotalLabel);
    setTextFieldValueToAlwaysCAPS(keyField);
    setTextFieldValueToAlwaysCAPS(sequenceKey);
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

  protected void showSequenceUI(javafx.scene.input.MouseEvent event) {
    try {
      var sequences = projectService.getContent().getSequencesOfProgram(programId.get());
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(searchSequenceFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SearchSequence searchSequence = loader.getController();
      searchSequence.setUp(sequences, programSequence);
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

  private void toggleVisibilityBetweenEditorAndLabel(Spinner<?> chooser, Label label) {
    label.setOnMouseClicked(e -> {
      label.setVisible(false);
      chooser.setVisible(true);
      //shift focus to the nameField
      chooser.requestFocus();
    });
    // Add a focus listener to the TextField
    chooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        chooser.setVisible(false);
        label.setVisible(true);
      }
    });
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
   handles value changes listening in the textfield components
   */
  private void setTextProcessing(TextField textField) {
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   handles value changes listening in the  value Chooser components
   */
  private void setChooserSelectionProcessing(Spinner<?> chooser) {
    chooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) handleProgramSave();
    });
  }

  /**
   handles value changes listening in the ComboBox components
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
    var program = projectService.getContent().getProgram(programId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    cmdModalController.cloneProgram(program);
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
   Update the Program Editor with the current Program.
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
      programSequence = sequence;
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
   closes the stage when clicking outside it (loses focus)
   */
  public static void closeWindowOnClickingAway(Stage window) {
    window.focusedProperty().addListener((obs, oldValue, newValue) -> {
      if (!newValue) {
        window.close();
      }
    });
  }
}
