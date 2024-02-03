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
import io.xj.hub.tables.InstrumentMeme;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
  public ToggleButton editButton;
  @FXML
  public ToggleButton bindButton;
  @FXML
  public Button configButton;
  @FXML
  public HBox memeTagContainer;
  @FXML
  public Button addMemeButton;
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
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>(null);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final StringProperty name = new SimpleStringProperty("");
  private final ObjectProperty<ProgramType> type = new SimpleObjectProperty<>();
  private final ObjectProperty<ProgramState> state = new SimpleObjectProperty<>();
  private final StringProperty key = new SimpleStringProperty("");
  protected final StringProperty config = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0);
  private final FloatProperty intensity = new SimpleFloatProperty(0);
  private final SpinnerValueFactory<Double> tempoValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1300, 0);
  private final SpinnerValueFactory<Double> intensityValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0);
  private final ObjectProperty<Double> tempoDoubleValue = new SimpleObjectProperty<>(tempoValueFactory.getValue());
  private final ObjectProperty<Double> intensityDoubleValue = new SimpleObjectProperty<>(intensityValueFactory.getValue());
  private final ObservableList<ProgramType> programTypes = FXCollections.observableArrayList(ProgramType.values());
  private final ObservableList<ProgramState> programStates = FXCollections.observableArrayList(ProgramState.values());

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
    name.addListener((o, ov, v) -> dirty.set(true));
    intensityChooserSpinner.valueProperty().addListener((o, ov, v) -> dirty.set(true));
    tempoChooserSpinner.valueProperty().addListener((o, ov, v) -> dirty.set(true));
    stateChooser.valueProperty().bindBidirectional(state);
    keyField.textProperty().bindBidirectional(key);
    // Bind the Spinner's value to the ObjectProperty
    intensity.bind(Bindings.createFloatBinding(() -> intensityDoubleValue.get().floatValue(), intensityDoubleValue));
    // Update the ObjectProperty when the Spinner value changes
    intensityValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> intensityDoubleValue.set(newValue));
    intensityChooserSpinner.setValueFactory(intensityValueFactory);
    // Bind the Spinner's value to the ObjectProperty
    tempo.bind(Bindings.createFloatBinding(() -> tempoDoubleValue.get().floatValue(), tempoDoubleValue));
    // Update the ObjectProperty when the Spinner value changes
    tempoChooserSpinner.valueProperty().addListener((observable, oldValue, newValue) -> tempoDoubleValue.set(newValue));
    tempoChooserSpinner.setValueFactory(tempoValueFactory);

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.ProgramEditor))
        setup();
    });
    // Add a listener to bindButton to deselect editButton
    bindButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        editButton.setSelected(!bindButton.isSelected());
      }
    });

    // Add a listener to editButton to deselect bindButton
    editButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        bindButton.setSelected(!editButton.isSelected());
      }
    });
    typeChooser.setItems(programTypes);
    stateChooser.setItems(programStates);
  }

  @FXML
  private void addMemeTag() {
    ProgramMeme programMeme = new ProgramMeme(UUID.randomUUID(), "XXX", this.programId.getValue());
    loadMemeTag(programMeme);
    try {
      projectService.getContent().put(programMeme);
    } catch (Exception e) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(e),e);
    }
  }

  protected void loadMemeTag(ProgramMeme programMeme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      Parent root = loader.load();
      MemeTagController memeTagController = loader.getController();
      memeTagController.memeTagInitializer(this, root,projectService, programMeme,programId.get());
      memeTagContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  @FXML
  protected void openCloneDialog() {
    try {
      Optional<Program> programOptional = projectService.getContent().getProgram(programId.get());
      if (programOptional.isEmpty())
        return;
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(cloneFxml.getURL());
      Parent root = loader.load();
      CloneMenuController cloneMenuController = loader.getController();
      cloneMenuController.cloneProgramInitializer(programOptional.get(), projectService, stage);
      stage.setScene(new Scene(root));
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

  @FXML
  protected void handlePressSave() {
    var program = projectService.getContent().getProgram(programId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    program.setName(name.get());
    program.setKey(key.get());
    program.setIntensity(intensity.get());
    program.setTempo(tempo.get());
    if (projectService.updateProgram(program))
      uiStateService.viewLibrary(program.getLibraryId());
  }

  @FXML
  protected void handleEditConfig() {
    try {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(configFxml.getURL());
      Parent root = loader.load();
      ProgramConfigController configController = loader.getController();
      configController.programConfigInitializer(this, stage);
      stage.setScene(new Scene(root));
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
  }
}
