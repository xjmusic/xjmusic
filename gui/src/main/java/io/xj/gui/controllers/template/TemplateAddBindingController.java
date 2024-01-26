// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.ReadyAfterBootModalController;
import io.xj.gui.modes.CmdMode;
import io.xj.gui.modes.CmdType;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 Modal to Create/Clone/Move/Delete (CcMD) an Entity.
 */
@Service
public class TemplateAddBindingController extends ReadyAfterBootModalController {
  private static final Logger LOG = LoggerFactory.getLogger(TemplateAddBindingController.class);
  private static final String WINDOW_TITLE = "Add Template Binding";
  private final ObjectProperty<Library> library = new SimpleObjectProperty<>();
  private final ObjectProperty<Program> program = new SimpleObjectProperty<>();
  private final ObjectProperty<Instrument> instrument = new SimpleObjectProperty<>();
  private final ObjectProperty<UUID> templateId = new SimpleObjectProperty<>();
  private final ObjectProperty<LibraryContentType> libraryContentType = new SimpleObjectProperty<>(LibraryContentType.Program);
  private final UIStateService uiStateService;
  private final ProjectService projectService;

  @FXML
  protected VBox container;

  @FXML
  protected Button buttonOK;

  @FXML
  protected Button buttonCancel;

  @FXML
  protected VBox libraryChoiceContainer;

  @FXML
  protected HBox libraryContentSelectionContainer;

  @FXML
  protected ChoiceBox<LibraryChoice> choiceLibrary;

  @FXML
  protected VBox programChoiceContainer;

  @FXML
  protected ChoiceBox<ProgramChoice> choiceProgram;

  @FXML
  protected VBox instrumentChoiceContainer;

  @FXML
  protected ChoiceBox<InstrumentChoice> choiceInstrument;

  @FXML
  protected ToggleGroup libraryContentSelectionToggle;

  @FXML
  protected ToggleButton buttonLibraryContentPrograms;

  @FXML
  protected ToggleButton buttonLibraryContentInstruments;


  public TemplateAddBindingController(
    @Value("classpath:/views/template/template-add-binding.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    UIStateService uiStateService,
    ThemeService themeService,
    ProjectService projectService
  ) {
    super(ac, themeService, fxml);
    this.uiStateService = uiStateService;
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    libraryContentSelectionContainer.visibleProperty().bind(library.isNotNull());
    libraryContentSelectionContainer.managedProperty().bind(library.isNotNull());
    programChoiceContainer.visibleProperty().bind(library.isNotNull().and(libraryContentType.isEqualTo(LibraryContentType.Program)));
    programChoiceContainer.managedProperty().bind(library.isNotNull().and(libraryContentType.isEqualTo(LibraryContentType.Program)));
    instrumentChoiceContainer.visibleProperty().bind(library.isNotNull().and(libraryContentType.isEqualTo(LibraryContentType.Instrument)));
    instrumentChoiceContainer.managedProperty().bind(library.isNotNull().and(libraryContentType.isEqualTo(LibraryContentType.Instrument)));

    choiceLibrary.setItems(FXCollections.observableList(projectService.getLibraries().stream().sorted(Comparator.comparing(Library::getName)).map(LibraryChoice::new).toList()));

    choiceLibrary.setOnAction(event -> {
      library.set(choiceLibrary.getValue().library());
      // TODO scan library name to see it if says programs or instruments
      choiceProgram.setItems(FXCollections.observableList(projectService.getContent().getProgramsOfLibrary(library.get().getId()).stream().sorted(Comparator.comparing(Program::getName)).map(ProgramChoice::new).toList()));
      choiceInstrument.setItems(FXCollections.observableList(projectService.getContent().getInstrumentsOfLibrary(library.get().getId()).stream().sorted(Comparator.comparing(Instrument::getName)).map(InstrumentChoice::new).toList()));
    });
    choiceProgram.setOnAction(event -> program.set(choiceProgram.getValue().program()));
    choiceInstrument.setOnAction(event -> instrument.set(choiceInstrument.getValue().instrument()));

    libraryContentSelectionToggle.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, buttonLibraryContentPrograms)) {
        Platform.runLater(() -> libraryContentType.set(LibraryContentType.Program));
      } else if (Objects.equals(v, buttonLibraryContentInstruments)) {
        Platform.runLater(() -> libraryContentType.set(LibraryContentType.Instrument));
      }
    });
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    createAndShowModal(WINDOW_TITLE);
  }

  @FXML
  protected void handlePressOK() {
// TODO bind that shit
  }

  @FXML
  protected void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  /**
   Launch the modal to add a binding to the given template

   @param templateId for which to add binding
   */
  public void addBindingToTemplate(UUID templateId) {
    this.templateId.set(templateId);
    launchModal();
  }

  /**
   Whether to bind a Program or an Instrument
   */
  enum LibraryContentType {
    Program,
    Instrument
  }

  /**
   This class is used to display the library name in the ChoiceBox while preserving the underlying ID
   */
  public record LibraryChoice(Library library) {
    @Override
    public String toString() {
      return Objects.nonNull(library) ? library.getName() : "Select...";
    }
  }

  /**
   This class is used to display the program name in the ChoiceBox while preserving the underlying ID
   */
  public record ProgramChoice(Program program) {
    @Override
    public String toString() {
      return Objects.nonNull(program) ? program.getName() : "Select...";
    }
  }

  /**
   This class is used to display the instrument name in the ChoiceBox while preserving the underlying ID
   */
  public record InstrumentChoice(Instrument instrument) {
    @Override
    public String toString() {
      return Objects.nonNull(instrument) ? instrument.getName() : "Select...";
    }
  }
}
