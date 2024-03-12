// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectModalController;
import io.xj.gui.modes.CmdMode;
import io.xj.gui.modes.CmdType;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.util.FormatUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 Modal to Create/Clone/Move/Delete (CcMD) an Entity.
 */
@Service
public class CmdModalController extends ProjectModalController {
  private static final Logger LOG = LoggerFactory.getLogger(CmdModalController.class);
  private static final Set<CmdMode> NAME_DISABLED_MODES = Set.of(
    CmdMode.Delete,
    CmdMode.Move
  );
  private final StringProperty windowTitle = new SimpleStringProperty();
  private final ObjectProperty<CmdMode> mode = new SimpleObjectProperty<>();
  private final ObjectProperty<CmdType> type = new SimpleObjectProperty<>();
  private final StringProperty name = new SimpleStringProperty();
  private final ObjectProperty<Library> parentLibrary = new SimpleObjectProperty<>();
  private final ObjectProperty<UUID> currentId = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  @FXML
  TextField fieldName;

  @FXML
  Button buttonOK;

  @FXML
  Button buttonCancel;

  @FXML
  VBox libraryChoiceContainer;

  @FXML
  ChoiceBox<LibraryChoice> choiceLibrary;


  public CmdModalController(
    @Value("classpath:/views/cmd-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    UIStateService uiStateService,
    ThemeService themeService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    var hasLibrary = Bindings.createBooleanBinding(
      () -> switch (type.get()) {
        case Program, Instrument -> true;
        default -> false;
      }, type);
    libraryChoiceContainer.visibleProperty().bind(hasLibrary);
    libraryChoiceContainer.managedProperty().bind(hasLibrary);
    choiceLibrary.setItems(FXCollections.observableList(projectService.getLibraries().stream().sorted(Comparator.comparing(Library::getName)).map(LibraryChoice::new).toList()));
    if (parentLibrary.isNotNull().get())
      choiceLibrary.setValue(new LibraryChoice(parentLibrary.get()));
    choiceLibrary.setOnAction(event -> parentLibrary.set(choiceLibrary.getValue().library()));
    fieldName.textProperty().bindBidirectional(name);
    fieldName.disableProperty().bind(Bindings.createBooleanBinding(() -> NAME_DISABLED_MODES.contains(mode.get()), mode));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    createAndShowModal(windowTitle.get());
  }

  @FXML
  void handleNameFieldKeyPressed(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      event.consume();
      handlePressOK();
    }
  }

