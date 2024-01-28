// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.ReadyAfterBootModalController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class InstrumentAddAudioController extends ReadyAfterBootModalController {
  private static final Logger LOG = LoggerFactory.getLogger(InstrumentAddAudioController.class);
  private static final String WINDOW_TITLE = "Add Instrument Audio";
  private final ObjectProperty<Library> library = new SimpleObjectProperty<>();
  private final ObjectProperty<Program> program = new SimpleObjectProperty<>();
  private final ObjectProperty<Instrument> instrument = new SimpleObjectProperty<>();
  private final ObjectProperty<UUID> instrumentId = new SimpleObjectProperty<>();
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


  public InstrumentAddAudioController(
    @Value("classpath:/views/content/instrument/instrument-add-audio.fxml") Resource fxml,
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
    // todo stage ready for add audio
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
    // TODO add audio pressed OK
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
   Launch the modal to add a audio to the given instrument

   @param instrumentId for which to add audio
   */
  public void addAudioToInstrument(UUID instrumentId) {
    this.instrumentId.set(instrumentId);
    launchModal();
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
