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
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    windowTitle.bind(Bindings.createStringBinding(
      () -> String.format("%s %s", mode.get(), type.get()),
      mode, type
    ));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    createAndShowModal(windowTitle.get());
  }

  /**
   Create a new Template.
   */
  public void createTemplate() {
    LOG.info("Will create Template"); // TODO
    mode.set(CmdMode.Create);
    type.set(CmdType.Template);
  }

  /**
   Create a new Library.
   */
  public void createLibrary() {
    LOG.info("Will create Library"); // TODO
    mode.set(CmdMode.Create);
    type.set(CmdType.Library);
  }

  /**
   Create a new Program.
   */
  public void createProgram(Library inLibrary) {
    LOG.info("Will create Program in Library \"{}\"", inLibrary.getName()); // TODO
    parentLibrary.set(inLibrary);
    mode.set(CmdMode.Create);
    type.set(CmdType.Program);
  }

  /**
   Create a new Instrument.
   */
  public void createInstrument(Library inLibrary) {
    LOG.info("Will create Instrument in Library \"{}\"", inLibrary.getName()); // TODO
    parentLibrary.set(inLibrary);
    mode.set(CmdMode.Create);
    type.set(CmdType.Instrument);
  }

  public void deleteLibrary(Library library) {
    mode.set(CmdMode.Delete);
    type.set(CmdType.Library);

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

  public void cloneLibrary(Library library) {
    LOG.info("Will clone Library \"{}\"", library.getName()); // TODO
    mode.set(CmdMode.Clone);
    type.set(CmdType.Library);
  }

  public void moveProgram(Program program) {
    LOG.info("Will move Program \"{}\"", program.getName()); // TODO
    mode.set(CmdMode.Move);
    type.set(CmdType.Program);
  }

  public void cloneProgram(Program program) {
    LOG.info("Will clone Program \"{}\"", program.getName()); // TODO
    mode.set(CmdMode.Clone);
    type.set(CmdType.Program);
  }

  public void deleteProgram(Program program) {
    mode.set(CmdMode.Delete);
    type.set(CmdType.Program);

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

  public void moveInstrument(Instrument instrument) {
    LOG.info("Will move Instrument \"{}\"", instrument.getName()); // TODO
    mode.set(CmdMode.Move);
    type.set(CmdType.Instrument);
  }

  public void cloneInstrument(Instrument instrument) {
    LOG.info("Will clone Instrument \"{}\"", instrument.getName()); // TODO
    mode.set(CmdMode.Clone);
    type.set(CmdType.Instrument);
  }

  public void deleteInstrument(Instrument instrument) {
    mode.set(CmdMode.Delete);
    type.set(CmdType.Instrument);

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

  public void cloneTemplate(Template template) {
    LOG.info("Will clone Template \"{}\"", template.getName()); // TODO
    mode.set(CmdMode.Clone);
    type.set(CmdType.Template);
  }

  public void deleteTemplate(Template template) {
    mode.set(CmdMode.Delete);
    type.set(CmdType.Template);

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