  @FXML
  void handlePressOK() {
    try {
      switch (mode.get()) {
        case Create -> {
          if (StringUtils.isNullOrEmpty(name.getValue())) {
            projectService.showWarningAlert(
              "Can't create project",
              "Name cannot be blank",
              "Please enter a name for the new entity."
            );
            return;
          }
          switch (type.get()) {
            case Template -> {
              var template = projectService.createTemplate(name.getValue());
              uiStateService.editTemplate(template.getId());
            }
            case Library -> {
              projectService.createLibrary(name.getValue());
              uiStateService.viewLibraries();
            }
            case Program -> {
              var program = projectService.createProgram(parentLibrary.get(), name.getValue());
              uiStateService.editProgram(program.getId());
            }
            case Instrument -> {
              var instrument = projectService.createInstrument(parentLibrary.get(), name.getValue());
              uiStateService.editInstrument(instrument.getId());
            }
          }
        }
        case Move -> {
          switch (type.get()) {
            case Program -> {
              var program = projectService.moveProgram(currentId.get(), parentLibrary.get());
              uiStateService.viewLibrary(program.getLibraryId());
            }
            case Instrument -> {
              var instrument = projectService.moveInstrument(currentId.get(), parentLibrary.get());
              uiStateService.viewLibrary(instrument.getLibraryId());
            }
          }
        }
        case Clone -> {
          switch (type.get()) {
            case Template -> {
              var template = projectService.cloneTemplate(currentId.get(), name.getValue());
              uiStateService.editTemplate(template.getId());
            }
            case Library -> {
              var library = projectService.cloneLibrary(currentId.get(), name.getValue());
              uiStateService.viewLibrary(library.getId());
            }
            case Program -> {
              var program = projectService.cloneProgram(currentId.get(), parentLibrary.get().getId(), name.getValue());
              uiStateService.viewLibrary(program.getLibraryId());
            }
            case Instrument -> {
              var instrument = projectService.cloneInstrument(currentId.get(), parentLibrary.get().getId(), name.getValue());
              uiStateService.viewLibrary(instrument.getLibraryId());
            }
          }
        }
      }
      Stage stage = (Stage) buttonOK.getScene().getWindow();
      stage.close();
      onStageClose();

    } catch (Exception e) {
      LOG.error("Error creating entity! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  @FXML
  void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  /**
   Create a new Template.
   */
  public void createTemplate() {
    name.set("");
    setup(CmdMode.Create, CmdType.Template);
    launchModal();
  }

  /**
   Create a new Library.
   */
  public void createLibrary() {
    name.set("");
    setup(CmdMode.Create, CmdType.Library);
    launchModal();
  }

  /**
   Create a new Program.
   */
  public void createProgram(Library inLibrary) {
    name.set("");
    parentLibrary.set(inLibrary);
    setup(CmdMode.Create, CmdType.Program);
    launchModal();
  }

  /**
   Create a new Instrument.
   */
  public void createInstrument(Library inLibrary) {
    name.set("");
    parentLibrary.set(inLibrary);
    setup(CmdMode.Create, CmdType.Instrument);
    launchModal();
  }

  /**
   Delete a library

   @param library to delete
   */
  public void deleteLibrary(Library library) {
    setup(CmdMode.Delete, CmdType.Library);

    var programs = projectService.getContent().getPrograms().stream().filter(program -> program.getLibraryId().equals(library.getId())).count();
    var instruments = projectService.getContent().getInstruments().stream().filter(instrument -> instrument.getLibraryId().equals(library.getId())).count();
    if (programs > 0 || instruments > 0) {
      showWarningDialog("Cannot delete Library", "Library contains content",
        String.format("Cannot delete Library \"%s\" because it contains %s", library.getName(),
          FormatUtils.describeCounts(Map.of(
            "Program", (int) programs,
            "Instrument", (int) instruments
          ))));
      return;
    }

    var bindings = projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(ContentBindingType.Library, binding.getType()) && Objects.equals(binding.getTargetId(), library.getId()))
      .count();
    if (bindings > 0) {
      showWarningDialog("Cannot delete Library", "Library is bound",
        String.format("Cannot delete Library \"%s\" because it is bound to %s",
          library.getName(), FormatUtils.describeCount("Template", bindings)));
      return;
    }

    if (!projectService.showConfirmationDialog("Delete Library?", "This action cannot be undone.", String.format("Are you sure you want to delete the Library \"%s\"?", library.getName())))
      return;

    projectService.deleteContent(library);
  }

  /**
   Clone a library

   @param library to clone
   */
  public void cloneLibrary(Library library) {
    setup(CmdMode.Clone, CmdType.Library);
    currentId.set(library.getId());
    name.set(String.format("Copy of %s", library.getName()));
    launchModal();
  }

  /**
   Move a program

   @param program to move
   */
  public void moveProgram(Program program) {
    setup(CmdMode.Move, CmdType.Program);
    currentId.set(program.getId());
    name.set(program.getName());
    parentLibrary.set(projectService.getContent().getLibrary(program.getLibraryId()).orElseThrow(() -> new RuntimeException("Could not find Library")));
    launchModal();
  }

  /**
   Clone a program

   @param program to clone
   */
  public void cloneProgram(Program program) {
    setup(CmdMode.Clone, CmdType.Program);
    currentId.set(program.getId());
    name.set(String.format("Copy of %s", program.getName()));
    parentLibrary.set(projectService.getContent().getLibrary(program.getLibraryId()).orElseThrow(() -> new RuntimeException("Could not find Library")));
    launchModal();
  }

  /**
   Delete a program

   @param program to delete
   */
  public void deleteProgram(Program program) {
    setup(CmdMode.Delete, CmdType.Program);

    var bindings = projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(ContentBindingType.Program, binding.getType()) && Objects.equals(binding.getTargetId(), program.getId()))
      .count();
    if (bindings > 0) {
      showWarningDialog("Cannot delete Program", "Program is bound",
        String.format("Cannot delete Program \"%s\" because it is bound to %s",
          program.getName(), FormatUtils.describeCount("Template", bindings)));
      return;
    }

    if (!projectService.showConfirmationDialog("Delete Program?", "This action cannot be undone.", String.format("Are you sure you want to delete the Program \"%s\"?", program.getName())))
      return;

    projectService.deleteContent(program);
  }

  /**
   Move an instrument

   @param instrument to move
   */
  public void moveInstrument(Instrument instrument) {
    setup(CmdMode.Move, CmdType.Instrument);
    currentId.set(instrument.getId());
    name.set(instrument.getName());
    parentLibrary.set(projectService.getContent().getLibrary(instrument.getLibraryId()).orElseThrow(() -> new RuntimeException("Could not find Library")));
    launchModal();
  }

  /**
   Clone an instrument

   @param instrument to clone
   */
  public void cloneInstrument(Instrument instrument) {
    setup(CmdMode.Clone, CmdType.Instrument);
    currentId.set(instrument.getId());
    name.set(String.format("Copy of %s", instrument.getName()));
    parentLibrary.set(projectService.getContent().getLibrary(instrument.getLibraryId()).orElseThrow(() -> new RuntimeException("Could not find Library")));
    launchModal();
  }

  /**
   Delete an instrument

   @param instrument to delete
   */
  public void deleteInstrument(Instrument instrument) {
    setup(CmdMode.Delete, CmdType.Instrument);

    var bindings = projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(ContentBindingType.Instrument, binding.getType()) && Objects.equals(binding.getTargetId(), instrument.getId()))
      .count();
    if (bindings > 0) {
      showWarningDialog("Cannot delete Instrument", "Instrument is bound",
        String.format("Cannot delete Instrument \"%s\" because it is bound to %s",
          instrument.getName(), FormatUtils.describeCount("Template", bindings)));
      return;
    }

    if (!projectService.showConfirmationDialog("Delete Instrument?", "This action cannot be undone.", String.format("Are you sure you want to delete the Instrument \"%s\"?", instrument.getName())))
      return;

    projectService.deleteContent(instrument);
  }

