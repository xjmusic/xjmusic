// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 Modal to Create/Clone/Move/Delete (CcMD) an Entity.
 */
@Service
public class TemplateAddBindingModalController extends ProjectModalController {
  private static final String WINDOW_TITLE = "Add Template Binding";
  private final ObjectProperty<Library> library = new SimpleObjectProperty<>();
  private final ObjectProperty<Program> program = new SimpleObjectProperty<>();
  private final ObjectProperty<Instrument> instrument = new SimpleObjectProperty<>();
  private final ObjectProperty<UUID> templateId = new SimpleObjectProperty<>();
  private final ObjectProperty<ContentBindingType> libraryContentType = new SimpleObjectProperty<>(ContentBindingType.Library);

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


  public TemplateAddBindingModalController(
    @Value("classpath:/views/template/template-add-binding-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    libraryContentSelectionContainer.visibleProperty().bind(library.isNotNull());
    libraryContentSelectionContainer.managedProperty().bind(library.isNotNull());
    var programsVisible = library.isNotNull().and(libraryContentType.isEqualTo(ContentBindingType.Program));
    var instrumentsVisible = library.isNotNull().and(libraryContentType.isEqualTo(ContentBindingType.Instrument));
    programChoiceContainer.visibleProperty().bind(programsVisible);
    programChoiceContainer.managedProperty().bind(programsVisible);
    instrumentChoiceContainer.visibleProperty().bind(instrumentsVisible);
    instrumentChoiceContainer.managedProperty().bind(instrumentsVisible);

    choiceLibrary.setItems(FXCollections.observableList(projectService.getLibraries().stream().sorted(Comparator.comparing(Library::getName)).map(LibraryChoice::new).toList()));

    choiceLibrary.setOnAction(event -> {
      library.set(choiceLibrary.getValue().library());

      var programs = projectService.getContent().getProgramsOfLibrary(library.get().getId()).stream().sorted(Comparator.comparing(Program::getName)).map(ProgramChoice::new).toList();
      choiceProgram.setItems(FXCollections.observableList(programs));
      buttonLibraryContentPrograms.setDisable(programs.isEmpty());

      var instruments = projectService.getContent().getInstrumentsOfLibrary(library.get().getId()).stream().sorted(Comparator.comparing(Instrument::getName)).map(InstrumentChoice::new).toList();
      choiceInstrument.setItems(FXCollections.observableList(instruments));
      buttonLibraryContentInstruments.setDisable(instruments.isEmpty());

      program.set(null);
      choiceProgram.setValue(null);
      instrument.set(null);
      choiceInstrument.setValue(null);
      libraryContentType.set(ContentBindingType.Library);
      libraryContentSelectionToggle.selectToggle(null);
    });

    choiceProgram.setOnAction(event -> program.set(Objects.nonNull(choiceProgram.getValue()) ? choiceProgram.getValue().program():null));
    choiceInstrument.setOnAction(event -> instrument.set(Objects.nonNull(choiceInstrument.getValue()) ? choiceInstrument.getValue().instrument():null));

    libraryContentSelectionToggle.selectedToggleProperty().addListener((o, ov, v) -> {
      if (Objects.equals(v, buttonLibraryContentPrograms)) {
        Platform.runLater(() -> libraryContentType.set(ContentBindingType.Program));
      } else if (Objects.equals(v, buttonLibraryContentInstruments)) {
        Platform.runLater(() -> libraryContentType.set(ContentBindingType.Instrument));
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
    if (Objects.nonNull(library.get())) Platform.runLater(() -> {
      switch (libraryContentType.get()) {
        case Library -> projectService.createTemplateBinding(
          templateId.get(),
          ContentBindingType.Library,
          library.get().getId());
        case Program -> projectService.createTemplateBinding(
          templateId.get(),
          ContentBindingType.Program,
          program.get().getId());
        case Instrument -> projectService.createTemplateBinding(
          templateId.get(),
          ContentBindingType.Instrument,
          instrument.get().getId());
      }
    });
    Stage stage = (Stage) buttonOK.getScene().getWindow();
    stage.close();
    onStageClose();
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
   This class is used to display the library name in the ChoiceBox while preserving the underlying ID
   */
  public record LibraryChoice(Library library) {
    @Override
    public String toString() {
      return Objects.nonNull(library) ? library.getName():"Select...";
    }
  }

  /**
   This class is used to display the program name in the ChoiceBox while preserving the underlying ID
   */
  public record ProgramChoice(Program program) {
    @Override
    public String toString() {
      return Objects.nonNull(program) ? program.getName():"Select...";
    }
  }

  /**
   This class is used to display the instrument name in the ChoiceBox while preserving the underlying ID
   */
  public record InstrumentChoice(Instrument instrument) {
    @Override
    public String toString() {
      return Objects.nonNull(instrument) ? instrument.getName():"Select...";
    }
  }
}
