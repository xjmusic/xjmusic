// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.modes.CmdMode;
import io.xj.gui.modes.CmdType;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

/**
 Modal to Create/Clone/Move/Delete (CcMD) an Entity.
 */
@Service
public class CmdModalController extends ReadyAfterBootModalController {
  static final Logger LOG = LoggerFactory.getLogger(CmdModalController.class);
  private final StringProperty windowTitle = new SimpleStringProperty();
  private final ObjectProperty<CmdMode> mode = new SimpleObjectProperty<>();
  private final ObjectProperty<CmdType> type = new SimpleObjectProperty<>();
  private final ObjectProperty<Library> parentLibrary = new SimpleObjectProperty<>();
  private final ProjectService projectService;

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  @FXML
  protected Button buttonOK;

  @FXML
  protected Button buttonCancel;


  public CmdModalController(
    @Value("classpath:/views/cmd-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService
  ) {
    super(ac, themeService, fxml);
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    // no op
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
  protected void handlePressOK() {
    switch (mode.get()) {
      case Create:
        if (StringUtils.isNullOrEmpty(fieldName.getText())) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText("Name cannot be blank");
          alert.setContentText("Please enter a name for the new entity.");
          alert.showAndWait();
          return;
        }
        switch (type.get()) {
          case Template:
            projectService.createTemplate(fieldName.getText());
            break;
          case Library:
            projectService.createLibrary(fieldName.getText());
            break;
          case Program:
            projectService.createProgram(parentLibrary.get(), fieldName.getText());
            break;
          case Instrument:
            projectService.createInstrument(parentLibrary.get(), fieldName.getText());
            break;
        }
        break;
      case Clone:
        switch (type.get()) {
          case Template:
            // TODO
            break;
          case Library:
            // TODO
            break;
          case Program:
            // TODO
            break;
          case Instrument:
            // TODO
            break;
        }
        break;
      case Move:
        switch (type.get()) {
          case Program:
            // TODO
            break;
          case Instrument:
            // TODO
            break;
        }
        break;
    }

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
   Create a new Template.
   */
  public void createTemplate() {
    setup(CmdMode.Create, CmdType.Template);
    launchModal();
  }

  /**
   Create a new Library.
   */
  public void createLibrary() {
    setup(CmdMode.Create, CmdType.Library);
    launchModal();
  }

  /**
   Create a new Program.
   */
  public void createProgram(Library inLibrary) {
    parentLibrary.set(inLibrary);
    setup(CmdMode.Create, CmdType.Program);
    launchModal();
  }

  /**
   Create a new Instrument.
   */
  public void createInstrument(Library inLibrary) {
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
          StringUtils.toProperCsvAnd(Stream.of(
            programs > 0 ? describeCount("Program", programs) : null,
            instruments > 0 ? describeCount("Instrument", instruments) : null
          ).filter(Objects::nonNull).toList())));
      return;
    }

    var bindings = projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(ContentBindingType.Library, binding.getType()) && Objects.equals(binding.getTargetId(), library.getId()))
      .count();
    if (bindings > 0) {
      showWarningDialog("Cannot delete Library", "Library is bound",
        String.format("Cannot delete Library \"%s\" because it is bound to %s",
          library.getName(), describeCount("Template", bindings)));
      return;
    }

    if (!showConfirmationDialog("Delete Library?", "This action cannot be undone.", String.format("Are you sure you want to delete the Library \"%s\"?", library.getName())))
      return;

    projectService.deleteLibrary(library);
  }

  /**
   Clone a library

   @param library to clone
   */
  public void cloneLibrary(Library library) {
    setup(CmdMode.Clone, CmdType.Library);
    LOG.info("Will clone Library \"{}\"", library.getName()); // TODO
  }

  /**
   Move a program

   @param program to move
   */
  public void moveProgram(Program program) {
    setup(CmdMode.Move, CmdType.Program);
    LOG.info("Will move Program \"{}\"", program.getName()); // TODO
  }

  /**
   Clone a program

   @param program to clone
   */
  public void cloneProgram(Program program) {
    setup(CmdMode.Clone, CmdType.Program);
    LOG.info("Will clone Program \"{}\"", program.getName()); // TODO
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
          program.getName(), describeCount("Template", bindings)));
      return;
    }

    if (!showConfirmationDialog("Delete Program?", "This action cannot be undone.", String.format("Are you sure you want to delete the Program \"%s\"?", program.getName())))
      return;

    projectService.deleteProgram(program);
  }

  /**
   Move an instrument

   @param instrument to move
   */
  public void moveInstrument(Instrument instrument) {
    setup(CmdMode.Move, CmdType.Instrument);

    LOG.info("Will move Instrument \"{}\"", instrument.getName()); // TODO
  }

  /**
   Clone an instrument

   @param instrument to clone
   */
  public void cloneInstrument(Instrument instrument) {
    setup(CmdMode.Clone, CmdType.Instrument);

    LOG.info("Will clone Instrument \"{}\"", instrument.getName()); // TODO
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
          instrument.getName(), describeCount("Template", bindings)));
      return;
    }

    if (!showConfirmationDialog("Delete Instrument?", "This action cannot be undone.", String.format("Are you sure you want to delete the Instrument \"%s\"?", instrument.getName())))
      return;

    projectService.deleteInstrument(instrument);
  }

  /**
   Clone a template

   @param template to clone
   */
  public void cloneTemplate(Template template) {
    setup(CmdMode.Clone, CmdType.Template);

    LOG.info("Will clone Template \"{}\"", template.getName()); // TODO
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
          StringUtils.toProperCsvAnd(Stream.of(
            programBindings > 0 ? describeCount("Program", programBindings) : null,
            instrumentBindings > 0 ? describeCount("Instrument", instrumentBindings) : null,
            libraryBindings > 0 ? describeCount("Library", libraryBindings) : null
          ).filter(Objects::nonNull).toList())));
      return;
    }

    if (!showConfirmationDialog("Delete Template?", "This action cannot be undone.", String.format("Are you sure you want to delete the Template \"%s\"?", template.getName())))
      return;

    projectService.deleteTemplate(template);
  }

  /**
   Setup the modal for the given mode and type.

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
   Show a custom confirmation dialog with Yes/No options.

   @param title   title of the dialog
   @param header  header of the dialog
   @param content content of the dialog
   @return true if the user clicked 'Yes', false otherwise
   */
  @SuppressWarnings({"SameParameterValue", "BooleanMethodIsAlwaysInverted"})
  private boolean showConfirmationDialog(String title, String header, String content) {
    // Create a custom dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle(title);

    // Set the header and content
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setHeaderText(header);
    dialogPane.setContentText(content);

    // Add Yes and No buttons
    ButtonType yesButton = new ButtonType("Yes", ButtonType.OK.getButtonData());
    ButtonType noButton = new ButtonType("No", ButtonType.CANCEL.getButtonData());
    dialogPane.getButtonTypes().addAll(yesButton, noButton);

    // Ensure it's resizable and has a preferred width
    dialogPane.setMinHeight(Region.USE_PREF_SIZE);
    dialogPane.setPrefWidth(400); // You can adjust this value

    // Show the dialog and wait for the user to close it
    java.util.Optional<ButtonType> result = dialog.showAndWait();

    // Return true if 'Yes' was clicked, false otherwise
    return result.isPresent() && result.get() == yesButton;
  }

  /**
   Describe a count of something, the the name pluralized if necessary

   @param name  of the thing
   @param count of the thing
   @return description of the count
   */
  private String describeCount(String name, long count) {
    return String.format("%d %s", count, count > 1 ? StringUtils.toPlural(name) : name);
  }
}