  /**
   Clone a template

   @param template to clone
   */
  public void cloneTemplate(Template template) {
    setup(CmdMode.Clone, CmdType.Template);
    currentId.set(template.getId());
    name.set(String.format("Copy of %s", template.getName()));
    launchModal();
  }

  /**
   Delete a template

   @param template to delete
   */
  public void deleteTemplate(Template template) {
    setup(CmdMode.Delete, CmdType.Template);

    var bindings = projectService.getContent().getTemplateBindings().stream().filter(program -> program.getTemplateId().equals(template.getId())).toList();
    var programBindings = bindings.stream().filter(binding -> Objects.equals(ContentBindingType.Program, binding.getType())).count();
    var instrumentBindings = bindings.stream().filter(binding -> Objects.equals(ContentBindingType.Instrument, binding.getType())).count();
    var libraryBindings = bindings.stream().filter(binding -> Objects.equals(ContentBindingType.Library, binding.getType())).count();

    if (programBindings > 0 || instrumentBindings > 0 || libraryBindings > 0) {
      showWarningDialog("Cannot delete Template", "Template is bound",
        String.format("Cannot delete Template \"%s\" because it is bound to %s", template.getName(),
          FormatUtils.describeCounts(Map.of(
            "Program", (int) programBindings,
            "Instrument", (int) instrumentBindings,
            "Library", (int) libraryBindings
          ))));
      return;
    }

    if (!projectService.showConfirmationDialog("Delete Template?", "This action cannot be undone.", String.format("Are you sure you want to delete the Template \"%s\"?", template.getName())))
      return;

    projectService.deleteContent(template);
  }

  /**
   Set up the modal for the given mode and type.

   @param mode of modal
   @param type of modal
   */
  private void setup(CmdMode mode, CmdType type) {
    this.mode.set(mode);
    this.type.set(type);
    windowTitle.set(String.format("%s %s", StringUtils.toProper(mode.name()), StringUtils.toProper(type.name())));
  }

  /**
   Show a warning dialog.

   @param title   title of dialog
   @param header  header of dialog
   @param content content of dialog
   */
  private void showWarningDialog(String title, String header, String content) {
    // Create a custom dialog
    Dialog<String> dialog = new Dialog<>();
    themeService.setup(dialog);
    dialog.setTitle(title);

    // Set the header and content
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setHeaderText(header);
    dialogPane.setContentText(content);

    // Set button types
    dialogPane.getButtonTypes().addAll(ButtonType.OK);

    // Ensure it's resizable and has a preferred width
    dialogPane.setMinHeight(Region.USE_PREF_SIZE);
    dialogPane.setPrefWidth(400); // You can adjust this value

    // Show the dialog and wait for the user to close it
    dialog.showAndWait();
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
}
