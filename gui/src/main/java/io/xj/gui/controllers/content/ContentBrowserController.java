// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.project.ProjectUpdate;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class ContentBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserController.class);
  private final ProjectService projectService;
  private final LibraryEditorController libraryEditorController;
  private final ProgramEditorController programEditorController;
  private final InstrumentEditorController instrumentEditorController;
  private final ObservableList<Library> libraries = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Program> programs = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Instrument> instruments = FXCollections.observableList(new ArrayList<>());
  private final ObjectProperty<Library> viewingLibrary = new SimpleObjectProperty<>(null);

  @FXML
  protected AnchorPane libraryContentBrowser;

  @FXML
  protected Label libraryTitle;

  @FXML
  protected StackPane container;

  @FXML
  protected TableView<Library> librariesTable;

  @FXML
  protected TableView<Program> programsTable;

  @FXML
  protected TableView<Instrument> instrumentsTable;

  @FXML
  protected TabPane libraryContentTabPane;

  @FXML
  protected Tab programsTab;

  @FXML
  protected Tab instrumentsTab;

  public ContentBrowserController(
    ProjectService projectService,
    LibraryEditorController libraryEditorController,
    ProgramEditorController programEditorController,
    InstrumentEditorController instrumentEditorController
  ) {
    this.projectService = projectService;
    this.libraryEditorController = libraryEditorController;
    this.programEditorController = programEditorController;
    this.instrumentEditorController = instrumentEditorController;
  }

  @Override
  public void onStageReady() {
    initLibraries();
    initPrograms();
    initInstruments();

    var isLibraryBrowser = projectService.contentModeProperty().isEqualTo(ContentMode.LibraryBrowser);
    librariesTable.visibleProperty().bind(isLibraryBrowser);
    librariesTable.managedProperty().bind(isLibraryBrowser);

    var isProgramOrInstrumentBrowser =
      projectService.contentModeProperty().isEqualTo(ContentMode.ProgramBrowser)
        .or(projectService.contentModeProperty().isEqualTo(ContentMode.InstrumentBrowser));
    libraryContentBrowser.visibleProperty().bind(isProgramOrInstrumentBrowser);
    libraryContentBrowser.managedProperty().bind(isProgramOrInstrumentBrowser);

    var visible = projectService.isStateReadyProperty()
      .and(projectService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(
        projectService.contentModeProperty().isEqualTo(ContentMode.LibraryBrowser)
          .or(projectService.contentModeProperty().isEqualTo(ContentMode.ProgramBrowser))
          .or(projectService.contentModeProperty().isEqualTo(ContentMode.InstrumentBrowser)));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    viewingLibrary.addListener((o, ov, value) -> {
      updatePrograms();
      updateInstruments();
    });
    libraryTitle.textProperty().bind(Bindings.createStringBinding(
      () -> Objects.isNull(viewingLibrary.get()) ? "" : viewingLibrary.get().getName(),
      viewingLibrary));
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Initialize the libraries table.
   */
  private void initLibraries() {
    addColumn(librariesTable, 200, "name", "Name");
    setupData(
      librariesTable,
      libraries,
      library -> LOG.debug("Did select Library \"{}\"", library.getName()),
      this::openLibrary
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Libraries, this::updateLibraries);
  }

  /**
   Update the libraries table data.
   */
  private void updateLibraries() {
    libraries.setAll(projectService.getLibraries());
  }

  /**
   Initialize the programs table.
   */
  private void initPrograms() {
    addColumn(programsTable, 200, "name", "Name");
    addColumn(programsTable, 90, "type", "Type");
    addColumn(programsTable, 50, "key", "Key");
    addColumn(programsTable, 50, "tempo", "Tempo");
    addColumn(programsTable, 50, "density", "Density");
    setupData(
      programsTable,
      programs,
      program -> LOG.debug("Did select Program \"{}\"", program.getName()),
      program -> programEditorController.editProgram(program.getId())
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Programs, this::updatePrograms);
  }

  /**
   Update the programs table data.
   */
  private void updatePrograms() {
    programs.setAll(projectService.getPrograms().stream()
      .filter(program -> Objects.isNull(viewingLibrary.get()) || Objects.equals(program.getLibraryId(), viewingLibrary.get().getId()))
      .toList());
  }

  /**
   Initialize the instruments table.
   */
  private void initInstruments() {
    addColumn(instrumentsTable, 200, "name", "Name");
    addColumn(instrumentsTable, 90, "type", "Type");
    addColumn(instrumentsTable, 90, "mode", "Mode");
    addColumn(instrumentsTable, 50, "density", "Density");
    addColumn(instrumentsTable, 50, "volume", "Volume");
    setupData(
      instrumentsTable,
      instruments,
      instrument -> LOG.debug("Did select Instrument \"{}\"", instrument.getName()),
      instrument -> instrumentEditorController.editInstrument(instrument.getId())
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Instruments, this::updateInstruments);
  }

  /**
   Update the instruments table data.
   */
  private void updateInstruments() {
    instruments.setAll(projectService.getInstruments().stream()
      .filter(instrument -> Objects.isNull(viewingLibrary.get()) || Objects.equals(instrument.getLibraryId(), viewingLibrary.get().getId()))
      .toList());
  }

  /**
   Open the given library in the content browser.

   @param library library to open
   */
  public void openLibrary(Library library) {
    viewingLibrary.set(library);
    if (Objects.nonNull(library))
      if (projectService.getContent().getInstruments().stream()
        .anyMatch(instrument -> Objects.equals(instrument.getLibraryId(), library.getId()))) {
        libraryContentTabPane.selectionModelProperty().get().select(instrumentsTab);
        projectService.contentModeProperty().set(ContentMode.InstrumentBrowser);
      } else {
        libraryContentTabPane.selectionModelProperty().get().select(programsTab);
        projectService.contentModeProperty().set(ContentMode.ProgramBrowser);
      }
  }
}
